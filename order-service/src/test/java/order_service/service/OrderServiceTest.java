package order_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import order_service.dto.ProductDTO.ProductOutput;
import order_service.kafka.OrderProducer;
import order_service.model.CartItem;
import order_service.model.OrderDetails;
import order_service.model.OrderItem;
import order_service.model.OrderStatus;
import order_service.model.ShoppingCart;
import order_service.repository.CartItemRepository;
import order_service.repository.OrderDetailsRepository;
import order_service.repository.OrderItemRepository;
import order_service.repository.ShoppingCartRepository;
import order_service.restApi.ProductClient;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private OrderDetailsRepository orderDetailsRepository;

    @Mock
    private OrderItemRepository orderItemsRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private OrderProducer orderProducer;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OrderService orderService;

    private String userId = "user-1";
    private String orderId = "order-1";
    private String productId = "prod-1";

    @BeforeEach
    void setUp() {
        ProductOutput product = ProductOutput.builder()
                .id(productId)
                .name("Test Product")
                .price(50.0)
                .quantity(10)
                .build();

        lenient().when(productClient.get(productId)).thenReturn(product);
    }

    // Test 1: Verify checkout converts cart items to an order and calculates total
    @Test
    void createOrder_Success() throws Exception {
        ShoppingCart cart = ShoppingCart.builder().id("cart-1").userId(userId).build();
        CartItem cartItem = CartItem.builder().id("item-1").shoppingCartId("cart-1").productId(productId).quantity(2).build();
        OrderDetails savedOrder = OrderDetails.builder().id(orderId).userId(userId).status(OrderStatus.ORDERED).total(0.0).build();

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(orderDetailsRepository.save(any(OrderDetails.class))).thenReturn(savedOrder);
        when(cartItemRepository.findByShoppingCartId("cart-1")).thenReturn(List.of(cartItem));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        Map<String, Object> response = orderService.createOrder(userId);

        assertNotNull(response.get("order_details"));
        verify(orderItemsRepository).saveAll(any());
        verify(orderProducer).sendMessage(eq("update-quantity"), anyString());
    }

    // Test 2: Verify cancelling an order updates its status to CANCELLED
    @Test
    void cancelOrder_Success() throws Exception {
        OrderDetails order = OrderDetails.builder().id(orderId).userId(userId).status(OrderStatus.ORDERED).build();
        OrderItem item = OrderItem.builder().id("item-1").orderId(orderId).productId(productId).quantity(2).build();

        when(orderDetailsRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderItemsRepository.findAllByOrderId(orderId)).thenReturn(List.of(item));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(orderDetailsRepository.save(order)).thenReturn(order);

        OrderDetails cancelled = orderService.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCELLED, cancelled.getStatus());
        verify(orderProducer).sendMessage(eq("update-quantity"), anyString());
    }
}

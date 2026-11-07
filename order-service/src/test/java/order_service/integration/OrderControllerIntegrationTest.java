package order_service.integration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import order_service.dto.ProductDTO.ProductOutput;
import order_service.kafka.OrderProducer;
import order_service.model.CartItem;
import order_service.model.OrderDetails;
import order_service.model.OrderStatus;
import order_service.model.ShoppingCart;
import order_service.repository.CartItemRepository;
import order_service.repository.OrderDetailsRepository;
import order_service.repository.OrderItemRepository;
import order_service.repository.ShoppingCartRepository;
import order_service.restApi.ProductClient;
import order_service.service.JwtService;

@SpringBootTest
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private OrderDetailsRepository orderDetailsRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private ShoppingCartRepository shoppingCartRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private OrderProducer orderProducer;

    @MockitoBean
    private JwtService jwtService;

    private String userId = "client-user-1";
    private String token = "Bearer valid-token";
    private String productId = "prod-1";

    @BeforeEach
    void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(springSecurity()).build();

        // Mock JWT authentication for client user
        when(jwtService.extractUserId("valid-token")).thenReturn(Map.of("id", userId, "role", "CLIENT"));

        // Mock Product microservice Feign response
        ProductOutput product = ProductOutput.builder()
                .id(productId)
                .name("Smart Phone")
                .price(100.0)
                .quantity(10)
                .build();

        lenient().when(productClient.get(productId)).thenReturn(product);
    }

    // Integration Test 1: Full Checkout HTTP POST /orders flow using mocked database
    @Test
    void createOrder_IntegrationSuccess() throws Exception {
        ShoppingCart cart = ShoppingCart.builder().id("cart-1").userId(userId).build();
        CartItem cartItem = CartItem.builder().id("item-1").shoppingCartId("cart-1").productId(productId).quantity(2).build();

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(java.util.Optional.of(cart));
        when(cartItemRepository.findByShoppingCartId("cart-1")).thenReturn(List.of(cartItem));
        when(orderDetailsRepository.save(any(OrderDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orderItemRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDetails expectedOrder = OrderDetails.builder()
                .userId(userId)
                .status(OrderStatus.ORDERED)
                .total(200.0)
                .build();
        when(orderDetailsRepository.findAllByUserId(userId, null)).thenReturn(List.of(expectedOrder));

        mockMvc.perform(post("/orders")
                .header("Authorization", token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.order_details.status").value("ORDERED"))
                .andExpect(jsonPath("$.order_details.total").value(200.0));

        // Verify order was saved (mocked)
        List<OrderDetails> orders = orderDetailsRepository.findAllByUserId(userId, null);
        assertEquals(1, orders.size());
        assertEquals(200.0, orders.get(0).getTotal());
    }

    // Integration Test 2: Fetching order by ID HTTP GET /orders/{id} flow using mocked database
    @Test
    void getOrderById_IntegrationSuccess() throws Exception {
        OrderDetails savedOrder = OrderDetails.builder()
                .id("order-123")
                .userId(userId)
                .status(OrderStatus.ORDERED)
                .total(150.0)
                .build();

        when(orderDetailsRepository.findById("order-123")).thenReturn(java.util.Optional.of(savedOrder));

        mockMvc.perform(get("/orders/order-123")
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("order-123"))
                .andExpect(jsonPath("$.status").value("ORDERED"));
    }
}

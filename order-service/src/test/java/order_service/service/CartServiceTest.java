package order_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import order_service.dto.CartDTO.CartItemInput;
import order_service.dto.CartDTO.CartItemOutput;
import order_service.dto.ProductDTO.ProductOutput;
import order_service.model.CartItem;
import order_service.model.ShoppingCart;
import order_service.repository.CartItemRepository;
import order_service.repository.ShoppingCartRepository;
import order_service.restApi.ProductClient;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ShoppingCartRepository shoppingCartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    private String userId = "user-123";
    private String cartId = "cart-123";
    private String productId = "prod-123";
    private ProductOutput productOutput;

    @BeforeEach
    void setUp() {
        productOutput = ProductOutput.builder()
                .id(productId)
                .name("Sample Phone")
                .price(100.0)
                .quantity(10)
                .build();
    }

    // Test 1: Verify adding a product creates a cart and saves the item
    @Test
    void createCartItem_Success() {
        CartItemInput input = new CartItemInput(productId, 2);
        ShoppingCart newCart = ShoppingCart.builder().id(cartId).userId(userId).build();
        CartItem savedItem = CartItem.builder().id("item-1").shoppingCartId(cartId).productId(productId).quantity(2).build();

        when(productClient.get(productId)).thenReturn(productOutput);
        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(newCart));
        when(cartItemRepository.findByShoppingCartIdAndProductId(cartId, productId)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(savedItem);

        CartItemOutput result = cartService.createCartItem(input, userId);

        assertNotNull(result);
        assertEquals("item-1", result.id());
        assertEquals(2, result.quantity());
    }

    // Test 2: Verify retrieving cart items combines cart data with product details
    @Test
    void getItems_Success() {
        ShoppingCart cart = ShoppingCart.builder().id(cartId).userId(userId).build();
        CartItem item = CartItem.builder().id("item-1").shoppingCartId(cartId).productId(productId).quantity(2).build();

        when(shoppingCartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByShoppingCartId(cartId)).thenReturn(List.of(item));
        when(productClient.getProductsCarts(Set.of(productId))).thenReturn(List.of(productOutput));

        List<CartItemOutput> items = cartService.getItems(userId);

        assertEquals(1, items.size());
        assertEquals("Sample Phone", items.get(0).product().name());
    }
}

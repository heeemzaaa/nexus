package order_service.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import order_service.dto.CartDTO.CartItemInput;
import order_service.dto.CartDTO.CartItemOutput;
import order_service.dto.ProductDTO.ProductOutput;
import order_service.exception.BadRequestException;
import order_service.exception.NotFoundException;
import order_service.mapper.CartItemMapper;
import order_service.model.CartItem;
import order_service.model.ShoppingCart;
import order_service.repository.CartItemRepository;
import order_service.repository.ShoppingCartRepository;
import order_service.restApi.ProductClient;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductClient productClient;

    public CartItemOutput createCartItem(final CartItemInput cartItemData, final String userId) {
        ProductOutput product = this.productClient.get(cartItemData.productId());

        // Create a shopping cart if not exists
        Optional<ShoppingCart> existingShoppingCart = this.shoppingCartRepository.findByUserId(userId);
        if (existingShoppingCart.isEmpty()) {
            existingShoppingCart = Optional.of(shoppingCartRepository.insert(ShoppingCart.builder()
                    .userId(userId)
                    .build()));
        }

        final ShoppingCart shoppingCart = existingShoppingCart.get();

        final var existingCartItem = cartItemRepository.findByShoppingCartIdAndProductId(shoppingCart.getId(),
                cartItemData.productId());
        final CartItem cartItem;
        if (existingCartItem.isPresent()) {
            existingCartItem.get().setQuantity(existingCartItem.get().getQuantity() + cartItemData.quantity());
            cartItem = existingCartItem.get();
        } else {
            cartItem = CartItem.builder()
                    .shoppingCartId(shoppingCart.getId())
                    .productId(product.id())
                    .quantity(cartItemData.quantity())
                    .build();
        }

        if (cartItem.getQuantity() > product.quantity())
            throw new BadRequestException("The quantity has reached the maximum.");

        final CartItem savedItem = this.cartItemRepository.save(cartItem);
        return CartItemMapper.toCartItemOutput(savedItem, null);
    }

    public List<CartItemOutput> getItems(final String userId) {
        final Optional<ShoppingCart> existingShoppingCart = shoppingCartRepository.findByUserId(userId);
        if (existingShoppingCart.isEmpty()) {
            return List.of();
        }

        final ShoppingCart shoppingCart = existingShoppingCart.get();

        List<CartItem> cartItems = cartItemRepository.findByShoppingCartId(shoppingCart.getId());
        final Set<String> productIds = cartItems.stream().map(item -> item.getProductId()).collect(Collectors.toSet());

        Map<String, ProductOutput> products = this.productClient.getProductsCarts(productIds)
                .stream().collect(Collectors.toMap(ProductOutput::id, Function.identity()));

        cartItems.stream().forEach(item -> {
            if (products.get(item.getProductId()) == null) {
                this.cartItemRepository.deleteById(item.getId());
            }
        });

        cartItems = cartItems.stream().filter(item -> products.get(item.getProductId()) != null).toList();
        return cartItems.stream().map(item -> CartItemMapper.toCartItemOutput(item, products.get(item.getProductId())))
                .toList();
    }

    public CartItemOutput updateItem(final String itemId, final CartItemInput cartItemData, final String userId) {
        final CartItem cartItem = this.getMyItem(itemId, userId);
        cartItem.setQuantity(cartItemData.quantity());
        final CartItem item = this.cartItemRepository.save(cartItem);
        return CartItemMapper.toCartItemOutput(item, null);
    }

    public Map<String, String> deleteItem(final String itemId, final String userId) {
        final CartItem cartItem = this.getMyItem(itemId, userId);
        this.cartItemRepository.delete(cartItem);
        return Map.of(
                "id", cartItem.getId(),
                "message", "item deleted successfully.");
    }

    private CartItem getMyItem(final String itemId, final String userId) {
        final CartItem cartItem = this.cartItemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Whoops! item not found."));

        final ShoppingCart shoppingCart = this.shoppingCartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("No shopping cart found."));

        if (!shoppingCart.getId().equals(cartItem.getShoppingCartId())) {
            throw new AccessDeniedException("Access denied");
        }

        return cartItem;
    }

}

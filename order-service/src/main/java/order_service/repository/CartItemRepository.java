package order_service.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import order_service.model.CartItem;

public interface CartItemRepository extends MongoRepository<CartItem, String> {

    List<CartItem> findByShoppingCartId(final String shoppingCartId);

    Optional<CartItem> findByShoppingCartIdAndProductId(final String shoppingCartId, final String productId);

}

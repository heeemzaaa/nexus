package order_service.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import order_service.model.ShoppingCart;

public interface ShoppingCartRepository extends MongoRepository<ShoppingCart, String> {

    Optional<ShoppingCart> findByUserId(final String userId);
    

}

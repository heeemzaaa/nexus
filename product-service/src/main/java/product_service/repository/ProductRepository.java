package product_service.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import product_service.model.Product;
import product_service.model.ProductStatus;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Page<Product> findByUserId(String userId, Pageable pageable);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    Page<Product> findByStatusAndNameContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            ProductStatus status1,
            String name,
            ProductStatus status2,
            String description,
            Pageable pageable);

    List<Product> findByIdIn(final Set<String> productIds);

}

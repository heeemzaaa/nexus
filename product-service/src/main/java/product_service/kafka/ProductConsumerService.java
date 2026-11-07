package product_service.kafka;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import product_service.model.Product;
import product_service.model.ProductStatus;
import product_service.repository.ProductRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductConsumerService {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "update-product-status", groupId = "productGroup")
    public void deleteProduct(String productId) {
        try {
            Optional<Product> product = this.productRepository.findById(productId);

            if (product.isPresent()) {
                product.get().setStatus(ProductStatus.ACTIVE);
                this.productRepository.save(product.get());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "update-quantity", groupId = "productGroup")
    public void updateProductQuantity(String event) {
        try {
            ItemEvent itemEvent = objectMapper.readValue(event, ItemEvent.class);

            List<Product> products = productRepository.findByIdIn(itemEvent.getItems().keySet());
            products.forEach(p -> {
                if (itemEvent.getIsIncrement()) {
                    p.setQuantity(p.getQuantity() + itemEvent.getItems().get(p.getId()));
                } else {
                    final Integer result = p.getQuantity() - itemEvent.getItems().get(p.getId());
                    p.setQuantity(result >= 0 ? result : 0);
                }

                productRepository.save(p);
            });

        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

}

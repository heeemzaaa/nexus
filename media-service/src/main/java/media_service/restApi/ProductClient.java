package media_service.restApi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import media_service.config.FeignClientConfiguration;
import media_service.model.dto.ProductDTO.ProductInput;

@FeignClient(name = "product-service", configuration = FeignClientConfiguration.class)
public interface ProductClient {
    
    @GetMapping("/api/products/{id}")
    public ProductInput getProduct(@PathVariable("id") String productId);

}

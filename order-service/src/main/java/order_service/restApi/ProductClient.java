package order_service.restApi;

import java.util.List;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import order_service.dto.ProductDTO.ProductOutput;

@FeignClient(name = "product-service")
public interface ProductClient {

    @GetMapping("/api/products/{id}")
    public ProductOutput get(@PathVariable("id") String productId);

    @PostMapping("/api/products/carts")
    public List<ProductOutput> getProductsCarts(@RequestBody Set<String> productIds);

}

package product_service.restApi;

import java.util.List;
import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "media-service")
public interface MediaClient {

    @GetMapping("/api/media/products/{id}")
    public List<String> getProductMedia(@PathVariable("id") String productId);

    @PostMapping("/api/media/products")
    public Map<String, List<String>> getMediaProducts(@RequestBody List<String> productIds);

}

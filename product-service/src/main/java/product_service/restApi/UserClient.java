package product_service.restApi;

import java.util.Map;
import java.util.Set;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import product_service.dto.UserDTO;

@FeignClient(name = "user-service")
public interface UserClient {

    @PostMapping("/api/users/products")
    public Map<String, UserDTO> getUserProducts(@RequestBody Set<String> userIds);

}

package media_service.restApi;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserClient {
    
    @PutMapping("/api/users/me")
    public Map<String, Object> updateAvatar(@RequestBody String avatarUrl);

}

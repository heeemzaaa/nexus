package media_service.service;

import java.util.Base64;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class JwtService {

    public Map<String, Object> extractUserId(String token) throws Exception {
        String[] parts = token.split("\\.");
        String payload = parts[1];

        String json = new String(Base64.getUrlDecoder().decode(payload));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(json, Map.class);
        return map;
    }

}

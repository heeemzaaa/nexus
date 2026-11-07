package user_service.service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import user_service.model.User;

@Service
public class JwtService {

    @Value("${SECRET_KEY:y04VbAKcuOebkaYbSwoRNTKimXUaG1RUNoUsrhsPsYR}")
    private String secretKey ;

    private Key getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(
            User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .claim("id", user.getId())
                .claim("avatar", user.getAvatarUrl())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 3600000 * 24))
                .signWith(getSignInKey())
                .compact();
    }

    public Map<String, Object> extractUserId(String token) throws Exception {
        String[] parts = token.split("\\.");
        String payload = parts[1];

        String json = new String(Base64.getUrlDecoder().decode(payload));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(json, Map.class);
        return map;
    }

}

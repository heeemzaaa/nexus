package api_gateway.service;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import api_gateway.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${SECRET_KEY:y04VbAKcuOebkaYbSwoRNTKimXUaG1RUNoUsrhsPsYR}")
    private String secretKey ;

    private SecretKey getSignInKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public User ValidateAndExtractUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        User user = User.builder()
                .name(claims.get("name", String.class))
                .email(claims.getSubject())
                .id(claims.get("id", String.class))
                .role(claims.get("role", String.class))
                .build();
        return user;
    }

}

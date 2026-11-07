package user_service.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import user_service.exception.BadRequestException;
import user_service.model.Role;
import user_service.model.User;
import user_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public Map<String, Object> register(String name, String email, String password, Role role) {

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(role)
                .build();

        User savedUser = userRepository.save(user);

        String jws = jwtService.generateToken(user);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jws);
        response.put("message", "User registered successfully");

        return response;

    }


    public Map<String, Object> login(String email, String password) {

        var auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(email, password));

        Map<String, Object> response = new HashMap<>();
        User user = (User) auth.getPrincipal();

        String jws = jwtService.generateToken(user);

        response.put("token", jws);
        response.put("message", "User logged in successfully");

        return response;

    }

}

package user_service.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import user_service.model.DTO.AuthDTO.LoginInput;
import user_service.model.DTO.AuthDTO.RegisterInput;
import user_service.service.AuthService;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody @Valid RegisterInput request) {
        return authService.register(request.name(), request.email(),
                request.password(), request.role());
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody @Valid LoginInput request) {

        return authService.login(request.email(), request.password());
    }

}

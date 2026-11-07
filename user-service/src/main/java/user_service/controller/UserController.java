package user_service.controller;

import java.util.Map;
import java.util.Set;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import user_service.model.DTO.UserDTO.AvatarInput;
import user_service.model.DTO.UserDTO.UserOutput;
import user_service.service.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Map<String, Object> getProfile(@AuthenticationPrincipal String id) {
        return userService.getUser(id);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getUser(@PathVariable("id") String userId) {
        return this.userService.getUser(userId);
    }

    @PutMapping("/me")
    public Map<String, Object> updateAvatar(@RequestBody String avatarUrl, @AuthenticationPrincipal String id) {
        AvatarInput avatarInput = new AvatarInput(id, avatarUrl);
        return userService.updateAvatar(avatarInput);
    }

    @PostMapping("/products")
    public Map<String, UserOutput> getUserProducts(@RequestBody Set<String> userIds) {
        return this.userService.getUserProducts(userIds);
    }

}

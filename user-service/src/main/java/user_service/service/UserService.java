package user_service.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import user_service.exception.NotFoundException;
import user_service.model.DTO.UserDTO.AvatarInput;
import user_service.model.DTO.UserDTO.UserOutput;
import user_service.model.User;
import user_service.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public Map<String, Object> getUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Whoops! user not found."));

        Map<String, Object> response = new HashMap<>();
        response.put("user_details", UserToUserOutput(user));

        return response;
    }

    public Map<String, Object> updateAvatar(AvatarInput avatarInput) {
        
        User user = userRepository.findById(avatarInput.userId()).get();
        user.setAvatarUrl(avatarInput.avatarUrl());
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Avatar updated successfully");

        return response;
    }

    private UserOutput UserToUserOutput(User user) {
        return UserOutput.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .avatar(user.getAvatarUrl())
                .build();
    }

    public Map<String, UserOutput> getUserProducts(Set<String> userIds) {
        List<User> users = this.userRepository.findByIdIn(userIds);

        return users.stream()
                .collect(Collectors.toMap(User::getId, user -> UserOutput.builder()
                        .name(user.getName())
                        .avatar(user.getAvatarUrl()).build()));
    }

}

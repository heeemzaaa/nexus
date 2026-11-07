package user_service.model.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;

public class UserDTO {

    public static record AvatarInput(
            @NotEmpty(message = "User id cannot be empty") String userId,
            @NotEmpty(message = "Avatar url cannot be empty") String avatarUrl) {

    }

    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static record UserOutput(
            String id,
            String name,
            String email,
            String role,
            String avatar) {
    }

}

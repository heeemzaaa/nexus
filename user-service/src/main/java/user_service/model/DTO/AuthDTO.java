package user_service.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import user_service.model.Role;

public class AuthDTO {

        public static record RegisterInput(
                        @NotBlank(message = "User name cannot be empty") @Size(min = 3, max = 20, message = "User name must be between 3 and 20 character") String name,
                        @NotBlank(message = "User email cannot be empty") @Email(message = "Please provide a valid email") String email,
                        @Size(min = 8, max = 50, message = "User password must be between 8 and 50 character") String password,
                        @NotNull(message = "User role cannot be empty") Role role) {

                public RegisterInput {
                        if (name != null && email != null) {
                                name = name.trim();
                                email = email.toLowerCase().trim();

                        }
                }
        }

        @Builder
        public static record AuthOutput(
                        String id,
                        String name,
                        String email,
                        String role,
                        String token) {

        }

        @Builder
        public static record LoginInput(
                        String email,
                        String password) {
        }

}
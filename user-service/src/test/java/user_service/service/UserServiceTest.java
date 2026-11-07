package user_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import user_service.exception.NotFoundException;
import user_service.model.DTO.UserDTO.AvatarInput;
import user_service.model.DTO.UserDTO.UserOutput;
import user_service.model.Role;
import user_service.model.User;
import user_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String testUserId;

    @BeforeEach
    void setUp() {
        testUserId = "507f1f77bcf86cd799439011";
        testUser = User.builder()
                .id(testUserId)
                .name("John Doe")
                .email("john@example.com")
                .password("hashed_password")
                .role(Role.CLIENT)
                .avatarUrl("https://example.com/avatar.jpg")
                .build();
    }

    @Test
    @DisplayName("Should return user details when user exists")
    void testGetUserSuccess() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        Map<String, Object> result = userService.getUser(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("user_details"));
        UserOutput userOutput = (UserOutput) result.get("user_details");
        assertEquals("John Doe", userOutput.name());
        assertEquals("john@example.com", userOutput.email());
        assertEquals("CLIENT", userOutput.role());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw NotFoundException when user does not exist")
    void testGetUserNotFound() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, 
            () -> userService.getUser(testUserId));
        assertEquals("Whoops! user not found.", exception.getMessage());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    @DisplayName("Should update avatar successfully")
    void testUpdateAvatarSuccess() {
        // Arrange
        String newAvatarUrl = "https://example.com/new-avatar.jpg";
        AvatarInput avatarInput = new AvatarInput(testUserId, newAvatarUrl);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Act
        Map<String, Object> result = userService.updateAvatar(avatarInput);

        // Assert
        assertNotNull(result);
        assertTrue(result.containsKey("message"));
        assertEquals("Avatar updated successfully", result.get("message"));
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should get user products for given user IDs")
    void testGetUserProductsSuccess() {
        // Arrange
        String userId1 = "507f1f77bcf86cd799439011";
        String userId2 = "507f1f77bcf86cd799439012";
        Set<String> userIds = Set.of(userId1, userId2);

        User user1 = User.builder()
                .id(userId1)
                .name("John Doe")
                .email("john@example.com")
                .role(Role.CLIENT)
                .avatarUrl("https://example.com/avatar1.jpg")
                .build();

        User user2 = User.builder()
                .id(userId2)
                .name("Jane Smith")
                .email("jane@example.com")
                .role(Role.SELLER)
                .avatarUrl("https://example.com/avatar2.jpg")
                .build();

        when(userRepository.findByIdIn(userIds)).thenReturn(List.of(user1, user2));

        // Act
        Map<String, UserOutput> result = userService.getUserProducts(userIds);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey(userId1));
        assertTrue(result.containsKey(userId2));
        assertEquals("John Doe", result.get(userId1).name());
        assertEquals("Jane Smith", result.get(userId2).name());
        verify(userRepository, times(1)).findByIdIn(userIds);
    }

    @Test
    @DisplayName("Should return empty map when no users found for given IDs")
    void testGetUserProductsEmpty() {
        // Arrange
        Set<String> userIds = Set.of("invalid_id1", "invalid_id2");
        when(userRepository.findByIdIn(userIds)).thenReturn(List.of());

        // Act
        Map<String, UserOutput> result = userService.getUserProducts(userIds);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByIdIn(userIds);
    }

    @Test
    @DisplayName("Should handle user with null avatar URL")
    void testGetUserWithNullAvatarUrl() {
        // Arrange
        User userWithoutAvatar = User.builder()
                .id(testUserId)
                .name("Test User")
                .email("test@example.com")
                .role(Role.CLIENT)
                .avatarUrl(null)
                .build();

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(userWithoutAvatar));

        // Act
        Map<String, Object> result = userService.getUser(testUserId);

        // Assert
        assertNotNull(result);
        UserOutput userOutput = (UserOutput) result.get("user_details");
        assertNull(userOutput.avatar());
        verify(userRepository, times(1)).findById(testUserId);
    }
}

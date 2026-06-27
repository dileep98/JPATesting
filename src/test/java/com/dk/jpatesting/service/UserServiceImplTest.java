package com.dk.jpatesting.service;

import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.entity.User;
import com.dk.jpatesting.entity.UserStatus;
import com.dk.jpatesting.exception.DuplicateEmailException;
import com.dk.jpatesting.exception.UserNotFoundException;
import com.dk.jpatesting.mapper.UserMapper;
import com.dk.jpatesting.repository.UserRepository;
import com.dk.jpatesting.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserResponse userResponse;
    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+11234567890")
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userResponse = UserResponse.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+11234567890")
                .status(UserStatus.ACTIVE)
                .build();

        createRequest = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+11234567890")
                .build();
    }

    // ── createUser ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUserSuccessfully() {
            when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
            when(userMapper.toEntity(createRequest)).thenReturn(user);
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.createUser(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");

            verify(userRepository).existsByEmail(createRequest.getEmail());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when email exists")
        void shouldThrowWhenEmailAlreadyExists() {
            when(userRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(createRequest))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("john.doe@example.com");

            verify(userRepository, never()).save(any());
        }
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getUserById()")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsers {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(user));
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            List<UserResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyList() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<UserResponse> result = userService.getAllUsers();

            assertThat(result).isEmpty();
        }
    }

    // ── updateUser ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("updateUser()")
    class UpdateUser {

        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUserSuccessfully() {
            UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                    .firstName("Jane")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(user)).thenReturn(user);
            when(userMapper.toResponse(user)).thenReturn(userResponse);

            UserResponse result = userService.updateUser(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(userMapper).updateEntityFromRequest(updateRequest, user);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should throw DuplicateEmailException when new email already taken")
        void shouldThrowWhenNewEmailAlreadyTaken() {
            UpdateUserRequest updateRequest = UpdateUserRequest.builder()
                    .email("taken@example.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateUser(1L, updateRequest))
                    .isInstanceOf(DuplicateEmailException.class);

            verify(userRepository, never()).save(any());
        }
    }

    // ── deleteUser ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));  // ← was existsById

            userService.deleteUser(1L);

            verify(userRepository).delete(user);  // ← was deleteById(1L)
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());  // ← was existsById

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userRepository, never()).delete(any());  // ← was deleteById
        }
    }
}
package com.dk.jpatesting.repository;

import com.dk.jpatesting.entity.User;
import com.dk.jpatesting.entity.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        savedUser = userRepository.save(User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+11234567890")
                .build());
    }

    @Test
    @DisplayName("should find user by email")
    void shouldFindByEmail() {
        Optional<User> result = userRepository.findByEmail("john.doe@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getFirstName()).isEqualTo("John");
    }

    @Test
    @DisplayName("should return empty when email not found")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<User> result = userRepository.findByEmail("unknown@example.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return true when email exists")
    void shouldReturnTrueWhenEmailExists() {
        boolean exists = userRepository.existsByEmail("john.doe@example.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should find users by status")
    void shouldFindByStatus() {
        List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

        assertThat(activeUsers).hasSize(1);
        assertThat(activeUsers.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("should save user and generate id")
    void shouldSaveAndGenerateId() {
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
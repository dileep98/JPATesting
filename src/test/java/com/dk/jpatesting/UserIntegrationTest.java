package com.dk.jpatesting;

import com.dk.jpatesting.dto.request.CreateOrderRequest;
import com.dk.jpatesting.dto.request.CreateUserRequest;
import com.dk.jpatesting.dto.request.UpdateUserRequest;
import com.dk.jpatesting.dto.response.OrderResponse;
import com.dk.jpatesting.dto.response.UserResponse;
import com.dk.jpatesting.exception.DuplicateEmailException;
import com.dk.jpatesting.exception.OrderNotFoundException;
import com.dk.jpatesting.exception.UserNotFoundException;
import com.dk.jpatesting.repository.UserRepository;
import com.dk.jpatesting.service.OrderService;
import com.dk.jpatesting.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Integration Tests")
class UserIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void cleanUp() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("should create user successfully")
    void shouldCreateUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("+11234567890")
                .build();

        UserResponse response = userService.createUser(request);

        assertThat(response.getId()).isNotNull();
        assertThat(response.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(response.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    @Order(2)
    @DisplayName("should throw DuplicateEmailException when email already exists")
    void shouldThrowOnDuplicateEmail() {
        createTestUser("john.doe@example.com");

        CreateUserRequest duplicate = CreateUserRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("john.doe@example.com")
                .build();

        assertThatThrownBy(() -> userService.createUser(duplicate))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("john.doe@example.com");
    }

    @Test
    @Order(3)
    @DisplayName("should get user by id")
    void shouldGetUserById() {
        UserResponse created = createTestUser("john.doe@example.com");

        UserResponse found = userService.getUserById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @Order(4)
    @DisplayName("should throw UserNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        assertThatThrownBy(() -> userService.getUserById(9999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("9999");
    }

    @Test
    @Order(5)
    @DisplayName("should get all users")
    void shouldGetAllUsers() {
        createTestUser("user1@example.com");
        createTestUser("user2@example.com");

        assertThat(userService.getAllUsers()).hasSize(2);
    }

    @Test
    @Order(6)
    @DisplayName("should update user — only changed fields updated")
    void shouldUpdateUser() {
        UserResponse created = createTestUser("john.doe@example.com");

        UpdateUserRequest update = UpdateUserRequest.builder()
                .firstName("Jane")
                .build();

        UserResponse updated = userService.updateUser(created.getId(), update);

        assertThat(updated.getFirstName()).isEqualTo("Jane");
        assertThat(updated.getLastName()).isEqualTo("Doe");               // unchanged
        assertThat(updated.getEmail()).isEqualTo("john.doe@example.com"); // unchanged
    }

    @Test
    @Order(7)
    @DisplayName("should throw DuplicateEmailException when updating to taken email")
    void shouldThrowOnUpdateWithTakenEmail() {
        createTestUser("user1@example.com");
        UserResponse user2 = createTestUser("user2@example.com");

        UpdateUserRequest update = UpdateUserRequest.builder()
                .email("user1@example.com")
                .build();

        assertThatThrownBy(() -> userService.updateUser(user2.getId(), update))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    @Order(8)
    @DisplayName("should delete user successfully")
    void shouldDeleteUser() {
        UserResponse created = createTestUser("john.doe@example.com");

        userService.deleteUser(created.getId());

        assertThat(userRepository.count()).isEqualTo(0);
    }

    @Test
    @Order(9)
    @DisplayName("should throw UserNotFoundException when deleting non-existent user")
    void shouldThrowWhenDeletingNonExistent() {
        assertThatThrownBy(() -> userService.deleteUser(9999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @Order(10)
    @DisplayName("should return only active users")
    void shouldReturnOnlyActiveUsers() {
        createTestUser("user1@example.com");
        createTestUser("user2@example.com");

        assertThat(userService.getActiveUsers()).hasSize(2);
        assertThat(userService.getActiveUsers())
                .allMatch(u -> u.getStatus().name().equals("ACTIVE"));
    }

    @Test
    @Order(11)
    @DisplayName("should create order for a user")
    void shouldCreateOrder() {
        UserResponse user = createTestUser("john.doe@example.com");

        OrderResponse order = orderService.createOrder(user.getId(),
                CreateOrderRequest.builder()
                        .orderNumber("ORD-001")
                        .description("Test order")
                        .totalAmount(new BigDecimal("99.99"))
                        .build());

        assertThat(order.getId()).isNotNull();
        assertThat(order.getOrderNumber()).isEqualTo("ORD-001");
        assertThat(order.getUserId()).isEqualTo(user.getId());
        assertThat(order.getStatus().name()).isEqualTo("PENDING");
    }

    @Test
    @Order(12)
    @DisplayName("should get order by id for correct user only")
    void shouldGetOrderByIdForCorrectUser() {
        UserResponse user1 = createTestUser("user1@example.com");
        UserResponse user2 = createTestUser("user2@example.com");

        OrderResponse order = orderService.createOrder(user1.getId(),
                CreateOrderRequest.builder()
                        .orderNumber("ORD-002")
                        .totalAmount(new BigDecimal("49.99"))
                        .build());

        // user1 can fetch their own order
        assertThat(orderService.getOrderById(user1.getId(), order.getId())).isNotNull();

        // user2 cannot fetch user1's order — should throw 404
        assertThatThrownBy(() -> orderService.getOrderById(user2.getId(), order.getId()))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    @Order(13)
    @DisplayName("should cancel order successfully")
    void shouldCancelOrder() {
        UserResponse user = createTestUser("john.doe@example.com");

        OrderResponse order = orderService.createOrder(user.getId(),
                CreateOrderRequest.builder()
                        .orderNumber("ORD-003")
                        .totalAmount(new BigDecimal("149.99"))
                        .build());

        orderService.cancelOrder(user.getId(), order.getId());

        // fetch and verify cancelled — use getOrdersByUserId to verify status
        assertThat(orderService.getOrdersByUserId(user.getId()))
                .anyMatch(o -> o.getId().equals(order.getId())
                        && o.getStatus().name().equals("CANCELLED"));
    }

    @Test
    @Order(14)
    @DisplayName("should throw when creating order for non-existent user")
    void shouldThrowWhenCreatingOrderForNonExistentUser() {
        assertThatThrownBy(() -> orderService.createOrder(9999L,
                CreateOrderRequest.builder()
                        .orderNumber("ORD-999")
                        .totalAmount(new BigDecimal("9.99"))
                        .build()))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ── HELPER ────────────────────────────────────────────────────────────────

    private UserResponse createTestUser(String email) {
        return userService.createUser(CreateUserRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email(email)
                .build());
    }
}
package com.dk.jpatesting.service;

import com.dk.jpatesting.dto.request.CreateOrderRequest;
import com.dk.jpatesting.dto.response.OrderResponse;
import com.dk.jpatesting.entity.Order;
import com.dk.jpatesting.entity.OrderStatus;
import com.dk.jpatesting.entity.User;
import com.dk.jpatesting.entity.UserStatus;
import com.dk.jpatesting.exception.OrderNotFoundException;
import com.dk.jpatesting.exception.UserNotFoundException;
import com.dk.jpatesting.mapper.OrderMapper;
import com.dk.jpatesting.repository.OrderRepository;
import com.dk.jpatesting.repository.UserRepository;
import com.dk.jpatesting.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl Tests")
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private User user;
    private Order order;
    private OrderResponse orderResponse;
    private CreateOrderRequest createRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .status(UserStatus.ACTIVE)
                .build();

        order = Order.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .description("Test order")
                .totalAmount(new BigDecimal("99.99"))
                .status(OrderStatus.PENDING)
                .user(user)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .totalAmount(new BigDecimal("99.99"))
                .status(OrderStatus.PENDING)
                .userId(1L)
                .build();

        createRequest = CreateOrderRequest.builder()
                .orderNumber("ORD-001")
                .description("Test order")
                .totalAmount(new BigDecimal("99.99"))
                .build();
    }

    @Nested
    @DisplayName("createOrder()")
    class CreateOrder {

        @Test
        @DisplayName("should create order successfully")
        void shouldCreateOrderSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(orderRepository.existsByOrderNumber("ORD-001")).thenReturn(false);
            when(orderMapper.toEntity(createRequest)).thenReturn(order);
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toResponse(order)).thenReturn(orderResponse);

            OrderResponse result = orderService.createOrder(1L, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getOrderNumber()).isEqualTo("ORD-001");
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.createOrder(99L, createRequest))
                    .isInstanceOf(UserNotFoundException.class);

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw IllegalArgumentException when order number exists")
        void shouldThrowWhenOrderNumberExists() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(orderRepository.existsByOrderNumber("ORD-001")).thenReturn(true);

            assertThatThrownBy(() -> orderService.createOrder(1L, createRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ORD-001");

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("getOrderById()")
    class GetOrderById {

        @Test
        @DisplayName("should return order when found for correct user")
        void shouldReturnOrderWhenFound() {
            when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(orderResponse);

            OrderResponse result = orderService.getOrderById(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getOrderNumber()).isEqualTo("ORD-001");
        }

        @Test
        @DisplayName("should throw OrderNotFoundException when order not found or wrong user")
        void shouldThrowWhenOrderNotFoundOrWrongUser() {
            when(orderRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(1L, 99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("cancelOrder()")
    class CancelOrder {

        @Test
        @DisplayName("should cancel order successfully")
        void shouldCancelOrderSuccessfully() {
            when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);

            orderService.cancelOrder(1L, 1L);

            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepository).save(order);
        }

        @Test
        @DisplayName("should throw IllegalStateException when cancelling delivered order")
        void shouldThrowWhenCancellingDeliveredOrder() {
            order.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot cancel a delivered order");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw OrderNotFoundException when order not found or wrong user")
        void shouldThrowWhenOrderNotFound() {
            when(orderRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 99L))
                    .isInstanceOf(OrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getOrdersByUserId()")
    class GetOrdersByUserId {

        @Test
        @DisplayName("should return orders for valid user")
        void shouldReturnOrdersForValidUser() {
            when(userRepository.existsById(1L)).thenReturn(true);
            when(orderRepository.findAllByUserIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(order));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(orderResponse);

            List<OrderResponse> result = orderService.getOrdersByUserId(1L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-001");
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> orderService.getOrdersByUserId(99L))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
package com.dk.jpatesting.controller;

import com.dk.jpatesting.dto.request.CreateOrderRequest;
import com.dk.jpatesting.dto.response.OrderResponse;
import com.dk.jpatesting.entity.OrderStatus;
import com.dk.jpatesting.exception.OrderNotFoundException;
import com.dk.jpatesting.exception.UserNotFoundException;
import com.dk.jpatesting.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@DisplayName("OrderController Tests")
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderResponse = OrderResponse.builder()
                .id(1L)
                .orderNumber("ORD-001")
                .description("Test order")
                .totalAmount(new BigDecimal("99.99"))
                .status(OrderStatus.PENDING)
                .userId(1L)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /users/{userId}/orders")
    class CreateOrder {

        @Test
        @DisplayName("should return 201 when order created")
        void shouldReturn201WhenCreated() throws Exception {
            when(orderService.createOrder(eq(1L), any())).thenReturn(orderResponse);

            CreateOrderRequest request = CreateOrderRequest.builder()
                    .orderNumber("ORD-001")
                    .totalAmount(new BigDecimal("99.99"))
                    .build();

            mockMvc.perform(post("/users/1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderNumber").value("ORD-001"))
                    .andExpect(jsonPath("$.status").value("PENDING"));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(orderService.createOrder(eq(99L), any()))
                    .thenThrow(new UserNotFoundException(99L));

            CreateOrderRequest request = CreateOrderRequest.builder()
                    .orderNumber("ORD-001")
                    .totalAmount(new BigDecimal("99.99"))
                    .build();

            mockMvc.perform(post("/users/99/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("User Not Found"));
        }

        @Test
        @DisplayName("should return 400 when request invalid")
        void shouldReturn400WhenInvalid() throws Exception {
            CreateOrderRequest invalid = CreateOrderRequest.builder()
                    .orderNumber("")              // blank
                    .totalAmount(new BigDecimal("-1")) // negative
                    .build();

            mockMvc.perform(post("/users/1/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Failed"));
        }
    }

    @Nested
    @DisplayName("GET /users/{userId}/orders")
    class GetOrders {

        @Test
        @DisplayName("should return all orders for user")
        void shouldReturnAllOrders() throws Exception {
            when(orderService.getOrdersByUserId(1L)).thenReturn(List.of(orderResponse));

            mockMvc.perform(get("/users/1/orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].orderNumber").value("ORD-001"));
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(orderService.getOrdersByUserId(99L))
                    .thenThrow(new UserNotFoundException(99L));

            mockMvc.perform(get("/users/99/orders"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /users/{userId}/orders/{orderId}")
    class GetOrderById {

        @Test
        @DisplayName("should return order when found")
        void shouldReturnOrder() throws Exception {
            when(orderService.getOrderById(1L, 1L)).thenReturn(orderResponse);

            mockMvc.perform(get("/users/1/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @DisplayName("should return 404 when order not found or wrong user")
        void shouldReturn404WhenNotFound() throws Exception {
            when(orderService.getOrderById(1L, 99L))
                    .thenThrow(new OrderNotFoundException(99L));

            mockMvc.perform(get("/users/1/orders/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Order Not Found"));
        }
    }

    @Nested
    @DisplayName("PATCH /users/{userId}/orders/{orderId}/cancel")
    class CancelOrder {

        @Test
        @DisplayName("should return 204 when cancelled")
        void shouldReturn204WhenCancelled() throws Exception {
            mockMvc.perform(patch("/users/1/orders/1/cancel"))
                    .andExpect(status().isNoContent());

            verify(orderService).cancelOrder(1L, 1L);
        }

        @Test
        @DisplayName("should return 404 when order not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new OrderNotFoundException(99L))
                    .when(orderService).cancelOrder(1L, 99L);

            mockMvc.perform(patch("/users/1/orders/99/cancel"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 409 when order already delivered")
        void shouldReturn409WhenDelivered() throws Exception {
            doThrow(new IllegalStateException("Cannot cancel a delivered order"))
                    .when(orderService).cancelOrder(1L, 1L);

            mockMvc.perform(patch("/users/1/orders/1/cancel"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Invalid Operation"));
        }
    }
}
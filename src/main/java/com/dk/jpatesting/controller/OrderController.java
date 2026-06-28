package com.dk.jpatesting.controller;

import com.dk.jpatesting.dto.request.CreateOrderRequest;
import com.dk.jpatesting.dto.response.OrderResponse;
import com.dk.jpatesting.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("users/{userId}/orders")
@RequiredArgsConstructor
@Tag(name = "Order management", description = "APIs for managing orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create an order for user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Order created Successfully"),
            @ApiResponse(responseCode = "400", description = "Validation Error"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Duplicate order number")
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Valid @RequestBody CreateOrderRequest request) {

        log.debug("POST /users/{}/orders", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(userId, request));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Order found"),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Order ID") @PathVariable Long orderId) {

        log.debug("GET /users/{}/orders/{}", userId, orderId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.getOrderById(userId, orderId));
    }

    @GetMapping
    @Operation(summary = "Get all orders for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")

    })
    public ResponseEntity<List<OrderResponse>> getOrdersByUser(
            @Parameter(description = "user ID") @PathVariable Long userId) {

        log.debug("GET /users/{}/orders", userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(orderService.getOrdersByUserId(userId));
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Cancel an order")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "409", description = "Cannot cancel a delivered order")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "User ID") @PathVariable Long userId,
            @Parameter(description = "Order ID") @PathVariable Long orderId) {

        log.debug("PATCH /users/{}/orders/{}", userId, orderId);
        orderService.cancelOrder(userId, orderId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}

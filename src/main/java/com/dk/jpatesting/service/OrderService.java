package com.dk.jpatesting.service;

import com.dk.jpatesting.dto.request.CreateOrderRequest;
import com.dk.jpatesting.dto.response.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(Long userId, CreateOrderRequest request);

    OrderResponse getOrderById(Long userId, Long orderId);

    List<OrderResponse> getOrdersByUserId(Long userId);

    void cancelOrder(Long userId, Long orderId);
}

package com.dk.jpatesting.service.impl;

import com.dk.jpatesting.dto.request.CreateOrderRequest;
import com.dk.jpatesting.dto.response.OrderResponse;
import com.dk.jpatesting.entity.Order;
import com.dk.jpatesting.entity.OrderStatus;
import com.dk.jpatesting.entity.User;
import com.dk.jpatesting.exception.OrderNotFoundException;
import com.dk.jpatesting.exception.UserNotFoundException;
import com.dk.jpatesting.mapper.OrderMapper;
import com.dk.jpatesting.repository.OrderRepository;
import com.dk.jpatesting.repository.UserRepository;
import com.dk.jpatesting.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest request) {
        log.debug("Creating order for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (orderRepository.existsByOrderNumber(request.getOrderNumber())) {
            throw new IllegalArgumentException("Order number already exists:" + request.getOrderNumber());
        }

        Order order = orderMapper.toEntity(request);
        order.setUser(user);

        Order saved = orderRepository.save(order);
        log.info("Order created for userId: {}, orderId: {}", userId, saved.getId());

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long userId, Long orderId) {
        log.debug("Fetching order with id: {}", orderId);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Long userId) {
        log.debug("Fetching orders for userId: {}", userId);
        if (!userRepository.existsById(userId))
            throw new UserNotFoundException(userId);

        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long orderId) {
        log.debug("Canceling order with id: {}", orderId);

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.DELIVERED)
            throw new IllegalStateException("Cannot cancel a delivered order");

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order cancelled for userId: {}, orderId: {}", order.getUser().getId(), order.getId());
    }
}

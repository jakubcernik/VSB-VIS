package com.example.watchmanagement.service;

import com.example.watchmanagement.model.Order;
import com.example.watchmanagement.model.User;
import com.example.watchmanagement.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public Optional<Order> findByUserAndStatus(User user, String status) {
        return orderRepository.findByUserAndStatus(user, status);
    }

    public List<Order> findByUserAndStatusNot(User user, String status) {
        return orderRepository.findByUserAndStatusNot(user, status);
    }

    public List<Order> findByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public void updateTotalPrice(Order order) {
        double total = order.getItems().stream()
                .mapToDouble(item -> item.getQuantity() * item.getWatch().getPrice())
                .sum();
        order.setTotalPrice(total);
        orderRepository.save(order);
    }
}

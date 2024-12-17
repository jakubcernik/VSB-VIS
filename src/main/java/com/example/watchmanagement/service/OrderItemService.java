package com.example.watchmanagement.service;

import com.example.watchmanagement.model.OrderItem;
import com.example.watchmanagement.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderItemService {
    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    public OrderItem save(OrderItem item) {
        orderItemRepository.save(item);
        return item;
    }

    public void delete(OrderItem item) {
        orderItemRepository.delete(item);
    }
}

package com.example.watchmanagement.repository;

import com.example.watchmanagement.model.OrderItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(OrderItem orderItem) {
        if (orderItem.getId() == null) {
            // If ID is null -> new data (INSERT)
            String sql = "INSERT INTO order_item (order_id, watch_id, quantity) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, orderItem.getOrder().getId(), orderItem.getWatch().getId(), orderItem.getQuantity());

            // Last inserted ID
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            orderItem.setId(id);
        } else {
            // If ID exists -> (UPDATE)
            String sql = "UPDATE order_item SET quantity = ? WHERE id = ?";
            jdbcTemplate.update(sql, orderItem.getQuantity(), orderItem.getId());
        }
    }

    public void delete(OrderItem orderItem) {
        String sql = "DELETE FROM order_item WHERE id = ?";
        jdbcTemplate.update(sql, orderItem.getId());
    }
}
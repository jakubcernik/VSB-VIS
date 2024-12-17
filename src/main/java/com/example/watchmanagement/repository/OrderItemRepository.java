package com.example.watchmanagement.repository;

import com.example.watchmanagement.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderItemRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderItemRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(OrderItem orderItem) {
        if (orderItem.getId() == null) {
            // Pokud ID je null -> jedná se o nový záznam (INSERT)
            String sql = "INSERT INTO order_item (order_id, watch_id, quantity) VALUES (?, ?, ?)";
            jdbcTemplate.update(sql, orderItem.getOrder().getId(), orderItem.getWatch().getId(), orderItem.getQuantity());

            // Získáme poslední vložené ID
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            orderItem.setId(id);
        } else {
            // Pokud ID existuje -> jedná se o aktualizaci (UPDATE)
            String sql = "UPDATE order_item SET quantity = ? WHERE id = ?";
            jdbcTemplate.update(sql, orderItem.getQuantity(), orderItem.getId());
        }
    }


    public Optional<OrderItem> findById(Long id) {
        String sql = "SELECT * FROM order_item WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), id)
                .stream()
                .findFirst();
    }

    public List<OrderItem> findByOrderId(Long orderId) {
        String sql = "SELECT * FROM order_item WHERE order_id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(OrderItem.class), orderId);
    }

    public void update(OrderItem orderItem) {
        String sql = "UPDATE order_item SET quantity = ? WHERE id = ?";
        jdbcTemplate.update(sql, orderItem.getQuantity(), orderItem.getId());
    }

    public void delete(OrderItem orderItem) {
        String sql = "DELETE FROM order_item WHERE id = ?";
        jdbcTemplate.update(sql, orderItem.getId());
    }

    public void deleteByOrderId(Long orderId) {
        String sql = "DELETE FROM order_item WHERE order_id = ?";
        jdbcTemplate.update(sql, orderId);
    }
}
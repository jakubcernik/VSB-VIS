package com.example.watchmanagement.repository;

import com.example.watchmanagement.model.Order;
import com.example.watchmanagement.model.OrderItem;
import com.example.watchmanagement.model.User;

import com.example.watchmanagement.model.Watch;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private final JdbcTemplate jdbcTemplate;

    public OrderRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public Optional<Order> findByUserAndStatus(User user, String status) {
        String sql = "SELECT * FROM orders WHERE user_id = ? AND status = ?";
        List<Order> orders = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), user.getId(), status);

        if (orders.isEmpty()) {
            return Optional.empty();
        }

        Order order = orders.get(0);
        List<OrderItem> items = loadOrderItems(order.getId());
        order.setItems(items);
        order.setUser(user);

        return Optional.of(order);
    }

    private List<OrderItem> loadOrderItems(Long orderId) {
        String sql = "SELECT * FROM order_item WHERE order_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OrderItem item = new OrderItem();
            item.setId(rs.getLong("id"));
            item.setQuantity(rs.getInt("quantity"));

            Long watchId = rs.getLong("watch_id");
            Watch watch = loadWatch(watchId);
            item.setWatch(watch);

            Order order = new Order();
            order.setId(orderId);
            item.setOrder(order);

            return item;
        }, orderId);
    }


    private Watch loadWatch(Long watchId) {
        String sql = "SELECT * FROM watch WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            Watch watch = new Watch();
            watch.setId(rs.getLong("id"));
            watch.setName(rs.getString("name"));
            watch.setPrice(rs.getInt("price"));
            watch.setStock(rs.getInt("stock"));
            return watch;
        }, watchId);
    }

    public Optional<Order> findById(Long id) {
        String sql = "SELECT * FROM orders WHERE id = ?";
        List<Order> orders = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), id);

        if (orders.isEmpty()) {
            return Optional.empty();
        }

        Order order = orders.get(0);
        Long userId = jdbcTemplate.queryForObject("SELECT user_id FROM orders WHERE id = ?", Long.class, id);
        User user = new User();
        user.setId(userId);
        order.setUser(user);

        List<OrderItem> items = loadOrderItems(id);
        order.setItems(items);

        return Optional.of(order);
    }

    public Order save(Order order) {
        if (order.getUser() == null || order.getUser().getId() == null) {
            System.err.println("Save Error: Order user is null or user ID is null. Order details: " + order);
            throw new IllegalArgumentException("Order must have a valid user before saving.");
        }
        if (order.getId() == null) {
            System.out.println("Inserting new order for user: " + order.getUser());
            String sql = "INSERT INTO orders (user_id, status, total_price, customer_name, customer_address, payment_method) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, order.getUser().getId(), order.getStatus(), order.getTotalPrice(),
                    order.getCustomerName(), order.getCustomerAddress(), order.getPaymentMethod());
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            order.setId(id);
        } else {
            System.out.println("Updating existing order: " + order);
            String sql = "UPDATE orders SET user_id = ?, status = ?, total_price = ?, customer_name = ?, customer_address = ?, payment_method = ? WHERE id = ?";
            jdbcTemplate.update(sql, order.getUser().getId(), order.getStatus(), order.getTotalPrice(),
                    order.getCustomerName(), order.getCustomerAddress(), order.getPaymentMethod(), order.getId());
        }
        return order;
    }



    public List<Order> findByUserAndStatusNot(User user, String status) {
        String sql = "SELECT * FROM orders WHERE user_id = ? AND status != ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), user.getId(), status);
    }

    public List<Order> findByStatus(String status) {
        String sql = "SELECT * FROM orders WHERE status = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Order.class), status);
    }
}


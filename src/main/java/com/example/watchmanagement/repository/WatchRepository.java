package com.example.watchmanagement.repository;

import com.example.watchmanagement.model.Watch;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class WatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public WatchRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Watch> findAll() {
        String sql = "SELECT * FROM watch";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Watch.class));
    }

    public Optional<Watch> findById(Long id) {
        String sql = "SELECT * FROM watch WHERE id = ?";
        return jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(Watch.class), id)
                .stream()
                .findFirst();
    }

    public void save(Watch watch) {
        if (watch.getDescription() == null) {
            watch.setDescription("");
        }

        if (watch.getId() == null) {
            // New data
            String sql = "INSERT INTO watch (name, price, stock, description, image) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, watch.getName(), watch.getPrice(), watch.getStock(), watch.getDescription(), watch.getImage());

            // Last ID
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            watch.setId(id);
        } else {
            // UPDATE
            String sql = "UPDATE watch SET name = ?, price = ?, stock = ?, description = ?, image = ? WHERE id = ?";
            jdbcTemplate.update(sql, watch.getName(), watch.getPrice(), watch.getStock(), watch.getDescription(), watch.getImage(), watch.getId());
        }
    }

    public void updateStock(Long id, int newStock) {
        String sql = "UPDATE watch SET stock = ? WHERE id = ?";
        jdbcTemplate.update(sql, newStock, id);
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM watch WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }
}
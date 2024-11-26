package com.example.watchmanagement.repository;

import com.example.watchmanagement.model.Order;
import com.example.watchmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByUserAndStatus(User user, String status);
    List<Order> findByUser(User user);

    List<Order> findByUserAndStatusNot(User loggedInUser, String pending);

    List<Order> findByStatus(String created);
}

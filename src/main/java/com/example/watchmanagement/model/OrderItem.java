package com.example.watchmanagement.model;

import jakarta.persistence.*;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Order order;

    @ManyToOne
    private Watch watch;

    private int quantity;

    // Gettery a settery

    public OrderItem() {}
}

package com.example.watchmanagement.model;

import jakarta.persistence.*;

@Entity
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(length = 1000)
    private String description;

    private double price;

    private String image;

    // Gettery a settery

    public Watch() {}
}

package com.example.watchmanagement.model;

import jakarta.persistence.*;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String role;

    // Gettery a settery

    // Konstruktor bez parametrů (nutný pro JPA)
    public User() {}

    // Další konstruktory, pokud je potřeba
}

package com.example.watchmanagement.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
public class Watch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Název je povinný")
    private String name;

    @NotBlank(message = "Popis je povinný")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Cena je povinná")
    @Min(value = 0, message = "Cena musí být kladná")
    private double price;

    @NotBlank(message = "URL obrázku je povinná")
    private String image;

    // Konstruktor bez parametrů
    public Watch() {}

    // Konstruktor s parametry
    public Watch(String name, String description, double price, String image) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.image = image;
    }

    // Gettery a settery

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
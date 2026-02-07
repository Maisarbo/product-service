package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vehicle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private VehicleBrand brand;

    @Column(nullable = false)
    private String name; // Gol, Corolla, Série 3
}



package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "vehicle_model",
        uniqueConstraints = @UniqueConstraint(columnNames = {"vehicle_id", "name"}))
@Getter
@Setter
@NoArgsConstructor
public class VehicleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToMany(mappedBy = "applications")
    private List<ProductCodeClassification> classifications = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "model_year",
            joinColumns = @JoinColumn(name = "model_id"),
            inverseJoinColumns = @JoinColumn(name = "year_id")
    )
    private List<YearModel> years = new ArrayList<>();


    /* Construtores de domínio */

    public VehicleModel(String name, Vehicle vehicle) {
        this.name = name;
        this.vehicle = vehicle;
    }

    public VehicleModel(String name, Vehicle vehicle, List<YearModel> years) {
        this.name = name;
        this.vehicle = vehicle;
        this.years = years != null ? years : new ArrayList<>();
    }



    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VehicleModel that = (VehicleModel) o;
        return Objects.equals(name, that.name) && Objects.equals(vehicle, that.vehicle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, vehicle);
    }
}

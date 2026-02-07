package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "vehicle_brand")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VehicleBrand {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Ex.: Chevrolet, Volkswagen

    private String abbreviation;

    @OneToMany(mappedBy = "brand")
    private List<VehicleModel> models = new ArrayList<>();



    public VehicleBrand(Object o, String montadora) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VehicleBrand that = (VehicleBrand) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}

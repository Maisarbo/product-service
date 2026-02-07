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
@Table(name = "year_model")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YearModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "year_value", nullable = false)
    private Integer year;

    @ManyToMany(mappedBy = "years")
    private List<VehicleModel> models = new ArrayList<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof YearModel)) return false;
        YearModel that = (YearModel) o;
        return Objects.equals(year, that.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year);
    }
}

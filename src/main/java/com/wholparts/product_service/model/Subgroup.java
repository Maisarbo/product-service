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
@Table(name = "subgroup")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Subgroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private ProductGroup group;

    @OneToMany(mappedBy = "subgroup")
    private List<ProductCodeClassification> classifications;

    public Subgroup(Object o, String subgrupo, ProductGroup group) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Subgroup subgroup = (Subgroup) o;
        return Objects.equals(name, subgroup.name) && Objects.equals(group, subgroup.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, group);
    }
}

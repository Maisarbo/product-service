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
@Table(name = "product_group")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subgroup> subgroupss = new ArrayList<>();

    public ProductGroup(Object o, String grupo) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductGroup that = (ProductGroup) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }
}

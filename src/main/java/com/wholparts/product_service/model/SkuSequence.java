package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Entity
@Table(name = "sku_sequence")
@Getter
@Setter
public class SkuSequence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prefix; // ex: VW2023M
    private int lastNumber; // último número usado

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SkuSequence that = (SkuSequence) o;
        return Objects.equals(prefix, that.prefix);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(prefix);
    }
}

package com.wholparts.product_service.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "product_barcode")
public class ProductBarcode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String barcode; // Código de barras

    // Rastreabilidade — código de barras pode estar ligado a uma entrada específica, se desejar
    private String batch; // opcional

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductBarcode that = (ProductBarcode) o;
        return Objects.equals(barcode, that.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(barcode);
    }
}

package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.*;


@Entity
@Table(
        name = "product_code_classification",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_classification_manufacturer_subgroup",
                        columnNames = {"subgroup_id", "brand"}
                )
        }
)
@Builder
@Getter
@Setter
@AllArgsConstructor

public class ProductCodeClassification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Marca do fabricante
     * Ex: BOSCH, TRW
     */
    @Column(nullable = false)
    private String brand;

    /**
     * Códigos vinculados à classification
     */
    @OneToMany(
            mappedBy = "classification",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ProductCode> productCodes = new HashSet<>();

    /**
     * Grupo técnico da peça
     * Ex: Freio > Pastilha
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Subgroup subgroup;

    /**
     * Aplicações veiculares
     */
    @ManyToMany
    @JoinTable(
            name = "product_code_classification_application",
            joinColumns = @JoinColumn(name = "classification_id"),
            inverseJoinColumns = @JoinColumn(name = "vehicle_model_id")
    )
    private Set<VehicleModel> applications = new HashSet<>();

    /**
     * Similaridades técnicas (TRW ↔ BOSCH)
     */
    @ManyToMany
    @JoinTable(
            name = "classification_similar",
            joinColumns = @JoinColumn(name = "classification_id"),
            inverseJoinColumns = @JoinColumn(name = "similar_id")
    )
    private Set<ProductCodeClassification> similars = new HashSet<>();

    public ProductCodeClassification() {

    }


    /* =========================
       Helpers de domínio
       ========================= */

    public void addProductCode(ProductCode code) {
        productCodes.add(code);
        code.setClassification(this);
    }

    public void addApplication(VehicleModel model) {
        applications.add(model);
    }


    public void addSimilar(ProductCodeClassification similar) {
        this.similars.add(similar);
        similar.getSimilars().add(this);
    }



    public ProductCode getManufacturerCode() {
        return productCodes.stream()
                .filter(c -> c.getCodeType() == CodeType.MANUFACTURER)
                .findFirst()
                .orElseThrow(() ->
                        new IllegalStateException("Manufacturer code não encontrado"));
    }

    /* =========================
       equals & hashCode
       ========================= */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProductCodeClassification other)) return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProductCodeClassification{" +
                "brand='" + brand + '\'' +
                ", productCodes=" + productCodes +
                ", subgroup=" + subgroup +
                ", applications=" + applications +
                '}';
    }
}

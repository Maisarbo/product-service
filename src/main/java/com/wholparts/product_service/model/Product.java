package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product {

    @Builder.Default
    private Boolean active = true;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dados principais do produto
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    private String ncm;
    private String cest;
    // Informações fiscais e regulatórias
    private String gtin; // Código de barras principal (opcional se for GTIN)
    private String ncmDescription;
    //private String manufacturerCode;
    @Builder.Default
    private Boolean autoCreated = false;



    // Relacionamentos
    @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductVariant> variants = new ArrayList<>();



    // Um produto pode ter vários códigos de barras (um para cada lote/entrada, rastreabilidade opcional)

   /* @Builder.Default
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductBarcode> barcodes = new ArrayList<>();*/


    // Informações adicionais, como dimensões, peso, etc., se desejar
    private String additionalInfo;

    // Timestamps
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public Product(long l, String produtoX) {
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public void setNormalizedName(String normalizedName) {
    }

    public void setAutoCreated(boolean b) {

    }

    // Getters, Setters, Equals e HashCode
}

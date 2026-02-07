package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    private Product product; // a peça

    private Boolean active = true;

    @OneToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "sku_code_id")
    private ProductCode sku; // CodeType.INTERNAL

    @OneToOne(optional = false)
    private ProductCodeClassification classification;
}

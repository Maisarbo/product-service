package com.wholparts.product_service.dto;


import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariantViewDTO {

    private Long variantId;
    private Boolean active;

    /* =========================
       Produto base
       ========================= */
    private Long productId;
    private String productName;
    private String ncm;

    /* =========================
       Classificação técnica
       ========================= */
    private String subgroup;
    private List<String> vehicleModels;

    /* =========================
       Códigos
       ========================= */
    // Código técnico principal
    private String manufacturerCode;

    // Códigos logísticos / comerciais
    private List<String> distributorCodes;

    // Código interno (SKU)
    private String sku;

    /* =========================
       Metadados opcionais
       ========================= */
    private String brand;
}

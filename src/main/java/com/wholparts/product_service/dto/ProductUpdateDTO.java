package com.wholparts.product_service.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class ProductUpdateDTO {
    private String name;
    private String description;
    private String ncm;
    private String gtin;
    private String ncmDescription;
    private String cest;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
}

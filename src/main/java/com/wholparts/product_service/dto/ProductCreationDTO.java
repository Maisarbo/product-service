package com.wholparts.product_service.dto;

import com.wholparts.product_service.model.Unit;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreationDTO {
    private String name;
    private String description;
    private String manufacturer;
    @Enumerated(EnumType.STRING)
    private Unit unit;
    private String gtin;
    private String ncm;
    private String distributorCodeValue;
    private String cest;
    private String ncmDescription;


}

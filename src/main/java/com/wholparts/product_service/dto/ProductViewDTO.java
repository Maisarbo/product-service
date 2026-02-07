package com.wholparts.product_service.dto;

import com.wholparts.product_service.model.Vehicle;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductViewDTO {
    private Long id;
    private String name;
    private String description;
    private String manufacturer;
    private String manufacturerCode;
    private String unit;
    private String ncm;
    private String ncmDescription;
    private String gtin;
    private String cest;
    private BigDecimal costPrice;
    private BigDecimal salePrice;
    private Boolean active;


    private String group;
    private String subgroup;
    private List<Vehicle> veiculos;
}

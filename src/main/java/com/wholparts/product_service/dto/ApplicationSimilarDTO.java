package com.wholparts.product_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApplicationSimilarDTO {

    private String brand;

    /**
     * Código MANUFACTURER do similar
     * (chave técnica)
     */
    private String manufacturerCode;

    /**
     * Outros códigos opcionais
     */
    private List<ProductCodeDTO> codes;
}


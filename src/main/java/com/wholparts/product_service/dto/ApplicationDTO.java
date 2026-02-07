package com.wholparts.product_service.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApplicationDTO {

    /**
     * Marca principal da classificação
     * Ex: BOSCH
     */
    private String brand;

    /**
     * Grupo técnico
     * Ex: Motor
     */
    private String group;

    /**
     * Subgrupo técnico
     * Ex: Correia Dentada
     */
    private String subgroup;

    /**
     * Códigos da classificação
     * Ex: MANUFACTURER, INTERNAL, DISTRIBUTOR
     */
    private List<ProductCodeDTO> codes;

    /**
     * Aplicações veiculares
     */
    private List<VehicleModelCreateDTO> veiculos;

    /**
     * SIMILARES TÉCNICOS
     * Ex: TRW, COBREQ, BOSCH
     *
     * Cada similar é outra classification
     */
    private List<ApplicationSimilarDTO> similarManufacturerCodes;
}

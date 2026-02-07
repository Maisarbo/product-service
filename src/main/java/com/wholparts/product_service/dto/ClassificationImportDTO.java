package com.wholparts.product_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class ClassificationImportDTO {

    /**
     * Marca do FABRICANTE da peça
     * Ex: BOSCH, TRW, COBREQ
     */
    private String brand;

    /**
     * Grupo técnico
     * Ex: Freios
     */
    private String group;

    /**
     * Subgrupo técnico
     * Ex: Pastilha de Freio
     */
    private String subgroup;

    /**
     * Todos os códigos equivalentes da peça
     * (fabricante, distribuidor, interno, etc)
     */
    private List<CodesDTO> codes;

    /**
     * Aplicações veiculares
     */
    private List<ApplicationDTO> applications;
}

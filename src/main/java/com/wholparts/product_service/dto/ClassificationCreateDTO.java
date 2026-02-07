package com.wholparts.product_service.dto;

import com.wholparts.product_service.model.Vehicle;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ClassificationCreateDTO {

    private String description;

    private String productGroup;
    private String subGroup;
    private String brand;

    private List<CodesDTO> codes;
    private List<Vehicle> veiculos;



    // getters e setters
}

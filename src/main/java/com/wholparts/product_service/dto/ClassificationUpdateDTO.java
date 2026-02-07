package com.wholparts.product_service.dto;

import com.wholparts.product_service.model.ProductCode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ClassificationUpdateDTO {

    private Long id;
    private String brand;
    private String group;
    private String subgroup;
    private List<ProductCodeDTO> codes;
    private List<VehicleModelCreateDTO> vehicles;


}

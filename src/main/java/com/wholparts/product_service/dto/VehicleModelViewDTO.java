package com.wholparts.product_service.dto;

import com.wholparts.product_service.model.VehicleModel;
import com.wholparts.product_service.model.YearModel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class VehicleModelViewDTO {

    private Long id;
    private String brand;
    private String vehicle;
    private String model;
    private List<Integer> years;

    public static VehicleModelViewDTO fromEntity(VehicleModel model) {
        return new VehicleModelViewDTO(
                model.getId(),
                model.getVehicle().getBrand().getName(),
                model.getVehicle().getName(),
                model.getName(),
                model.getYears()
                        .stream()
                        .map(YearModel::getYear)
                        .toList()
        );
    }
}


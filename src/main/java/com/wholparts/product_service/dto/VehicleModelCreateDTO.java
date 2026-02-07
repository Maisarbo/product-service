package com.wholparts.product_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VehicleModelCreateDTO {

    @NotBlank
    private String brand;     // Chevrolet

    @NotBlank
    private String vehicle;   // Onix

    @NotBlank
    private String model;     // 1.0 Turbo / LTZ / Flex

    @NotEmpty
    private List<Integer> years; // [2019, 2020, 2021]
}

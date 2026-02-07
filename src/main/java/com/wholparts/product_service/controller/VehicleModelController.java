package com.wholparts.product_service.controller;

import com.wholparts.product_service.dto.VehicleModelCreateDTO;
import com.wholparts.product_service.dto.VehicleModelViewDTO;
import com.wholparts.product_service.model.VehicleModel;
import com.wholparts.product_service.service.VehicleModelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehicle-models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService service;

    @PostMapping
    public ResponseEntity<VehicleModelViewDTO> create(
            @RequestBody @Valid VehicleModelCreateDTO dto
    ) {
        VehicleModel model = service.create(dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(VehicleModelViewDTO.fromEntity(model));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleModelViewDTO> findById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                VehicleModelViewDTO.fromEntity(
                        service.findById(id)
                )
        );
    }
}

package com.wholparts.product_service.controller;

import com.wholparts.product_service.dto.ClassificationCreateDTO;
import com.wholparts.product_service.dto.ClassificationUpdateDTO;
import com.wholparts.product_service.dto.ProductCodeClassificationViewDTO;
import com.wholparts.product_service.model.ProductCodeClassification;
import com.wholparts.product_service.service.ClassificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classifications")
@RequiredArgsConstructor
public class ProductCodeClassificationController {

    private final ClassificationService classificationService;


    @PostMapping
    public ResponseEntity<ProductCodeClassificationViewDTO> create(
            @RequestBody @Valid ClassificationCreateDTO dto
    ) {
        ProductCodeClassification classification =
                classificationService.createManual(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductCodeClassificationViewDTO.fromEntity(classification));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductCodeClassificationViewDTO> update(
            @PathVariable Long id,
            @RequestBody @Valid ClassificationUpdateDTO dto
    ) {
        ProductCodeClassification classification =
                classificationService.update(dto);

        return ResponseEntity.ok(
                ProductCodeClassificationViewDTO.fromEntity(classification)
        );
    }


}


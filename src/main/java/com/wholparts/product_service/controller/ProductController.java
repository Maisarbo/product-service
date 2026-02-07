package com.wholparts.product_service.controller;

import com.wholparts.product_service.dto.ProductCreationDTO;
import com.wholparts.product_service.dto.ProductUpdateDTO;
import com.wholparts.product_service.dto.ProductViewDTO;
import com.wholparts.product_service.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductViewDTO> create(@RequestBody ProductCreationDTO dto) {
        ProductViewDTO created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ProductViewDTO findById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping
    public List<ProductViewDTO> findAll() {
        return productService.findAll();
    }

    @GetMapping("/active")
    public List<ProductViewDTO> findAllActive() {
        return productService.findAllActive();
    }

    @GetMapping("/auto-created")
    public List<ProductViewDTO> findAutoCreated() {
        return productService.findAutoCreated();
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductViewDTO> update(
            @PathVariable Long id,
            @RequestBody ProductUpdateDTO dto
    ) {
        ProductViewDTO updated = productService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

}

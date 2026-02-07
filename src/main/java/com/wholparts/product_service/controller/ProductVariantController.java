package com.wholparts.product_service.controller;

import com.wholparts.product_service.dto.ProductCreationDTO;
import com.wholparts.product_service.dto.ProductVariantViewDTO;
import com.wholparts.product_service.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping
    public ProductVariantViewDTO create(@RequestBody ProductCreationDTO dto) {
        return productVariantService.create(dto);
    }

    @GetMapping("/{id}")
    public ProductVariantViewDTO findById(@PathVariable Long id) {
        return productVariantService.findById(id);
    }

    @GetMapping
    public List<ProductVariantViewDTO> findAll() {
        return productVariantService.findAll();
    }

    @GetMapping("/sku/{sku}")
    public ProductVariantViewDTO findBySku(@PathVariable String sku) {
        return productVariantService.findBySku(sku);
    }

    @GetMapping("/code/{code}")
    public List<ProductVariantViewDTO> findByAnyCode(@PathVariable String code) {
        return productVariantService.findByAnyCode(code);
    }

    @GetMapping("/product/{productId}")
    public List<ProductVariantViewDTO> findByProduct(
            @PathVariable Long productId
    ) {
        return productVariantService.findByProduct(productId);
    }

    @GetMapping("/classification/{classificationId}")
    public List<ProductVariantViewDTO> findByClassification(
            @PathVariable Long classificationId
    ) {
        return productVariantService.findByClassification(classificationId);
    }

    @PatchMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id) {
        productVariantService.desactivate(id);
    }

    @PatchMapping("/{id}/activate")
    public void activate(@PathVariable Long id) {
        productVariantService.activate(id);
    }
}


package com.wholparts.product_service.controller;

import com.wholparts.product_service.dto.ProductVariantViewDTO;
import com.wholparts.product_service.model.CodeType;
import com.wholparts.product_service.model.ProductCode;
import com.wholparts.product_service.service.ProductCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/codes")
@RequiredArgsConstructor
public class ProductCodeController {

    private final ProductCodeService productCodeService;

    @GetMapping("/{code}")
    public ProductVariantViewDTO findVariantByAnyCode(
            @PathVariable String code
    ) {
        return productCodeService.findVariantByAnyCode(code);
    }

    @GetMapping("/classification/{classificationId}")
    public List<String> findCodesByClassification(
            @PathVariable Long classificationId
    ) {
        return productCodeService.findCodesByClassification(classificationId)
                .stream()
                .map(ProductCode::getCode)
                .toList();
    }

    @GetMapping("/classification/{classificationId}/type/{type}")
    public List<String> findCodesByType(
            @PathVariable Long classificationId,
            @PathVariable String type
    ) {
        CodeType codeType = CodeType.valueOf(type.toUpperCase());
        return productCodeService.findCodesByType(classificationId, codeType);
    }

}

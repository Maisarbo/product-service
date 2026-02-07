package com.wholparts.product_service.service;

import com.wholparts.product_service.dto.ProductVariantViewDTO;
import com.wholparts.product_service.model.*;
import com.wholparts.product_service.repository.ProductCodeRepository;
import com.wholparts.product_service.repository.ProductVariantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCodeService {


    private final ProductCodeRepository productCodeRepository;
    private final ProductVariantRepository productVariantRepository;

    /* 🔹 Busca qualquer código */
    @Transactional(readOnly = true)
    public Object findCode(String code) {
        return productCodeRepository
                .findByCode(code)
                .orElseThrow(() ->
                        new RuntimeException("Código não encontrado: " + code)
                );
    }

    /* 🔹 Todos os códigos da classificação */
    @Transactional(readOnly = true)
    public List<ProductCode> findCodesByClassification(Long classificationId) {
        return productCodeRepository
                .findAllByClassificationId(classificationId);
    }

    /* 🔹 Códigos filtrados por tipo */
    @Transactional(readOnly = true)
    public List<String> findCodesByType(
            Long classificationId,
            CodeType codeType
    ) {
        return productCodeRepository
                .findAllByClassificationId(classificationId)
                .stream()
                .filter(pc -> pc.getCodeType() == codeType)
                .map(ProductCode::getCode)
                .toList();
    }

    /* 🔹 Verifica se código existe */
    @Transactional(readOnly = true)
    public boolean codeExists(String code) {
        return productCodeRepository.existsByCode(code);
    }

    /* 🔹 Criação de código */
    @Transactional
    public ProductCode createCode(
            String code,
            CodeType codeType,
            ProductCodeClassification classification
    ) {
        if (productCodeRepository.existsByCode(code)) {
            throw new RuntimeException("Código já cadastrado: " + code);
        }

        ProductCode productCode = new ProductCode(
                code,
                codeType,
                classification
        );

        return productCodeRepository.save(productCode);
    }

    /* 🔹 Exclusão */
    @Transactional
    public void deleteCode(Long codeId) {
        ProductCode code = productCodeRepository
                .findById(codeId)
                .orElseThrow(() ->
                        new RuntimeException("Código não encontrado")
                );

        productCodeRepository.delete(code);
    }

    @Transactional(readOnly = true)
    public ProductVariantViewDTO findVariantByAnyCode(String code) {

        ProductCode productCode = productCodeRepository
                .findByCode(code)
                .orElseThrow(() ->
                        new EntityNotFoundException("Código não encontrado: " + code)
                );

        ProductVariant variant = productVariantRepository
                .findByClassification(productCode.getClassification())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Variant não encontrada para a classificação: "
                                        + productCode.getClassification().getId()
                        )
                );

        return toViewDTO(variant);
    }
    private ProductVariantViewDTO toViewDTO(ProductVariant variant) {

        ProductVariantViewDTO dto = new ProductVariantViewDTO();

    /* =========================
       Variant
       ========================= */
        dto.setVariantId(variant.getId());
        dto.setActive(variant.getActive());

    /* =========================
       Produto base
       ========================= */
        Product product = variant.getProduct();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());
        dto.setNcm(product.getNcm());

    /* =========================
       Classificação técnica
       ========================= */
        ProductCodeClassification classification = variant.getClassification();

        if (classification.getSubgroup() != null) {
            dto.setSubgroup(classification.getSubgroup().getName());
        }

        dto.setVehicleModels(
                classification.getApplications().stream()
                        .map(VehicleModel::getName)
                        .toList()
        );

    /* =========================
       Códigos
       ========================= */

        // 🔹 Manufacturer (1 por classificação)
        classification.getProductCodes().stream()
                .filter(pc -> pc.getCodeType() == CodeType.MANUFACTURER)
                .findFirst()
                .ifPresent(pc -> dto.setManufacturerCode(pc.getCode()));

        // 🔹 Distributor (N)
        dto.setDistributorCodes(
                classification.getProductCodes().stream()
                        .filter(pc -> pc.getCodeType() == CodeType.DISTRIBUTOR)
                        .map(ProductCode::getCode)
                        .toList()
        );

        // 🔹 SKU (INTERNAL)
        if (variant.getSku() != null) {
            dto.setSku(variant.getSku().getCode());
        }

    /* =========================
       Metadados opcionais
       ========================= */
        // Se brand vier do produto ou da classificação, ajuste aqui
        // dto.setBrand(...);

        return dto;
    }



}

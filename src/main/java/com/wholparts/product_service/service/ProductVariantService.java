package com.wholparts.product_service.service;

import com.wholparts.product_service.dto.ProductCreationDTO;
import com.wholparts.product_service.dto.ProductVariantViewDTO;
import com.wholparts.product_service.model.*;
import com.wholparts.product_service.repository.ProductCodeRepository;
import com.wholparts.product_service.repository.ProductVariantRepository;
import com.wholparts.product_service.util.SkuGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductService productService;
    private final ProductCodeRepository productCodeRepository;
    private final CodeConverterService codeConverterService;
    private final SkuGenerator skuGenerator;

    public ProductVariantService(
            ProductVariantRepository productVariantRepository,
            ProductService productService,
            ProductCodeRepository productCodeRepository,
            CodeConverterService codeConverterService,
            SkuGenerator skuGenerator) {

        this.productVariantRepository = productVariantRepository;
        this.productService = productService;
        this.productCodeRepository = productCodeRepository;
        this.codeConverterService = codeConverterService;
        this.skuGenerator = skuGenerator;
    }

    @Transactional
    public ProductVariantViewDTO create(ProductCreationDTO dto) {

        // 1️⃣ Produto base
        Product product = productService.getOrCreate(dto);

        // 2️⃣ Código distribuidor
        //criar codigo no service
        ProductCode distributorCode = productCodeRepository
                .findByCodeAndCodeType(dto.getDistributorCodeValue(), CodeType.DISTRIBUTOR)
                .orElseThrow(() -> new RuntimeException(
                        "Código distribuidor não encontrado"
                ));

        // 3️⃣ Código fabricante
        //criar codigo no service
        ProductCode manufacturerCode = codeConverterService
                .convertDistributorToManufacturer(distributorCode.getCode())
                .orElseThrow(() -> new RuntimeException(
                        "Código fabricante não encontrado"
                ));

        // 4️⃣ Classification vem do código (ponto-chave)
        ProductCodeClassification classification =
                manufacturerCode.getClassification();

        // 5️⃣ SKU (INTERNAL)
        ProductCode sku = skuGenerator.generate(
                dto.getManufacturer(),
                distributorCode.getCode(),
                manufacturerCode.getCode()
        );

        sku.setClassification(classification);
        productCodeRepository.save(sku);

        // 6️⃣ Variant
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .classification(classification)
                .sku(sku)
                .active(true)
                .build();

        productVariantRepository.save(variant);

        return toViewDTO(variant);
    }

    private ProductVariantViewDTO toViewDTO(ProductVariant variant) {

        ProductVariantViewDTO dto = new ProductVariantViewDTO();

        // 🔹 Variant
        dto.setVariantId(variant.getId());
        dto.setActive(variant.getActive());

        // 🔹 Product
        Product product = variant.getProduct();
        dto.setProductId(product.getId());
        dto.setProductName(product.getName());

        // 🔹 SKU (INTERNAL)
        dto.setSku(variant.getSku().getCode());

        // 🔹 Manufacturer Code (1 por classificação)
        ProductCodeClassification classification = variant.getClassification();

        dto.setManufacturerCode(
                classification
                        .getManufacturerCode()
                        .getCode()
        );

        // 🔹 Distributor Codes (N)
        List<String> distributorCodes =
                classification.getProductCodes().stream()
                        .filter(pc -> pc.getCodeType() == CodeType.DISTRIBUTOR)
                        .map(ProductCode::getCode)
                        .toList();

        dto.setDistributorCodes(distributorCodes);

        // 🔹 Subgrupo
        if (classification.getSubgroup() != null) {
            dto.setSubgroup(classification.getSubgroup().getName());
        }

        // 🔹 Aplicações (modelos)
        List<String> models =
                classification.getApplications().stream()
                        .map(VehicleModel::getName)
                        .toList();

        dto.setVehicleModels(models);

        return dto;
    }

    @Transactional(readOnly = true)
    public ProductVariantViewDTO findById(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Variant não encontrada")
                );

        return toViewDTO(variant);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantViewDTO> findAll() {
        return productVariantRepository.findAll().stream()
                .map(this::toViewDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductVariantViewDTO findBySku(String sku) {

        ProductCode skuCode = productCodeRepository
                .findByCodeAndCodeType(sku, CodeType.INTERNAL)
                .orElseThrow(() ->
                        new RuntimeException("SKU não encontrado")
                );

        ProductVariant variant = (ProductVariant) productVariantRepository
                .findBySku(skuCode)
                .orElseThrow(() ->
                        new RuntimeException("Variant não encontrada para SKU")
                );

        return toViewDTO(variant);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantViewDTO> findByAnyCode(String code) {

        ProductCode productCode = productCodeRepository
                .findByCode(code)
                .orElseThrow(() ->
                        new RuntimeException("Código não encontrado")
                );

        // 🔹 Caso 1: SKU
        if (productCode.getCodeType() == CodeType.INTERNAL) {

            ProductVariant variant = productVariantRepository
                    .findBySku(productCode)
                    .orElseThrow(() ->
                            new RuntimeException("Variant não encontrada para o SKU")
                    );

            return List.of(toViewDTO(variant));
        }

        // 🔹 Caso 2: código técnico
        ProductCodeClassification classification =
                productCode.getClassification();

        return productVariantRepository
                .findAllByClassification(classification)
                .stream()
                .map(this::toViewDTO)
                .toList();
    }


    @Transactional
    public void desactivate(Long id) {

        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Variant não encontrada")
                );

        variant.setActive(false);
    }

    @Transactional
    public void activate(Long id) {

        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Variant não encontrada")
                );

        variant.setActive(true);
    }

    @Transactional(readOnly = true)
    public List<ProductVariantViewDTO> findByClassification(Long classificationId) {

        return productVariantRepository
                .findAllByClassificationId(classificationId)
                .stream()
                .map(this::toViewDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductVariantViewDTO> findByProduct(Long productId) {

        return productVariantRepository
                .findAllByProductId(productId)
                .stream()
                .map(this::toViewDTO)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Variant não encontrada para exclusão"));
        productVariantRepository.delete(variant);
    }

    @Transactional(readOnly = true)
    public ProductVariantViewDTO view(Long id) {
        return findById(id); // já retorna ProductVariantViewDTO
    }

    @Transactional(readOnly = true)
    public List<ProductVariantViewDTO> findAllActive() {
        return productVariantRepository.findAllByActiveTrue()
                .stream()
                .map(this::toViewDTO)
                .toList();
    }
}




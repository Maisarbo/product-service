package com.wholparts.product_service.service;

import com.wholparts.product_service.dto.*;
import com.wholparts.product_service.model.*;
import com.wholparts.product_service.repository.*;
import com.wholparts.product_service.util.ApplicationApiClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassificationService {

    private final ProductCodeClassificationRepository classificationRepository;

    private final ProductGroupRepository productGroupRepository;
    private final SubgroupRepository subgroupRepository;


    private final VehicleModelService vehicleModelService;


    private final ApplicationApiClient apiClient;



    public void importarClassifications() {

        List<ApplicationDTO> imports = apiClient.buscarAplicacoes();

        for (ApplicationDTO dto : imports) {
            importarUmaClassification(dto);
        }
    }

    @Transactional
    public void importarUmaClassification(ApplicationDTO dto) {
        // 1️⃣ Pega o código do fabricante (obrigatório)
        ProductCodeDTO manufacturerCode = dto.getCodes()
                .stream()
                .filter(c -> c.getCodeType() == CodeType.MANUFACTURER)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Código MANUFACTURER obrigatório"));

        // 2️⃣ Cria ou recupera o subgrupo
        Subgroup subgroup = getOrCreateSubgroup(dto.getGroup(), dto.getSubgroup());

        // 3️⃣ Busca a classificação existente ou cria nova
        ProductCodeClassification classification = classificationRepository
                .findByProductCodes_CodeAndProductCodes_CodeType(
                        manufacturerCode.getCode(),
                        CodeType.MANUFACTURER
                ).orElseGet(() -> criarClassification(dto, subgroup));

        // 4️⃣ Atualiza marca e subgrupo (importante para atualização)
        classification.setBrand(dto.getBrand());
        classification.setSubgroup(subgroup);

        // 5️⃣ Merge incremental para códigos, aplicações e similares
        mergeCodesIncremental(classification, dto.getCodes());
        mergeApplicationsIncremental(classification, dto.getVeiculos());
        mergeSimilaresIncremental(classification, dto.getSimilarManufacturerCodes());



        // 6️⃣ Salva ou atualiza no banco apenas uma vez
        classificationRepository.saveAndFlush(classification);
    }
    private ProductCodeClassification criarClassification(
            ApplicationDTO dto,
            Subgroup subgroup
    ) {
        ProductCodeClassification c = new ProductCodeClassification();
        c.setBrand(dto.getBrand());
        c.setSubgroup(subgroup);
        return c;
    }

    private void mergeCodesIncremental(ProductCodeClassification classification, List<ProductCodeDTO> codes) {
        if (codes == null) return;

        for (ProductCodeDTO dto : codes) {
            boolean exists = classification.getProductCodes().stream()
                    .anyMatch(c -> c.getCode().equals(dto.getCode()) && c.getCodeType() == dto.getCodeType());
            if (!exists) {
                classification.addProductCode(new ProductCode(dto.getCode(), dto.getCodeType(), classification));
            }
        }
    }

    private void mergeApplicationsIncremental(
            ProductCodeClassification classification,
            List<VehicleModelCreateDTO> applications
    ) {
        if (applications == null) return;

        for (VehicleModelCreateDTO dto : applications) {
            for (Integer year : dto.getYears()) {

                VehicleModel model = vehicleModelService.create(new VehicleModelCreateDTO() {{
                    setBrand(dto.getBrand());
                    setVehicle(dto.getVehicle());
                    setModel(dto.getModel());
                    setYears(List.of(year));
                }});

                if (model == null) continue; // 🔹 ignora nulos

                // =====================================================
                // 1️⃣ PRINCIPAL <- DTO
                // =====================================================
                if (!classification.getApplications().contains(model)) {
                    classification.addApplication(model);
                }

                // =====================================================
                // 2️⃣ PRINCIPAL -> SIMILARES
                // =====================================================
                for (ProductCodeClassification similar : classification.getSimilars()) {
                    if (similar != null && !similar.getApplications().contains(model)) {
                        similar.addApplication(model);
                    }
                }
            }
        }

        // =====================================================
        // 3️⃣ SIMILARES -> PRINCIPAL (BIDIRECIONAL)
        // =====================================================
        for (ProductCodeClassification similar : classification.getSimilars()) {
            if (similar == null) continue;

            for (VehicleModel app : similar.getApplications()) {
                if (app != null && !classification.getApplications().contains(app)) {
                    classification.addApplication(app);
                }
            }
        }
    }

    public void mergeSimilaresIncremental(
            ProductCodeClassification principal,
            List<ApplicationSimilarDTO> similarManufacturerCodes
    ) {
        if (similarManufacturerCodes == null || similarManufacturerCodes.isEmpty()) return;

        for (ApplicationSimilarDTO dto : similarManufacturerCodes) {
            String code = dto.getManufacturerCode();

            // 1️⃣ Obtém ou cria o similar
            ProductCodeClassification similar =
                    classificationRepository
                            .findByProductCodes_CodeAndProductCodes_CodeType(
                                    code,
                                    CodeType.MANUFACTURER
                            )
                            .orElseGet(() -> {
                                ProductCodeClassification s = new ProductCodeClassification();
                                s.setBrand(dto.getBrand() != null ? dto.getBrand() : "DESCONHECIDA");
                                s.setSubgroup(principal.getSubgroup());
                                s.addProductCode(new ProductCode(code, CodeType.MANUFACTURER, s));
                                return classificationRepository.saveAndFlush(s);
                            });

            // 2️⃣ Liga principal <-> similar
            if (!principal.getSimilars().contains(similar)) principal.addSimilar(similar);
            if (!similar.getSimilars().contains(principal)) similar.addSimilar(principal);

            // 3️⃣ Merge bidirecional de aplicações apenas
            mergeApplicationsBidirectional(principal, similar);

            // 4️⃣ Garantir consistência entre todos os similares do principal
            for (ProductCodeClassification other : principal.getSimilars()) {
                if (other == similar) continue;

                // liga similares entre si
                if (!other.getSimilars().contains(similar)) other.addSimilar(similar);
                if (!similar.getSimilars().contains(other)) similar.addSimilar(other);

                // sincroniza aplicações
                mergeApplicationsBidirectional(other, similar);
            }
        }
    }



    private void mergeApplicationsBidirectional(
            ProductCodeClassification a,
            ProductCodeClassification b
    ) {
        // a -> b
        for (VehicleModel app : a.getApplications()) {
            if (!b.getApplications().contains(app)) {
                b.addApplication(app);
            }
        }

        // b -> a
        for (VehicleModel app : b.getApplications()) {
            if (!a.getApplications().contains(app)) {
                a.addApplication(app);
            }
        }
    }




    private void mergeCodesBidirectional(
            ProductCodeClassification principal,
            ProductCodeClassification similar
    ) {
        // =====================================================
        // principal -> similar
        // =====================================================
        for (ProductCode code : principal.getProductCodes()) {
            boolean exists = similar.getProductCodes().stream()
                    .anyMatch(c ->
                            c.getCode().equals(code.getCode()) &&
                                    c.getCodeType() == code.getCodeType()
                    );

            if (!exists) {
                similar.addProductCode(
                        new ProductCode(
                                code.getCode(),
                                code.getCodeType(),
                                similar
                        )
                );
            }
        }

        // =====================================================
        // similar -> principal
        // =====================================================
        for (ProductCode code : similar.getProductCodes()) {
            boolean exists = principal.getProductCodes().stream()
                    .anyMatch(c ->
                            c.getCode().equals(code.getCode()) &&
                                    c.getCodeType() == code.getCodeType()
                    );

            if (!exists) {
                principal.addProductCode(
                        new ProductCode(
                                code.getCode(),
                                code.getCodeType(),
                                principal
                        )
                );
            }
        }
    }



    private Subgroup getOrCreateSubgroup(String groupName, String subgroupName) {

        ProductGroup group = productGroupRepository
                .findByName(groupName)
                .orElseGet(() -> {
                    ProductGroup g = new ProductGroup();
                    g.setName(groupName);
                    return productGroupRepository.save(g);
                });

        return subgroupRepository
                .findByNameAndGroup(subgroupName, group)
                .orElseGet(() -> {
                    Subgroup s = new Subgroup();
                    s.setName(subgroupName);
                    s.setGroup(group);
                    return subgroupRepository.save(s);
                });
    }




    public ProductCodeClassification getByManufacturerCode(String manufacturerCode) {
        return classificationRepository
                .findByProductCodes_CodeAndProductCodes_CodeType(
                        manufacturerCode,
                        CodeType.MANUFACTURER
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Classification não encontrada para código fabricante: " + manufacturerCode
                        )
                );
    }
    @Transactional
    public ProductCodeClassification update(ClassificationUpdateDTO dto) {

        ProductCodeClassification classification =
                classificationRepository.findById(dto.getId())
                        .orElseThrow(() ->
                                new EntityNotFoundException("Classification não encontrada")
                        );

        // 1️⃣ Marca
        if (dto.getBrand() != null) {
            classification.setBrand(dto.getBrand());
        }

        // 2️⃣ Grupo / Subgrupo
        if (dto.getGroup() != null && dto.getSubgroup() != null) {
            Subgroup subgroup = getOrCreateSubgroup(
                    dto.getGroup(),
                    dto.getSubgroup()
            );
            classification.setSubgroup(subgroup);
        }

        // 3️⃣ Códigos (somente adiciona novos)
        if (dto.getCodes() != null && !dto.getCodes().isEmpty()) {
            mergeCodesIncremental(classification, dto.getCodes());
        }

        // 4️⃣ Aplicações
        if (dto.getVehicles() != null && !dto.getVehicles().isEmpty()) {
            mergeApplicationsIncremental(classification, dto.getVehicles());
        }

        return classificationRepository.save(classification);
    }

    @Transactional(readOnly = true)
    public List<VehicleModel> getApplications(String manufacturerCode) {

        ProductCodeClassification classification =
                getByManufacturerCode(manufacturerCode);

        return List.copyOf(classification.getApplications());
    }


    @Transactional
    public void addSimilar(
            Long classificationId,
            Long similarId
    ) {
        ProductCodeClassification c1 = classificationRepository.findById(classificationId)
                .orElseThrow();

        ProductCodeClassification c2 = classificationRepository.findById(similarId)
                .orElseThrow();

        if (!c1.getSimilars().contains(c2)) {
            c1.getSimilars().add(c2);
            c2.getSimilars().add(c1); // garante simetria
        }
    }

    @Transactional
    public ProductCodeClassification createManual(ClassificationCreateDTO dto) {

        Subgroup subgroup = subgroupRepository
                .findByName(dto.getSubGroup())
                .orElseThrow(() -> new IllegalArgumentException("Subgrupo inválido"));

        ProductCodeClassification classification = new ProductCodeClassification();
        classification.setBrand(dto.getBrand());
        classification.setSubgroup(subgroup);

        for (CodesDTO codeDTO : dto.getCodes()) {
            ProductCode code = new ProductCode(
                    codeDTO.getCode(),
                    codeDTO.getCodeType(),
                    classification
            );
            classification.getProductCodes().add(code);
        }

        return classificationRepository.save(classification);
    }

    @Transactional(readOnly = true)
    public ProductCodeClassification findById(Long id) {
        return classificationRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "ProductCodeClassification não encontrada. ID: " + id
                        )
                );
    }
}









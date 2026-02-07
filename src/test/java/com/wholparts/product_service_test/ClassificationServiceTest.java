package com.wholparts.product_service_test;

import com.wholparts.product_service.dto.*;
import com.wholparts.product_service.model.*;
import com.wholparts.product_service.repository.*;
import com.wholparts.product_service.service.ClassificationService;
import com.wholparts.product_service.service.VehicleModelService;
import com.wholparts.product_service.util.ApplicationApiClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassificationServiceTest {

    @InjectMocks
    private ClassificationService classificationService;

    @Mock
    private ProductCodeClassificationRepository classificationRepository;

    @Mock
    private ProductGroupRepository productGroupRepository;

    @Mock
    private SubgroupRepository subgroupRepository;

    @Mock
    private VehicleModelService vehicleModelService;

    @Mock
    private ApplicationApiClient apiClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private ProductCodeClassification classification(Long id, String brand) {
        ProductCodeClassification c = new ProductCodeClassification();
        c.setId(id);
        c.setBrand(brand);
        return c;
    }

    private Subgroup subgroup() {
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup s = new Subgroup();
        s.setId(1L);
        s.setName("Pastilhas");
        s.setGroup(group);
        return s;
    }


    // =====================================================
    // 1️⃣ Teste: Criar e sincronizar similares por manufacturer
    // =====================================================
    @Test
    void testeImportarUmaClassificationComPrintln() {
        System.out.println("=== INÍCIO DO TESTE DE IMPORTAÇÃO ===");

        // 1️⃣ Criando DTO de teste
        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");

        ProductCodeDTO codeDTO = new ProductCodeDTO();
        codeDTO.setCode("TRW-001");
        codeDTO.setCodeType(CodeType.MANUFACTURER);
        dto.setCodes(List.of(codeDTO));

        VehicleModelCreateDTO vehicleDTO = new VehicleModelCreateDTO();
        vehicleDTO.setBrand("VW");
        vehicleDTO.setVehicle("Gol");
        vehicleDTO.setModel("1.0");
        vehicleDTO.setYears(List.of(2023));  // apenas um ano
        dto.setVeiculos(List.of(vehicleDTO));

        ApplicationSimilarDTO similarDTO = new ApplicationSimilarDTO();
        similarDTO.setManufacturerCode("SIM-001");
        similarDTO.setBrand("TRW-Similar");
        dto.setSimilarManufacturerCodes(List.of(similarDTO));

        // 2️⃣ Mocks necessários
        VehicleBrand mockBrand = new VehicleBrand();
        mockBrand.setName("VW");

        Vehicle mockVehicle = new Vehicle();
        mockVehicle.setName("Gol");
        mockVehicle.setBrand(mockBrand);

        // Mock do VehicleModelService
        when(vehicleModelService.create(any(VehicleModelCreateDTO.class)))
                .thenAnswer(invocation -> {
                    VehicleModelCreateDTO arg = invocation.getArgument(0);

                    VehicleBrand brand = new VehicleBrand();
                    brand.setName(arg.getBrand());

                    Vehicle vehicle = new Vehicle();
                    vehicle.setName(arg.getVehicle());
                    vehicle.setBrand(brand);

                    List<YearModel> years = new ArrayList<>();
                    for (Integer y : arg.getYears()) {
                        YearModel ym = new YearModel();
                        ym.setYear(y);
                        years.add(ym);
                    }

                    VehicleModel model = new VehicleModel(arg.getModel(), vehicle, years);

                    System.out.println("Criando VehicleModel: " +
                            brand.getName() + " " +
                            vehicle.getName() + " " +
                            model.getName() + " " +
                            years.stream().map(YearModel::getYear).toList());
                    return model;
                });

        // Mock para simular criação (não existe ainda)
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "TRW-001", CodeType.MANUFACTURER))
                .thenReturn(Optional.empty());

        // Mock para salvar a classification
        when(classificationRepository.saveAndFlush(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> {
                    ProductCodeClassification c = invocation.getArgument(0);

                    System.out.println("Salvando classification: " + c.getBrand());
                    System.out.println("Códigos: " +
                            c.getProductCodes().stream()
                                    .map(ProductCode::getCode)
                                    .toList());
                    System.out.println("Aplicações: " +
                            c.getApplications().stream()
                                    .map(vm -> vm.getVehicle().getBrand().getName() + " " +
                                            vm.getVehicle().getName() + " " +
                                            vm.getName() + " " +
                                            vm.getYears().stream().map(YearModel::getYear).toList())
                                    .toList());
                    System.out.println("Similares: " +
                            c.getSimilars().stream()
                                    .map(ProductCodeClassification::getBrand)
                                    .toList());
                    return c;
                });

        // 3️⃣ Executando o método
        classificationService.importarUmaClassification(dto);

        System.out.println("=== FIM DO TESTE DE IMPORTAÇÃO ===");
    }

    @Test
    void testPromoteSimilarToPrincipal() {
        System.out.println("=== INÍCIO DO TESTE DE PROMOÇÃO ===");

        // Criando grupo e subgrupo
        ProductGroup freios = new ProductGroup();
        freios.setName("Freios");

        Subgroup pastilhas = new Subgroup();
        pastilhas.setName("Pastilhas");
        pastilhas.setGroup(freios);

        // Criando marca e veículo
        VehicleBrand vwBrand = new VehicleBrand();
        vwBrand.setName("VW");

        Vehicle gol = new Vehicle();
        gol.setBrand(vwBrand);
        gol.setName("Gol 1.0");

        // Criando VehicleModel vinculado ao veículo
        VehicleModel golModel2023 = new VehicleModel();
        golModel2023.setName("VW Gol 1.0 [2023]");
        golModel2023.setVehicle(gol);

        // Criando classification principal
        ProductCodeClassification principal = new ProductCodeClassification();
        principal.setBrand("TRW");
        principal.setSubgroup(pastilhas);

        // Adicionando aplicação
        principal.addApplication(golModel2023);

        // Criando classification similar
        ProductCodeClassification similar = new ProductCodeClassification();
        similar.setBrand("TRW-Similar");
        similar.setSubgroup(principal.getSubgroup());

        // Vinculando similar à principal
        principal.addSimilar(similar);

        // --- Promoção: similar se torna principal ---
        // Passo 1: copiar aplicações e product codes
        similar.getApplications().addAll(principal.getApplications());
        similar.getProductCodes().addAll(principal.getProductCodes());

        // Passo 2: atualizar lista de similars mantendo bidirecionalidade
        Set<ProductCodeClassification> previousSimilars = new HashSet<>(principal.getSimilars());
        previousSimilars.remove(similar); // remover o similar para não duplicar
        similar.getSimilars().addAll(previousSimilars);
        similar.getSimilars().add(principal); // antigo principal continua como similar

        for (ProductCodeClassification s : previousSimilars) {
            s.getSimilars().remove(principal);
            s.getSimilars().add(similar);
        }

        // Atualizar o antigo principal para manter o novo similar
        principal.getSimilars().clear();
        principal.getSimilars().add(similar);

        // --- Impressões para debug ---
        System.out.println("Principal original: " + principal.getBrand());
        System.out.println("Similar promovido: " + similar.getBrand());
        System.out.println("Aplicações do novo principal: " + similar.getApplications());
        System.out.println("Similares do novo principal: " + similar.getSimilars());
        System.out.println("Similares do antigo principal: " + principal.getSimilars());

        // --- Validações ---
        assertThat(similar.getApplications()).contains(golModel2023);
        assertThat(similar.getSimilars()).contains(principal);
        assertThat(principal.getSimilars()).contains(similar);

        System.out.println("=== FIM DO TESTE DE PROMOÇÃO ===");
    }

    @Test
    void importarClassifications_deveImportarListaInteira() {
        System.out.println("=== INÍCIO DO TESTE IMPORTAR LISTA ===");

        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");

        ProductCodeDTO manufacturer = new ProductCodeDTO();
        manufacturer.setCode("TRW-1234");
        manufacturer.setCodeType(CodeType.MANUFACTURER);

        dto.setCodes(List.of(manufacturer));

        when(apiClient.buscarAplicacoes())
                .thenReturn(List.of(dto));

        when(classificationRepository.saveAndFlush(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        classificationService.importarClassifications();

        verify(classificationRepository, times(1)).saveAndFlush(any());

        System.out.println("=== FIM DO TESTE IMPORTAR LISTA ===");
    }

    @Test
    void importarUmaClassification_deveCriarNovaClassification() {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");
        dto.setCodes(List.of(
                new ProductCodeDTO("ABC123", CodeType.MANUFACTURER)
        ));

        when(productGroupRepository.findByName("Freios"))
                .thenReturn(Optional.empty());
        when(subgroupRepository.findByNameAndGroup(any(), any()))
                .thenReturn(Optional.empty());
        when(classificationRepository
                .findByProductCodes_CodeAndProductCodes_CodeType(any(), any()))
                .thenReturn(Optional.empty());

        classificationService.importarUmaClassification(dto);

        verify(classificationRepository).saveAndFlush(any());
    }

    @Test
    void importarUmaClassification_semManufacturer_deveFalhar() {
        ApplicationDTO dto = new ApplicationDTO();
        dto.setCodes(List.of(
                new ProductCodeDTO("999", CodeType.INTERNAL)
        ));

        assertThrows(IllegalStateException.class,
                () -> classificationService.importarUmaClassification(dto));
    }

    /* =====================================================
       getByManufacturerCode
       ===================================================== */

    @Test
    void getByManufacturerCode_deveRetornarClassification() {
        ProductCodeClassification c = classification(1L, "TRW");

        when(classificationRepository
                .findByProductCodes_CodeAndProductCodes_CodeType(
                        "ABC", CodeType.MANUFACTURER))
                .thenReturn(Optional.of(c));

        ProductCodeClassification result =
                classificationService.getByManufacturerCode("ABC");

        assertEquals("TRW", result.getBrand());
    }

    @Test
    void getByManufacturerCode_inexistente_deveLancarErro() {
        when(classificationRepository
                .findByProductCodes_CodeAndProductCodes_CodeType(any(), any()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> classificationService.getByManufacturerCode("XXX"));
    }

    /* =====================================================
       update
       ===================================================== */

    @Test
    void update_deveAtualizarMarca() {

        // ====== ARRANGE ======
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup subgroup = new Subgroup();
        subgroup.setId(1L);
        subgroup.setName("Pastilhas");
        subgroup.setGroup(group);

        ProductCodeClassification existing = new ProductCodeClassification();
        existing.setId(10L);
        existing.setBrand("TRW");
        existing.setSubgroup(subgroup);

        when(classificationRepository.findById(10L))
                .thenReturn(Optional.of(existing));

        // 🔴 ISSO É O QUE FALTAVA
        when(classificationRepository.save(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClassificationUpdateDTO dto = new ClassificationUpdateDTO();
        dto.setId(10L);
        dto.setBrand("BOSCH");

        // ====== ACT ======
        ProductCodeClassification updated =
                classificationService.update(dto);

        // ====== ASSERT ======
        assertThat(updated).isNotNull();
        assertThat(updated.getBrand()).isEqualTo("BOSCH");

        // ====== LOG ======
        System.out.println("Marca atualizada para: " + updated.getBrand());
    }


    @Test
    void update_classificationInexistente() {
        when(classificationRepository.findById(any()))
                .thenReturn(Optional.empty());

        ClassificationUpdateDTO dto = new ClassificationUpdateDTO();
        dto.setId(99L);

        assertThrows(EntityNotFoundException.class,
                () -> classificationService.update(dto));
    }

    /* =====================================================
       getApplications
       ===================================================== */

    @Test
    void getApplications_deveRetornarListaImutavel() {
        ProductCodeClassification c = classification(1L, "TRW");

        VehicleModel model = new VehicleModel();
        model.setId(1L);
        c.getApplications().add(model);

        when(classificationRepository
                .findByProductCodes_CodeAndProductCodes_CodeType(any(), any()))
                .thenReturn(Optional.of(c));

        List<VehicleModel> apps =
                classificationService.getApplications("ABC");

        assertEquals(1, apps.size());
        assertThrows(UnsupportedOperationException.class,
                () -> apps.add(new VehicleModel()));
    }

    /* =====================================================
       addSimilar
       ===================================================== */

    @Test
    void addSimilar_deveCriarRelacionamentoBidirecional() {
        ProductCodeClassification c1 = classification(1L, "TRW");
        ProductCodeClassification c2 = classification(2L, "BOSCH");

        when(classificationRepository.findById(1L))
                .thenReturn(Optional.of(c1));
        when(classificationRepository.findById(2L))
                .thenReturn(Optional.of(c2));

        classificationService.addSimilar(1L, 2L);

        assertTrue(c1.getSimilars().contains(c2));
        assertTrue(c2.getSimilars().contains(c1));
    }

    /* =====================================================
       createManual
       ===================================================== */

    @Test
    void createManual_deveCriarClassificationComCodes() {

        // ===== ARRANGE =====
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup subgroup = new Subgroup();
        subgroup.setId(1L);
        subgroup.setName("Pastilhas");
        subgroup.setGroup(group);

        when(subgroupRepository.findByName("Pastilhas"))
                .thenReturn(Optional.of(subgroup));

        // 🔴 ISSENCIAL — sem isso o retorno é null
        when(classificationRepository.save(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CodesDTO code = new CodesDTO();
        code.setCode("TRW-001");
        code.setCodeType(CodeType.MANUFACTURER);

        ClassificationCreateDTO dto = new ClassificationCreateDTO();
        dto.setBrand("TRW");
        dto.setSubGroup("Pastilhas");
        dto.setCodes(List.of(code));

        // ===== ACT =====
        ProductCodeClassification saved =
                classificationService.createManual(dto);

        // ===== ASSERT =====
        assertThat(saved).isNotNull();
        assertThat(saved.getBrand()).isEqualTo("TRW");
        assertThat(saved.getSubgroup().getName()).isEqualTo("Pastilhas");

        assertThat(saved.getProductCodes())
                .hasSize(1)
                .anyMatch(c ->
                        c.getCode().equals("TRW-001") &&
                                c.getCodeType() == CodeType.MANUFACTURER
                );

        // ===== LOG =====
        System.out.println("Classification criada: " + saved.getBrand());
        System.out.println("Códigos: " + saved.getProductCodes());
    }

    @Test
    void findById_deveRetornar() {
        ProductCodeClassification c = classification(1L, "TRW");

        when(classificationRepository.findById(1L))
                .thenReturn(Optional.of(c));

        ProductCodeClassification result = classificationService.findById(1L);

        assertEquals("TRW", result.getBrand());
    }

    @Test
    void findById_inexistente() {
        when(classificationRepository.findById(any()))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> classificationService.findById(99L));
    }

    @Test
    void importar_deveCriarSimilarQuandoNaoExistirEReplicarBaseDoPrincipal() {

        System.out.println("\n=== TESTE: IMPORTAÇÃO CRIA SIMILAR E REPLICA BASE ===");

        // =====================================================
        // ARRANGE — GRUPO / SUBGRUPO
        // =====================================================
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup subgroup = new Subgroup();
        subgroup.setId(10L);
        subgroup.setName("Pastilhas");
        subgroup.setGroup(group);

        when(productGroupRepository.findByName("Freios"))
                .thenReturn(Optional.of(group));

        when(subgroupRepository.findByNameAndGroup("Pastilhas", group))
                .thenReturn(Optional.of(subgroup));

        // =====================================================
        // ARRANGE — VEÍCULO / APLICAÇÃO (JÁ EXISTENTE)
        // =====================================================
        VehicleBrand vw = new VehicleBrand(null, "VW");
        Vehicle gol = new Vehicle(null, vw, "Gol");

        VehicleModel gol2023 = new VehicleModel();
        gol2023.setName("Gol 1.0 2023");
        gol2023.setVehicle(gol);

        // =====================================================
        // ARRANGE — PRINCIPAL EXISTENTE
        // =====================================================
        ProductCodeClassification principal = new ProductCodeClassification();
        principal.setId(100L);
        principal.setBrand("TRW");
        principal.setSubgroup(subgroup);

        principal.addProductCode(
                new ProductCode("TRW-001", CodeType.MANUFACTURER, principal)
        );

        // aplicação já existente no principal
        principal.addApplication(gol2023);

        assertThat(principal.getSimilars()).isEmpty();

        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "TRW-001", CodeType.MANUFACTURER))
                .thenReturn(Optional.of(principal));

        // =====================================================
        // ARRANGE — SIMILAR NÃO EXISTE
        // =====================================================
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "BOSCH-999", CodeType.MANUFACTURER))
                .thenReturn(Optional.empty());

        when(classificationRepository.saveAndFlush(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> {
                    ProductCodeClassification c = invocation.getArgument(0);
                    if (c.getId() == null) c.setId(new Random().nextLong());
                    return c;
                });

        // =====================================================
        // ARRANGE — DTO DE IMPORTAÇÃO
        // =====================================================
        ProductCodeDTO mainCode = new ProductCodeDTO();
        mainCode.setCode("TRW-001");
        mainCode.setCodeType(CodeType.MANUFACTURER);

        ApplicationSimilarDTO similarDto = new ApplicationSimilarDTO();
        similarDto.setManufacturerCode("BOSCH-999");
        similarDto.setBrand("BOSCH");

        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");
        dto.setCodes(List.of(mainCode));

        // IMPORTANTE: não testamos criação de veículo aqui
        dto.setVeiculos(Collections.emptyList());

        dto.setSimilarManufacturerCodes(List.of(similarDto));

        when(apiClient.buscarAplicacoes())
                .thenReturn(List.of(dto));

        // =====================================================
        // ACT
        // =====================================================
        classificationService.importarClassifications();

        // =====================================================
        // ASSERT — SIMILAR CRIADO
        // =====================================================
        assertThat(principal.getSimilars()).hasSize(1);

        ProductCodeClassification similar =
                principal.getSimilars().iterator().next();

        // =====================================================
        // ASSERT — BASE REPLICADA
        // =====================================================
        assertThat(similar.getBrand()).isEqualTo("BOSCH");
        assertThat(similar.getSubgroup()).isEqualTo(subgroup);

        // =====================================================
        // ASSERT — PRODUCT CODES
        // =====================================================
        assertThat(similar.getProductCodes())
                .anyMatch(c ->
                        c.getCode().equals("BOSCH-999") &&
                                c.getCodeType() == CodeType.MANUFACTURER
                );

        // =====================================================
        // ASSERT — APLICAÇÕES HERDADAS
        // =====================================================
        assertThat(similar.getApplications())
                .containsExactlyInAnyOrderElementsOf(principal.getApplications());

        // =====================================================
        // ASSERT — RELAÇÃO DE SIMILARES
        // =====================================================
        assertThat(similar.getSimilars())
                .contains(principal)
                .doesNotContain(similar);

        assertThat(principal.getSimilars())
                .contains(similar);

        // =====================================================
        // LOG
        // =====================================================
        System.out.println("Principal: " + principal.getBrand());
        System.out.println("Aplicações principal: " +
                principal.getApplications().stream()
                        .map(v -> v.getVehicle().getName())
                        .toList());

        System.out.println("Similar criado: " + similar.getBrand());
        System.out.println("Aplicações similar: " +
                similar.getApplications().stream()
                        .map(v -> v.getVehicle().getName())
                        .toList());

        System.out.println("Similares do principal: " +
                principal.getSimilars().stream()
                        .map(ProductCodeClassification::getBrand)
                        .toList());

        System.out.println("Similares do similar: " +
                similar.getSimilars().stream()
                        .map(ProductCodeClassification::getBrand)
                        .toList());

        System.out.println("=== FIM TESTE ===\n");
    }

    @Test
    void importar_deveCriarMultiplosSimilaresEManterRelacionamentoCompleto() {

        System.out.println("\n=== TESTE: PRINCIPAL COM MÚLTIPLOS SIMILARES ===");

        // =====================================================
        // GRUPO / SUBGRUPO
        // =====================================================
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup subgroup = new Subgroup();
        subgroup.setId(10L);
        subgroup.setName("Pastilhas");
        subgroup.setGroup(group);

        when(productGroupRepository.findByName("Freios"))
                .thenReturn(Optional.of(group));

        when(subgroupRepository.findByNameAndGroup("Pastilhas", group))
                .thenReturn(Optional.of(subgroup));

        // =====================================================
        // VEÍCULO / APLICAÇÃO
        // =====================================================
        VehicleBrand vw = new VehicleBrand(null, "VW");
        Vehicle gol = new Vehicle(null, vw, "Gol");

        VehicleModel gol2023 = new VehicleModel();
        gol2023.setName("VW Gol 1.0 [2023]");
        gol2023.setVehicle(gol);

        when(vehicleModelService.create(any()))
                .thenReturn(gol2023);

        // =====================================================
        // PRINCIPAL EXISTENTE
        // =====================================================
        ProductCodeClassification principal = new ProductCodeClassification();
        principal.setId(100L);
        principal.setBrand("TRW");
        principal.setSubgroup(subgroup);

        principal.addProductCode(
                new ProductCode("TRW-001", CodeType.MANUFACTURER, principal)
        );

        principal.addApplication(gol2023);

        // =====================================================
        // SIMILAR JÁ EXISTENTE
        // =====================================================
        ProductCodeClassification similarExistente = new ProductCodeClassification();
        similarExistente.setId(200L);
        similarExistente.setBrand("ATE");
        similarExistente.setSubgroup(subgroup);

        similarExistente.addProductCode(
                new ProductCode("ATE-111", CodeType.MANUFACTURER, similarExistente)
        );

        // já ligado ao principal
        principal.addSimilar(similarExistente);
        similarExistente.addSimilar(principal);

        // =====================================================
        // MOCK REPOSITORY
        // =====================================================
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "TRW-001", CodeType.MANUFACTURER))
                .thenReturn(Optional.of(principal));

        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "ATE-111", CodeType.MANUFACTURER))
                .thenReturn(Optional.of(similarExistente));

        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "BOSCH-999", CodeType.MANUFACTURER))
                .thenReturn(Optional.empty());

        when(classificationRepository.saveAndFlush(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> {
                    ProductCodeClassification c = invocation.getArgument(0);
                    if (c.getId() == null) {
                        c.setId(new Random().nextLong());
                    }
                    return c;
                });

        // =====================================================
        // DTO DE IMPORTAÇÃO
        // =====================================================
        ProductCodeDTO mainCode = new ProductCodeDTO();
        mainCode.setCode("TRW-001");
        mainCode.setCodeType(CodeType.MANUFACTURER);

        ApplicationSimilarDTO similar1 = new ApplicationSimilarDTO();
        similar1.setManufacturerCode("ATE-111");
        similar1.setBrand("ATE");

        ApplicationSimilarDTO similar2 = new ApplicationSimilarDTO();
        similar2.setManufacturerCode("BOSCH-999");
        similar2.setBrand("BOSCH");

        // =====================================================
// DTO DE IMPORTAÇÃO — VEÍCULOS (FORMA CORRETA)
// =====================================================
        VehicleModelCreateDTO vehicleDTO = new VehicleModelCreateDTO();
        vehicleDTO.setBrand("VW");
        vehicleDTO.setVehicle("Gol");
        vehicleDTO.setModel("1.0");
        vehicleDTO.setYears(List.of(2023));

        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");
        dto.setCodes(List.of(mainCode));
        dto.setVeiculos(List.of(vehicleDTO));
        dto.setSimilarManufacturerCodes(List.of(similar1, similar2));


        when(apiClient.buscarAplicacoes())
                .thenReturn(List.of(dto));

        // =====================================================
        // ACT
        // =====================================================
        classificationService.importarClassifications();

        // =====================================================
        // ASSERT — PRINCIPAL
        // =====================================================
        assertThat(principal.getSimilars()).hasSize(2);

        // =====================================================
        // ASSERT — NOVO SIMILAR
        // =====================================================
        ProductCodeClassification bosch = principal.getSimilars().stream()
                .filter(c -> c.getBrand().equals("BOSCH"))
                .findFirst()
                .orElseThrow();

        assertThat(bosch.getApplications())
                .containsExactlyInAnyOrder(gol2023);

        assertThat(bosch.getSimilars())
                .contains(principal, similarExistente)
                .doesNotContain(bosch);

        // =====================================================
        // ASSERT — SIMILAR EXISTENTE
        // =====================================================
        assertThat(similarExistente.getSimilars())
                .contains(principal, bosch)
                .doesNotContain(similarExistente);

        // =====================================================
        // LOG
        // =====================================================
        System.out.println("Principal: " + principal.getBrand());
        System.out.println("Similares do principal: " +
                principal.getSimilars().stream()
                        .map(ProductCodeClassification::getBrand)
                        .toList());

        System.out.println("Similares do BOSCH: " +
                bosch.getSimilars().stream()
                        .map(ProductCodeClassification::getBrand)
                        .toList());

        System.out.println("=== FIM TESTE ===\n");
    }

    @Test
    void importar_deveManterAplicacoesExistentesDoSimilarEAdicionarSomenteNovas() {

        // =====================================================
        // ARRANGE — VEÍCULOS
        // =====================================================
        VehicleBrand vw = new VehicleBrand(null, "VW");

        Vehicle gol = new Vehicle(null, vw, "Gol");
        Vehicle palio = new Vehicle(null, new VehicleBrand(null, "FIAT"), "Palio");
        Vehicle fox = new Vehicle(null, vw, "Fox");

        VehicleModel golModel = new VehicleModel();
        golModel.setName("Gol 1.0");
        golModel.setVehicle(gol);

        VehicleModel palioModel = new VehicleModel();
        palioModel.setName("Palio 1.0");
        palioModel.setVehicle(palio);

        VehicleModel foxModel = new VehicleModel();
        foxModel.setName("Fox 1.6");
        foxModel.setVehicle(fox);

        when(vehicleModelService.create(any()))
                .thenAnswer(inv -> {
                    VehicleModel vm = inv.getArgument(0);
                    if (vm.getName().contains("Gol")) return golModel;
                    if (vm.getName().contains("Palio")) return palioModel;
                    return foxModel;
                });

        // =====================================================
        // ARRANGE — SUBGRUPO
        // =====================================================
        Subgroup subgroup = new Subgroup();
        subgroup.setId(10L);
        subgroup.setName("Pastilhas");

        // =====================================================
        // ARRANGE — SIMILAR JÁ EXISTENTE
        // =====================================================
        ProductCodeClassification similarExistente =
                new ProductCodeClassification();
        similarExistente.setId(200L);
        similarExistente.setBrand("BOSCH");
        similarExistente.setSubgroup(subgroup);

        similarExistente.addProductCode(
                new ProductCode("BOSCH-999", CodeType.MANUFACTURER, similarExistente)
        );

        // 🔹 Já possui Gol e Palio
        similarExistente.addApplication(golModel);
        similarExistente.addApplication(palioModel);

        // =====================================================
        // ARRANGE — PRINCIPAL
        // =====================================================
        ProductCodeClassification principal =
                new ProductCodeClassification();
        principal.setId(100L);
        principal.setBrand("TRW");
        principal.setSubgroup(subgroup);

        principal.addProductCode(
                new ProductCode("TRW-001", CodeType.MANUFACTURER, principal)
        );

        // Importação como principal traz Gol, Palio e Fox
        principal.addApplication(golModel);
        principal.addApplication(palioModel);
        principal.addApplication(foxModel);

        // =====================================================
        // MOCK — REPOSITÓRIO
        // =====================================================
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "BOSCH-999", CodeType.MANUFACTURER))
                .thenReturn(Optional.of(similarExistente));

        when(classificationRepository.saveAndFlush(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        // =====================================================
        // ACT — MERGE
        // =====================================================
        ApplicationSimilarDTO dto = new ApplicationSimilarDTO();
        dto.setManufacturerCode("BOSCH-999");
        dto.setBrand("BOSCH");

        classificationService.mergeSimilaresIncremental(
                principal,
                List.of(dto)
        );

        // =====================================================
        // ASSERT — APLICAÇÕES
        // =====================================================
        assertThat(similarExistente.getApplications())
                .containsExactlyInAnyOrder(
                        golModel,
                        palioModel,
                        foxModel
                );

        // =====================================================
        // ASSERT — RELACIONAMENTO
        // =====================================================
        assertThat(principal.getSimilars())
                .contains(similarExistente);

        assertThat(similarExistente.getSimilars())
                .contains(principal);

        // =====================================================
        // LOG
        // =====================================================
        System.out.println("Aplicações finais do similar:");
        similarExistente.getApplications()
                .forEach(a ->
                        System.out.println("- " + a.getVehicle().getName())
                );
    }
    @Test
    void importar_deveReutilizarProductCodeVehicleModelESimilarExistente() {

        System.out.println("\n=== TESTE: REUTILIZA TUDO NA IMPORTAÇÃO ===");

        // =====================================================
        // GRUPO / SUBGRUPO
        // =====================================================
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup subgroup = new Subgroup();
        subgroup.setId(10L);
        subgroup.setName("Pastilhas");
        subgroup.setGroup(group);

        when(productGroupRepository.findByName("Freios"))
                .thenReturn(Optional.of(group));

        when(subgroupRepository.findByNameAndGroup("Pastilhas", group))
                .thenReturn(Optional.of(subgroup));

        // =====================================================
        // VEHICLE MODEL JÁ EXISTENTE
        // =====================================================
        VehicleBrand vw = new VehicleBrand(null, "VW");
        Vehicle gol = new Vehicle(null, vw, "Gol");

        VehicleModel golModel = new VehicleModel();
        golModel.setId(200L);
        golModel.setName("Gol 1.0");
        golModel.setVehicle(gol);

        when(vehicleModelService.create(any()))
                .thenReturn(golModel); // reutiliza sempre

        // =====================================================
        // SIMILAR JÁ EXISTENTE
        // =====================================================
        ProductCodeClassification bosch = new ProductCodeClassification();
        bosch.setId(300L);
        bosch.setBrand("BOSCH");
        bosch.setSubgroup(subgroup);

        ProductCode boschCode =
                new ProductCode("BOSCH-999", CodeType.MANUFACTURER, bosch);

        bosch.addProductCode(boschCode);
        bosch.addApplication(golModel);

        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "BOSCH-999", CodeType.MANUFACTURER
        )).thenReturn(Optional.of(bosch));

        // =====================================================
        // PRINCIPAL NÃO EXISTE
        // =====================================================
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "TRW-001", CodeType.MANUFACTURER
        )).thenReturn(Optional.empty());

        // 👉 guarda o principal salvo
        AtomicReference<ProductCodeClassification> principalSalvo =
                new AtomicReference<>();

        when(classificationRepository.saveAndFlush(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> {
                    ProductCodeClassification c = invocation.getArgument(0);
                    if (c.getId() == null) c.setId(100L);
                    principalSalvo.set(c);
                    return c;
                });

        // 👉 depois da importação, o findBy precisa devolver o principal criado
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "TRW-001", CodeType.MANUFACTURER
        )).thenAnswer(invocation ->
                Optional.ofNullable(principalSalvo.get())
        );

        // =====================================================
        // DTO DE IMPORTAÇÃO
        // =====================================================
        ProductCodeDTO trwCode = new ProductCodeDTO();
        trwCode.setCode("TRW-001");
        trwCode.setCodeType(CodeType.MANUFACTURER);

        ApplicationSimilarDTO similarDto = new ApplicationSimilarDTO();
        similarDto.setManufacturerCode("BOSCH-999");
        similarDto.setBrand("BOSCH");

        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");
        dto.setCodes(List.of(trwCode));
        dto.setVeiculos(List.of()); // reutiliza aplicações do similar
        dto.setSimilarManufacturerCodes(List.of(similarDto));

        when(apiClient.buscarAplicacoes())
                .thenReturn(List.of(dto));

        // =====================================================
        // ACT
        // =====================================================
        classificationService.importarClassifications();

        // =====================================================
        // ASSERT — PRINCIPAL CRIADO
        // =====================================================
        ProductCodeClassification principal =
                classificationRepository
                        .findByProductCodes_CodeAndProductCodes_CodeType(
                                "TRW-001", CodeType.MANUFACTURER
                        )
                        .orElseThrow();

        // =====================================================
        // ASSERT — PRODUCT CODE
        // =====================================================
        assertThat(principal.getProductCodes())
                .anyMatch(c ->
                        c.getCode().equals("TRW-001") &&
                                c.getCodeType() == CodeType.MANUFACTURER
                );

        // =====================================================
        // ASSERT — VEHICLE MODEL REUTILIZADO DO SIMILAR
        // =====================================================
        assertThat(principal.getApplications())
                .containsExactly(golModel);

        // =====================================================
        // ASSERT — SIMILAR REUTILIZADO
        // =====================================================
        assertThat(principal.getSimilars())
                .contains(bosch);

        assertThat(bosch.getSimilars())
                .contains(principal);

        // =====================================================
        // LOG
        // =====================================================
        System.out.println("Principal: " + principal.getBrand());
        System.out.println("Aplicações: " +
                principal.getApplications().stream()
                        .map(v -> v.getVehicle().getName())
                        .toList());

        System.out.println("Similares do principal: " +
                principal.getSimilars().stream()
                        .map(ProductCodeClassification::getBrand)
                        .toList());

        System.out.println("=== FIM TESTE ===\n");
    }
    @Test
    void importar_deveSincronizarApplicationsECodesEntrePrincipalESimilares_Opcao2() {
        System.out.println("\n=== TESTE: SINCRONIZAÇÃO DE APLICAÇÕES E CODES — OPÇÃO 2 ===");

        // =====================================================
        // ARRANGE — GRUPO / SUBGRUPO
        // =====================================================
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup subgroup = new Subgroup();
        subgroup.setId(10L);
        subgroup.setName("Pastilhas");
        subgroup.setGroup(group);

        when(productGroupRepository.findByName("Freios")).thenReturn(Optional.of(group));
        when(subgroupRepository.findByNameAndGroup("Pastilhas", group)).thenReturn(Optional.of(subgroup));

        // =====================================================
        // ARRANGE — VEÍCULO / APLICAÇÃO EXISTENTE
        // =====================================================
        VehicleBrand vw = new VehicleBrand(null, "VW");
        Vehicle gol = new Vehicle(null, vw, "Gol");

        VehicleModel gol2023 = new VehicleModel();
        gol2023.setName("Gol 1.0 2023");
        gol2023.setVehicle(gol);

        // =====================================================
        // ARRANGE — PRINCIPAL EXISTENTE
        // =====================================================
        ProductCodeClassification principal = new ProductCodeClassification();
        principal.setId(100L);
        principal.setBrand("TRW");
        principal.setSubgroup(subgroup);

        principal.addProductCode(new ProductCode("TRW-001", CodeType.MANUFACTURER, principal));
        principal.addApplication(gol2023);

        when(vehicleModelService.create(any(VehicleModelCreateDTO.class)))
                .thenAnswer(invocation -> {
                    VehicleModelCreateDTO dtoArg = invocation.getArgument(0);
                    VehicleModel vm = new VehicleModel();
                    vm.setName(dtoArg.getModel());  // Preenche corretamente
                    VehicleBrand brand = new VehicleBrand(null, dtoArg.getBrand());
                    vm.setVehicle(new Vehicle(null, brand, dtoArg.getVehicle()));
                    return vm;
                });


        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "TRW-001", CodeType.MANUFACTURER))
                .thenReturn(Optional.of(principal));

        // =====================================================
        // ARRANGE — SIMILAR NÃO EXISTE
        // =====================================================
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "BOSCH-999", CodeType.MANUFACTURER))
                .thenReturn(Optional.empty());

        when(classificationRepository.saveAndFlush(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> {
                    ProductCodeClassification c = invocation.getArgument(0);
                    if (c.getId() == null) c.setId(new Random().nextLong());
                    return c;
                });

        // =====================================================
        // ARRANGE — DTO DE IMPORTAÇÃO
        // =====================================================
        ProductCodeDTO mainCode = new ProductCodeDTO();
        mainCode.setCode("TRW-001");
        mainCode.setCodeType(CodeType.MANUFACTURER);

        ApplicationSimilarDTO similarDto = new ApplicationSimilarDTO();
        similarDto.setManufacturerCode("BOSCH-999");
        similarDto.setBrand("BOSCH");

        VehicleModelCreateDTO newAppDto = new VehicleModelCreateDTO();
        newAppDto.setBrand("TRW");
        newAppDto.setVehicle("Gol");
        newAppDto.setModel("Gol 1.6 2023");
        newAppDto.setYears(List.of(2023));

        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");
        dto.setCodes(List.of(mainCode));
        dto.setVeiculos(List.of(newAppDto));
        dto.setSimilarManufacturerCodes(List.of(similarDto));

        when(apiClient.buscarAplicacoes()).thenReturn(List.of(dto));

        // =====================================================
        // ACT
        // =====================================================
        classificationService.importarClassifications();

        // =====================================================
        // ASSERT — PRINTS SEGURAS
        // =====================================================
        System.out.println("Principal Codes: " +
                safeListToString(principal.getProductCodes(), ProductCode::getCode));
        System.out.println("Principal Applications: " +
                safeListToString(principal.getApplications(), app -> app.getName()));
        System.out.println("Principal Similares: " +
                safeListToString(principal.getSimilars(), sim -> String.valueOf(sim.getManufacturerCode())));

        if (principal.getSimilars() != null && !principal.getSimilars().isEmpty()) {
            ProductCodeClassification similar = principal.getSimilars().iterator().next();

            System.out.println("Similar Codes: " +
                    safeListToString(similar.getProductCodes(), ProductCode::getCode));
            System.out.println("Similar Applications: " +
                    safeListToString(similar.getApplications(), app -> app.getName()));
            System.out.println("Similar Similares: " +
                    safeListToString(similar.getSimilars(), sim -> String.valueOf(sim.getManufacturerCode())));

            // =====================================================
            // ASSERTS — SEGURAS
            // =====================================================
            assertThat(principal.getSimilars()).hasSize(1);
            assertThat(principal.getApplications()).anyMatch(app -> app != null && "Gol 1.6 2023".equals(app.getName()));
            assertThat(similar.getApplications()).anyMatch(app -> app != null && "Gol 1.6 2023".equals(app.getName()));
            assertThat(similar.getProductCodes()).anyMatch(c -> c != null && "BOSCH-999".equals(c.getCode()));
            assertThat(similar.getSimilars()).contains(principal);
            assertThat(principal.getSimilars()).contains(similar);
        }

        System.out.println("=== FIM TESTE ===\n");
    }

    // =====================================================
// MÉTODO AUXILIAR PARA PRINTS SEGURAS
// =====================================================
    private <T> String safeListToString(Collection<T> list, Function<T, String> mapper) {
        if (list == null) return "null";
        return list.stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    try {
                        String value = mapper.apply(item);
                        return value == null ? "null" : value;
                    } catch (Exception e) {
                        return "erro";
                    }
                })
                .toList().toString();
    }

    @Test
    void importar_deveSincronizarApplicationsECodesEntrePrincipalEDoisSimilares() {
        System.out.println("\n=== TESTE: SINCRONIZAÇÃO DE APLICAÇÕES E CODES — DOIS SIMILARES ===");

        // =====================================================
        // ARRANGE — GRUPO / SUBGRUPO
        // =====================================================
        ProductGroup group = new ProductGroup();
        group.setId(1L);
        group.setName("Freios");

        Subgroup subgroup = new Subgroup();
        subgroup.setId(10L);
        subgroup.setName("Pastilhas");
        subgroup.setGroup(group);

        when(productGroupRepository.findByName("Freios")).thenReturn(Optional.of(group));
        when(subgroupRepository.findByNameAndGroup("Pastilhas", group)).thenReturn(Optional.of(subgroup));

        // =====================================================
        // ARRANGE — VEÍCULO / APLICAÇÃO EXISTENTE
        // =====================================================
        VehicleBrand vw = new VehicleBrand(null, "VW");
        Vehicle gol = new Vehicle(null, vw, "Gol");

        VehicleModel gol2023 = new VehicleModel();
        gol2023.setName("Gol 1.0 2023");
        gol2023.setVehicle(gol);

        // =====================================================
        // ARRANGE — PRINCIPAL EXISTENTE
        // =====================================================
        ProductCodeClassification principal = new ProductCodeClassification();
        principal.setId(100L);
        principal.setBrand("TRW");
        principal.setSubgroup(subgroup);

        principal.addProductCode(new ProductCode("TRW-001", CodeType.MANUFACTURER, principal));
        principal.addApplication(gol2023);

        when(vehicleModelService.create(any(VehicleModelCreateDTO.class)))
                .thenAnswer(invocation -> {
                    VehicleModelCreateDTO dtoArg = invocation.getArgument(0);
                    VehicleModel vm = new VehicleModel();
                    vm.setName(dtoArg.getModel());
                    VehicleBrand brand = new VehicleBrand(null, dtoArg.getBrand());
                    vm.setVehicle(new Vehicle(null, brand, dtoArg.getVehicle()));
                    return vm;
                });

        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "TRW-001", CodeType.MANUFACTURER))
                .thenReturn(Optional.of(principal));

        // =====================================================
        // ARRANGE — SIMILARS NÃO EXISTEM
        // =====================================================
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "BOSCH-999", CodeType.MANUFACTURER))
                .thenReturn(Optional.empty());
        when(classificationRepository.findByProductCodes_CodeAndProductCodes_CodeType(
                "BOSCH-888", CodeType.MANUFACTURER))
                .thenReturn(Optional.empty());

        when(classificationRepository.saveAndFlush(any(ProductCodeClassification.class)))
                .thenAnswer(invocation -> {
                    ProductCodeClassification c = invocation.getArgument(0);
                    if (c.getId() == null) c.setId(new Random().nextLong());
                    return c;
                });

        // =====================================================
        // ARRANGE — DTO DE IMPORTAÇÃO
        // =====================================================
        ProductCodeDTO mainCode = new ProductCodeDTO();
        mainCode.setCode("TRW-001");
        mainCode.setCodeType(CodeType.MANUFACTURER);

        ApplicationSimilarDTO similarDto1 = new ApplicationSimilarDTO();
        similarDto1.setManufacturerCode("BOSCH-999");
        similarDto1.setBrand("BOSCH");

        ApplicationSimilarDTO similarDto2 = new ApplicationSimilarDTO();
        similarDto2.setManufacturerCode("BOSCH-888");
        similarDto2.setBrand("BOSCH");

        VehicleModelCreateDTO newAppDto = new VehicleModelCreateDTO();
        newAppDto.setBrand("TRW");
        newAppDto.setVehicle("Gol");
        newAppDto.setModel("Gol 1.6 2023");
        newAppDto.setYears(List.of(2023));

        ApplicationDTO dto = new ApplicationDTO();
        dto.setBrand("TRW");
        dto.setGroup("Freios");
        dto.setSubgroup("Pastilhas");
        dto.setCodes(List.of(mainCode));
        dto.setVeiculos(List.of(newAppDto));
        dto.setSimilarManufacturerCodes(List.of(similarDto1, similarDto2));

        when(apiClient.buscarAplicacoes()).thenReturn(List.of(dto));

        // =====================================================
        // ACT
        // =====================================================
        classificationService.importarClassifications();

        // =====================================================
        // ASSERT — PRINTS SEGURAS
        // =====================================================
        System.out.println("Principal Codes: " +
                safeListToString(principal.getProductCodes(), ProductCode::getCode));
        System.out.println("Principal Applications: " +
                safeListToString(principal.getApplications(), app -> app.getName()));
        System.out.println("Principal Similares: " +
                safeListToString(principal.getSimilars(), sim -> String.valueOf(sim.getManufacturerCode())));

        assertThat(principal.getSimilars()).hasSize(2);
        assertThat(principal.getApplications())
                .anyMatch(app -> app != null && "Gol 1.6 2023".equals(app.getName()));

        for (ProductCodeClassification similar : principal.getSimilars()) {
            System.out.println("Similar Codes: " +
                    safeListToString(similar.getProductCodes(), ProductCode::getCode));
            System.out.println("Similar Applications: " +
                    safeListToString(similar.getApplications(), app -> app.getName()));
            System.out.println("Similar Similares: " +
                    safeListToString(similar.getSimilars(), sim -> String.valueOf(sim.getManufacturerCode())));

            assertThat(similar.getApplications())
                    .anyMatch(app -> app != null && "Gol 1.6 2023".equals(app.getName()));
            assertThat(similar.getSimilars()).contains(principal);
        }

        System.out.println("=== FIM TESTE ===\n");
    }



}
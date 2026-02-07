package com.wholparts.product_service_test;

import com.wholparts.product_service.dto.ProductCreationDTO;
import com.wholparts.product_service.dto.ProductVariantViewDTO;
import com.wholparts.product_service.model.*;
import com.wholparts.product_service.repository.ProductCodeRepository;
import com.wholparts.product_service.repository.ProductVariantRepository;
import com.wholparts.product_service.service.CodeConverterService;
import com.wholparts.product_service.service.ProductService;
import com.wholparts.product_service.service.ProductVariantService;
import com.wholparts.product_service.util.SkuGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductVariantServiceTest {

    @InjectMocks
    private ProductVariantService productVariantService;

    @Mock
    private ProductCreationDTO creationDTO;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductService productService;

    @Mock
    private ProductCodeRepository productCodeRepository;

    @Mock
    private CodeConverterService codeConverterService;

    @Mock
    private SkuGenerator skuGenerator;

    private ProductCodeClassification classification;
    private Product product;
    private ProductCode distributorCode;
    private ProductCode manufacturerCode;
    private ProductCode skuCode;
    private Vehicle vehicle;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // DTO real
        creationDTO = new ProductCreationDTO();
        creationDTO.setDistributorCodeValue("D123");
        creationDTO.setManufacturer("BOSCH");
        creationDTO.setName("Filtro de óleo");

        // 🚗 Veículo
        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setName("Gol");

        // VehicleModel
        VehicleModel model = new VehicleModel("Gol", vehicle);

        // Produto
        product = new Product();
        product.setId(1L);
        product.setName("Filtro de óleo");

        // Código distribuidor
        distributorCode = new ProductCode();
        distributorCode.setCode("D123");
        distributorCode.setCodeType(CodeType.DISTRIBUTOR);

        // Código fabricante
        manufacturerCode = new ProductCode();
        manufacturerCode.setCode("M123");
        manufacturerCode.setCodeType(CodeType.MANUFACTURER);

        // SKU interno
        skuCode = new ProductCode();
        skuCode.setCode("SKU123");
        skuCode.setCodeType(CodeType.INTERNAL);

        // Classification
        classification = new ProductCodeClassification();
        classification.setId(1L);
        classification.setBrand("Classificação Teste");
        classification.addProductCode(distributorCode);
        classification.addProductCode(manufacturerCode);
        classification.addApplication(model);

        // 🔹 Mocks
        when(productService.getOrCreate(any())).thenReturn(product);
        when(productCodeRepository.findByCodeAndCodeType("D123", CodeType.DISTRIBUTOR))
                .thenReturn(Optional.of(distributorCode));
        when(codeConverterService.convertDistributorToManufacturer("D123"))
                .thenReturn(Optional.of(manufacturerCode));
        when(skuGenerator.generate(anyString(), anyString(), anyString()))
                .thenReturn(skuCode);
        when(productVariantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void create_deveCriarVariantComSucesso() {
        ProductVariantViewDTO dto = productVariantService.create(creationDTO);

        assertNotNull(dto);
        assertEquals("M123", dto.getManufacturerCode());
        assertEquals("SKU123", dto.getSku());
        assertEquals("Filtro de óleo", dto.getProductName());
        assertFalse(dto.getVehicleModels().isEmpty());
        assertTrue(dto.getDistributorCodes().contains("D123"));

        verify(productCodeRepository).save(skuCode);
        verify(productVariantRepository).save(any());
    }

    @Test
    void findById_deveRetornarDTO() {
        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .product(product)
                .classification(classification)
                .sku(skuCode)
                .active(true)
                .build();

        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));

        ProductVariantViewDTO dto = productVariantService.findById(1L);

        assertNotNull(dto);
        assertEquals("M123", dto.getManufacturerCode());
    }

    @Test
    void findAll_deveRetornarListaDTO() {
        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .product(product)
                .classification(classification)
                .sku(skuCode)
                .active(true)
                .build();

        when(productVariantRepository.findAll()).thenReturn(List.of(variant));

        List<ProductVariantViewDTO> list = productVariantService.findAll();

        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("M123", list.get(0).getManufacturerCode());
    }

    @Test
    void findBySku_deveRetornarDTO() {
        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .product(product)
                .classification(classification)
                .sku(skuCode)
                .active(true)
                .build();

        when(productCodeRepository.findByCodeAndCodeType("SKU123", CodeType.INTERNAL))
                .thenReturn(Optional.of(skuCode));
        when(productVariantRepository.findBySku(skuCode))
                .thenReturn(Optional.of(variant));

        ProductVariantViewDTO dto = productVariantService.findBySku("SKU123");

        assertNotNull(dto);
        assertEquals("M123", dto.getManufacturerCode());
    }

    @Test
    void activate_desactivate_deveMudarStatus() {
        ProductVariant variant = ProductVariant.builder()
                .id(1L)
                .product(product)
                .classification(classification)
                .sku(skuCode)
                .active(false)
                .build();

        when(productVariantRepository.findById(1L)).thenReturn(Optional.of(variant));

        productVariantService.activate(1L);
        assertTrue(variant.getActive());

        productVariantService.desactivate(1L);
        assertFalse(variant.getActive());
    }
}

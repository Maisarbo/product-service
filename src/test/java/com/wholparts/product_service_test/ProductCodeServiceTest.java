package com.wholparts.product_service_test;


import com.wholparts.product_service.dto.ProductVariantViewDTO;
import com.wholparts.product_service.model.*;
import com.wholparts.product_service.repository.ProductCodeRepository;
import com.wholparts.product_service.repository.ProductVariantRepository;
import com.wholparts.product_service.service.ProductCodeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductCodeServiceTest {

    @Mock
    private ProductCodeRepository productCodeRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private ProductCodeService productCodeService;

    private ProductCodeClassification classification;
    private ProductCode manufacturerCode;
    private ProductCode distributorCode;
    private ProductVariant variant;
    private Product product;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Produto
        product = new Product();
        product.setId(1L);
        product.setName("Produto Teste");
        product.setNcm("12345678");

        // Classificação
        classification = new ProductCodeClassification();
        classification.setId(1L);

        // Códigos
        manufacturerCode = new ProductCode("MANU-001", CodeType.MANUFACTURER, classification);
        distributorCode = new ProductCode("DIST-001", CodeType.DISTRIBUTOR, classification);

        classification.setProductCodes(Set.of(manufacturerCode, distributorCode));

        // Variant
        variant = new ProductVariant();
        variant.setId(10L);
        variant.setSku(new ProductCode("SKU-001", CodeType.INTERNAL, classification));
        variant.setProduct(product);
        variant.setClassification(classification);
        variant.setActive(true);
    }

    @Test
    void findCode_deveRetornarProductCode() {
        when(productCodeRepository.findByCode("MANU-001"))
                .thenReturn(Optional.of(manufacturerCode));

        Object result = productCodeService.findCode("MANU-001");

        assertTrue(result instanceof ProductCode);
        assertEquals("MANU-001", ((ProductCode) result).getCode());
    }

    @Test
    void findCodesByClassification_deveRetornarLista() {
        when(productCodeRepository.findAllByClassificationId(1L))
                .thenReturn(List.of(manufacturerCode, distributorCode));

        List<ProductCode> codes = productCodeService.findCodesByClassification(1L);

        assertEquals(2, codes.size());
        assertTrue(codes.contains(manufacturerCode));
        assertTrue(codes.contains(distributorCode));
    }

    @Test
    void findCodesByType_deveFiltrarPorTipo() {
        when(productCodeRepository.findAllByClassificationId(1L))
                .thenReturn(List.of(manufacturerCode, distributorCode));

        List<String> manufacturerCodes = productCodeService.findCodesByType(1L, CodeType.MANUFACTURER);

        assertEquals(1, manufacturerCodes.size());
        assertEquals("MANU-001", manufacturerCodes.get(0));
    }

    @Test
    void codeExists_deveRetornarTrueQuandoExistir() {
        when(productCodeRepository.existsByCode("MANU-001")).thenReturn(true);

        boolean exists = productCodeService.codeExists("MANU-001");

        assertTrue(exists);
    }

    @Test
    void createCode_deveSalvarNovoCodigo() {
        when(productCodeRepository.existsByCode("NEW-001")).thenReturn(false);
        ProductCode newCode = new ProductCode("NEW-001", CodeType.DISTRIBUTOR, classification);
        when(productCodeRepository.save(any(ProductCode.class))).thenReturn(newCode);

        ProductCode result = productCodeService.createCode("NEW-001", CodeType.DISTRIBUTOR, classification);

        assertNotNull(result);
        assertEquals("NEW-001", result.getCode());
    }

    @Test
    void deleteCode_deveChamarDelete() {
        when(productCodeRepository.findById(1L)).thenReturn(Optional.of(manufacturerCode));

        productCodeService.deleteCode(1L);

        verify(productCodeRepository, times(1)).delete(manufacturerCode);
    }

    @Test
    void findVariantByAnyCode_deveRetornarDTO() {
        when(productCodeRepository.findByCode("MANU-001")).thenReturn(Optional.of(manufacturerCode));
        when(productVariantRepository.findByClassification(classification)).thenReturn(Optional.of(variant));

        ProductVariantViewDTO dto = productCodeService.findVariantByAnyCode("MANU-001");

        assertNotNull(dto);
        assertEquals(variant.getId(), dto.getVariantId());
        assertEquals(product.getId(), dto.getProductId());
        assertEquals("Produto Teste", dto.getProductName());
        assertEquals("MANU-001", dto.getManufacturerCode());
        assertTrue(dto.getDistributorCodes().contains("DIST-001"));
        assertEquals("SKU-001", dto.getSku());
    }

    @Test
    void findVariantByAnyCode_deveLancarExceptionSeNaoExistirCodigo() {
        when(productCodeRepository.findByCode("INVALID")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                productCodeService.findVariantByAnyCode("INVALID")
        );
    }

    @Test
    void findVariantByAnyCode_deveLancarExceptionSeVariantNaoExistir() {
        when(productCodeRepository.findByCode("MANU-001")).thenReturn(Optional.of(manufacturerCode));
        when(productVariantRepository.findByClassification(classification)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                productCodeService.findVariantByAnyCode("MANU-001")
        );
    }
}

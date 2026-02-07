package com.wholparts.product_service.service;

import com.wholparts.product_service.dto.ProductCreationDTO;
import com.wholparts.product_service.dto.ProductViewDTO;
import com.wholparts.product_service.model.Product;
import com.wholparts.product_service.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private NcmCestService ncmCestService;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateProduct() {
        ProductCreationDTO dto = new ProductCreationDTO();
        dto.setName("Filtro de Óleo");
        dto.setNcm("12345678");
        dto.setDescription("Filtro");

        when(ncmCestService.getCestByNcm(dto.getNcm())).thenReturn("123456");
        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductViewDTO result = productService.create(dto);

        assertNotNull(result);
        assertEquals("Filtro de Óleo", result.getName());
        assertEquals("123456", result.getCest());

        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldGetOrCreateExistingProduct() {
        ProductCreationDTO dto = new ProductCreationDTO();
        dto.setName("Pastilha Freio");
        dto.setNcm("999");

        Product product = new Product();
        product.setId(1L);

        when(productRepository.findByNameAndNcm(any(), eq("999")))
                .thenReturn(Optional.of(product));

        Product result = productService.getOrCreate(dto);

        assertEquals(1L, result.getId());
        verify(productRepository, never()).save(any());
    }
}

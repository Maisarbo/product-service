package com.wholparts.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wholparts.product_service.dto.ProductCreationDTO;
import com.wholparts.product_service.dto.ProductViewDTO;
import com.wholparts.product_service.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldCreateProduct() throws Exception {

        ProductCreationDTO dto = new ProductCreationDTO();
        dto.setName("Amortecedor");

        ProductViewDTO response = new ProductViewDTO();
        response.setId(1L);
        response.setName("Amortecedor");

        when(productService.create(any())).thenReturn(response);

        mockMvc.perform(post("/products")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Amortecedor"));
    }
}

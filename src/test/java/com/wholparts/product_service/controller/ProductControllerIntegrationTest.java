package com.wholparts.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wholparts.product_service.dto.ProductCreationDTO;
import com.wholparts.product_service.model.Unit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateProduct() throws Exception {
        // Cria o objeto Java
        ProductCreationDTO dto = new ProductCreationDTO();
        dto.setName("Filtro de Óleo");
        dto.setDescription("Filtro motor");
        dto.setNcm("84212300");
        dto.setCest("0100100");
        dto.setUnit(Unit.UN);

        // Converte para JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(dto);

        mockMvc.perform(post("/products")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Filtro de Óleo"));
    }

}

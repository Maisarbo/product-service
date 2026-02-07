package com.wholparts.product_service.dto;

import com.wholparts.product_service.model.CodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductCodeDTO {

    @NotBlank
    private String code;

    @NotNull
    private CodeType codeType;
}


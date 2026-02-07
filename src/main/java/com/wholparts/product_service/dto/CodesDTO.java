package com.wholparts.product_service.dto;

import com.wholparts.product_service.model.CodeType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodesDTO {
    private String code;
    private CodeType codeType;
}

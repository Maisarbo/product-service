package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductCodeClassificationRepository extends JpaRepository<ProductCodeClassification, Long> {

    /**
     * Valida se já existe classification para aquele fabricante
     */
    Optional<ProductCodeClassification>
    findByProductCodes_CodeAndProductCodes_CodeType(
            String code,
            CodeType codeType
    );

    boolean
    existsByProductCodes_CodeAndProductCodes_CodeType(
            String code,
            CodeType codeType
    );


    List<ProductCodeClassification>
    findAllByProductCodes_CodeAndProductCodes_CodeType(
            @NotBlank String code,
            CodeType codeType
    );

    List<ProductCodeClassification> findAllBySubgroup(Subgroup subgroup);
}



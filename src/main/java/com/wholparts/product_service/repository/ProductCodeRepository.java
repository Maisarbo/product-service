package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.CodeType;
import com.wholparts.product_service.model.ProductCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductCodeRepository extends JpaRepository<ProductCode, Long> {


    Optional<ProductCode> findByCode(String code);


    Optional<ProductCode> findByCodeAndCodeType(String code, CodeType codeType);

    List<ProductCode> findAllByClassificationId(Long classificationId);

    boolean existsByCode(String code);
}


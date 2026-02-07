package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.*;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findBySku(ProductCode sku);

    List<ProductVariant> findAllByClassification(ProductCodeClassification classification);

    List<ProductVariant> findAllByClassificationId(Long classificationId);

    List<ProductVariant> findAllByProductId(Long productId);

    Optional<ProductVariant> findByClassification(
            ProductCodeClassification classification
    );

    List<ProductVariant> findAllByActiveTrue();

}




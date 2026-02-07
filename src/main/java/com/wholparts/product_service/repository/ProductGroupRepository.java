package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.ProductGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductGroupRepository extends JpaRepository<ProductGroup, Long> {
    Optional<ProductGroup> findByName(String name);
}

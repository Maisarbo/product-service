package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.SkuSequence;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkuSequenceRepository extends JpaRepository<SkuSequence, Long> {
    Optional<SkuSequence> findByPrefix(String prefix);
}

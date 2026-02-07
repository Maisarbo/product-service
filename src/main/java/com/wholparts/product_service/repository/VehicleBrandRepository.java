package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.VehicleBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VehicleBrandRepository extends JpaRepository<VehicleBrand, Long> {
    Optional<VehicleBrand> findByName(String name);
}

package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.VehicleModel;
import com.wholparts.product_service.model.YearModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YearModelRepository extends JpaRepository<YearModel, Long> {

    Optional<YearModel> findByYear(Integer ano);

}

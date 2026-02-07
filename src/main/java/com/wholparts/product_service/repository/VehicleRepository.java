package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.Vehicle;
import com.wholparts.product_service.model.VehicleBrand;
import com.wholparts.product_service.model.YearModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByNameAndBrand(String vehicleName, VehicleBrand brand);

}

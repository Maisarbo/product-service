package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.Vehicle;
import com.wholparts.product_service.model.VehicleBrand;
import com.wholparts.product_service.model.VehicleModel;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleModelRepository extends JpaRepository<VehicleModel, Long> {

    Optional<VehicleModel> findByModelAndVehicle(String modelName, Vehicle vehicle);

    Optional<VehicleModel> findByNameAndVehicle(@NotBlank String model, Vehicle vehicle);
}

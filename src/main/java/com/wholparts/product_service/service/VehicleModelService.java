package com.wholparts.product_service.service;

import com.wholparts.product_service.dto.VehicleModelCreateDTO;
import com.wholparts.product_service.model.Vehicle;
import com.wholparts.product_service.model.VehicleBrand;
import com.wholparts.product_service.model.VehicleModel;
import com.wholparts.product_service.model.YearModel;
import com.wholparts.product_service.repository.VehicleBrandRepository;
import com.wholparts.product_service.repository.VehicleModelRepository;
import com.wholparts.product_service.repository.VehicleRepository;
import com.wholparts.product_service.repository.YearModelRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VehicleModelService {

    private final VehicleBrandRepository brandRepository;
    private final VehicleRepository vehicleRepository;
    private final VehicleModelRepository modelRepository;
    private final YearModelRepository yearRepository;

    public VehicleModel create(VehicleModelCreateDTO dto) {

        VehicleBrand brand = getOrCreateBrand(dto.getBrand());

        Vehicle vehicle = getOrCreateVehicle(
                dto.getVehicle(),
                brand
        );

        VehicleModel model = modelRepository
                .findByNameAndVehicle(dto.getModel(), vehicle)
                .orElseGet(() -> {
                    VehicleModel m = new VehicleModel(dto.getModel(), vehicle);
                    return modelRepository.save(m);
                });

        for (Integer y : dto.getYears()) {
            YearModel year = getOrCreateYear(y);

            if (!model.getYears().contains(year)) {
                model.getYears().add(year);
            }
        }

        return modelRepository.save(model);
    }

    public VehicleModel findById(Long id) {
        return modelRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("VehicleModel não encontrado: " + id)
                );
    }

    /* ================= AUXILIARES ================= */

    private VehicleBrand getOrCreateBrand(String name) {
        return brandRepository.findByName(name)
                .orElseGet(() -> {
                    VehicleBrand b = new VehicleBrand();
                    b.setName(name);
                    return brandRepository.save(b);
                });
    }

    private Vehicle getOrCreateVehicle(String name, VehicleBrand brand) {
        return vehicleRepository.findByNameAndBrand(name, brand)
                .orElseGet(() -> {
                    Vehicle v = new Vehicle();
                    v.setName(name);
                    v.setBrand(brand);
                    return vehicleRepository.save(v);
                });
    }

    private YearModel getOrCreateYear(Integer year) {
        return yearRepository.findByYear(year)
                .orElseGet(() -> {
                    YearModel y = new YearModel();
                    y.setYear(year);
                    return yearRepository.save(y);
                });
    }
}


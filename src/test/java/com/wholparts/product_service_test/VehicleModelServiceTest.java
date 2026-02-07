package com.wholparts.product_service_test;

import com.wholparts.product_service.dto.VehicleModelCreateDTO;
import com.wholparts.product_service.model.Vehicle;
import com.wholparts.product_service.model.VehicleBrand;
import com.wholparts.product_service.model.VehicleModel;
import com.wholparts.product_service.model.YearModel;
import com.wholparts.product_service.repository.VehicleBrandRepository;
import com.wholparts.product_service.repository.VehicleModelRepository;
import com.wholparts.product_service.repository.VehicleRepository;
import com.wholparts.product_service.repository.YearModelRepository;
import com.wholparts.product_service.service.VehicleModelService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class VehicleModelServiceTest {

    @InjectMocks
    private VehicleModelService vehicleModelService;

    @Mock
    private VehicleBrandRepository brandRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private VehicleModelRepository modelRepository;

    @Mock
    private YearModelRepository yearRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // =====================================================
    // TESTE: CRIAR UM NOVO VEHICLEMODEL COMPLETO
    // =====================================================
    @Test
    void create_deveCriarBrandVehicleModelEYearQuandoNaoExistem() {
        // ARRANGE
        VehicleModelCreateDTO dto = new VehicleModelCreateDTO();
        dto.setBrand("VW");
        dto.setVehicle("Gol");
        dto.setModel("Gol 1.0 2023");
        dto.setYears(List.of(2023, 2024));

        // MOCKS: brandRepository
        when(brandRepository.findByName("VW")).thenReturn(Optional.empty());
        when(brandRepository.save(any(VehicleBrand.class))).thenAnswer(i -> i.getArgument(0));

        // MOCKS: vehicleRepository
        when(vehicleRepository.findByNameAndBrand(anyString(), any(VehicleBrand.class))).thenReturn(Optional.empty());
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArgument(0));

        // MOCKS: modelRepository
        when(modelRepository.findByNameAndVehicle(anyString(), any(Vehicle.class))).thenReturn(Optional.empty());
        when(modelRepository.save(any(VehicleModel.class))).thenAnswer(i -> i.getArgument(0));

        // MOCKS: yearRepository
        when(yearRepository.findByYear(2023)).thenReturn(Optional.empty());
        when(yearRepository.findByYear(2024)).thenReturn(Optional.empty());
        when(yearRepository.save(any(YearModel.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        VehicleModel result = vehicleModelService.create(dto);

        // ASSERT
        assertThat(result.getName()).isEqualTo("Gol 1.0 2023");
        assertThat(result.getVehicle().getName()).isEqualTo("Gol");
        assertThat(result.getVehicle().getBrand().getName()).isEqualTo("VW");
        assertThat(result.getYears()).extracting(YearModel::getYear).containsExactlyInAnyOrder(2023, 2024);

        // VERIFY INTERAÇÕES
        verify(brandRepository).save(any(VehicleBrand.class));
        verify(vehicleRepository).save(any(Vehicle.class));
        verify(modelRepository, times(2)).save(any(VehicleModel.class)); // 1x ao criar, 1x no final
        verify(yearRepository, times(2)).save(any(YearModel.class));
    }

    // =====================================================
    // TESTE: USANDO BRAND E VEHICLE EXISTENTES
    // =====================================================
    @Test
    void create_deveReusarBrandEVehicleExistentes() {
        // ARRANGE
        VehicleBrand brand = new VehicleBrand();
        brand.setName("VW");

        Vehicle vehicle = new Vehicle();
        vehicle.setName("Gol");
        vehicle.setBrand(brand);

        VehicleModelCreateDTO dto = new VehicleModelCreateDTO();
        dto.setBrand("VW");
        dto.setVehicle("Gol");
        dto.setModel("Gol 1.0 2023");
        dto.setYears(List.of(2023));

        when(brandRepository.findByName("VW")).thenReturn(Optional.of(brand));
        when(vehicleRepository.findByNameAndBrand("Gol", brand)).thenReturn(Optional.of(vehicle));

        VehicleModel model = new VehicleModel();
        model.setName("Gol 1.0 2023");
        model.setVehicle(vehicle);

        when(modelRepository.findByNameAndVehicle("Gol 1.0 2023", vehicle)).thenReturn(Optional.of(model));
        when(yearRepository.findByYear(2023)).thenReturn(Optional.empty());
        when(yearRepository.save(any(YearModel.class))).thenAnswer(i -> i.getArgument(0));
        when(modelRepository.save(any(VehicleModel.class))).thenAnswer(i -> i.getArgument(0));

        // ACT
        VehicleModel result = vehicleModelService.create(dto);

        // ASSERT
        assertThat(result.getVehicle()).isSameAs(vehicle);
        assertThat(result.getName()).isEqualTo("Gol 1.0 2023");
        assertThat(result.getYears()).extracting(YearModel::getYear).containsExactly(2023);

        // VERIFY
        verify(brandRepository, never()).save(any());
        verify(vehicleRepository, never()).save(any());
        verify(modelRepository).save(any(VehicleModel.class));
        verify(yearRepository).save(any(YearModel.class));
    }

    // =====================================================
    // TESTE: findById - ENTITY NOT FOUND
    // =====================================================
    @Test
    void findById_deveLancarExceptionQuandoNaoEncontrado() {
        when(modelRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> vehicleModelService.findById(999L));
    }
}


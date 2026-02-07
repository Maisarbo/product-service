package com.wholparts.product_service.service;


import com.wholparts.product_service.dto.CodesDTO;
import com.wholparts.product_service.model.*;
import com.wholparts.product_service.repository.ProductCodeRepository;
import com.wholparts.product_service.util.ApiCodeClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CodeConverterService {

    private final ProductCodeRepository productCodeRepository;


    private final ApiCodeClient apiCodeClient;

    /**
     * Converte um código de distribuidor para o ProductCode do fabricante correspondente
     * do mesmo produto.
     *
     * @param distributorCodeValue Código do distribuidor
     * @return ProductCode do fabricante se existir
     */
    public Optional<ProductCode> convertDistributorToManufacturer(String distributorCodeValue) {

        return productCodeRepository
                .findByCodeAndCodeType(distributorCodeValue, CodeType.DISTRIBUTOR)
                .map(ProductCode::getClassification)
                .flatMap(classification ->
                        classification.getProductCodes().stream()
                                .filter(code -> code.getCodeType() == CodeType.MANUFACTURER)
                                .findFirst()
                );
    }



}

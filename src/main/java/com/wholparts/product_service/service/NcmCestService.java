package com.wholparts.product_service.service;

import com.wholparts.product_service.model.NcmCest;
import com.wholparts.product_service.repository.NcmCestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NcmCestService {

    private final NcmCestRepository repository;

    public String getCestByNcm(String ncm) {
        NcmCest ncmCest = repository.findByNcm(ncm);
        return (ncmCest != null) ? ncmCest.getCest() : null;
    }
}

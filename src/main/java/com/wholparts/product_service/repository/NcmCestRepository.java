package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.NcmCest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NcmCestRepository extends JpaRepository<NcmCest, String> {
    NcmCest findByNcm(String ncm);
}

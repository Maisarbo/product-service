package com.wholparts.product_service.util;

import com.wholparts.product_service.dto.ApplicationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "applicationApi", url = "http://api-externa.com")
public interface ApplicationApiClient {

    @GetMapping("/aplicacoes")
    List<ApplicationDTO> buscarAplicacoes();
}

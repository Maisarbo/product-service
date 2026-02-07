package com.wholparts.product_service.util;


import com.wholparts.product_service.dto.CodesDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "apiCodeClient", url = "http://api-externa.com")
public interface ApiCodeClient {

    @GetMapping("/codigos")
    List<CodesDTO> buscarCodigos();
}
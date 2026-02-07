package com.wholparts.product_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class NcmCest {

    @Id
    private String ncm;   // Exemplo: "87083090"

    private String cest;  // Exemplo: "0100500"

    private String description; // Ex: "Freios e suas partes"
}
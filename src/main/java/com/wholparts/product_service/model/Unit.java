package com.wholparts.product_service.model;

public enum Unit {
    PC("Peça"),
    CX("Caixa"),
    LT("Litro"),
    KG("Quilo"),
    MT("Metro"),
    UN("Unidade");

    private final String description;

    Unit(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

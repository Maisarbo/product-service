package com.wholparts.product_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.context.MessageSourceResolvable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "product_code")
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProductCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String code; // O valor do código (distribuidor, montadora, interno...)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CodeType codeType; // Tipo do código (DISTRIBUTOR, MANUFACTURER, INTERNAL)

    @ManyToOne(optional = false)
    private ProductCodeClassification classification;

    public ProductCode(String code, CodeType codeType) {
        this.code = code;
        this.codeType = codeType;
    }

    public ProductCode(String code, CodeType codeType, ProductCodeClassification classification) {
        this.code = code;
        this.codeType = codeType;
        this.classification = classification;
    }

    public ProductCode() {

    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProductCode that = (ProductCode) o;
        return Objects.equals(code, that.code) && codeType == that.codeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, codeType);
    }

    @Override
    public String toString() {
        return "ProductCode{" +
                "code='" + code + '\'' +
                '}';
    }
}

package com.wholparts.product_service.util;

import com.wholparts.product_service.model.CodeType;
import com.wholparts.product_service.model.ProductCode;
import com.wholparts.product_service.model.SkuSequence;
import com.wholparts.product_service.repository.SkuSequenceRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
public class SkuGenerator {

    private final SkuSequenceRepository sequenceRepository;

    /**
     * Abreviações de MONTADORAS (não fabricantes de peças)
     */
    private static final Map<String, String> BRAND_ABBREVIATIONS = Map.ofEntries(
            Map.entry("FIAT", "FT"),
            Map.entry("VOLKSWAGEN", "VW"),
            Map.entry("CHEVROLET", "GM"),
            Map.entry("FORD", "FD"),
            Map.entry("TOYOTA", "TY"),
            Map.entry("HONDA", "HD"),
            Map.entry("HYUNDAI", "HY"),
            Map.entry("RENAULT", "RN"),
            Map.entry("PEUGEOT", "PG"),
            Map.entry("CITROEN", "CT"),
            Map.entry("GENERIC", "GN")
    );

    public SkuGenerator(SkuSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    /**
     * Gera ProductCode do tipo INTERNAL (SKU)
     */
    @Transactional
    public ProductCode generate(
            String vehicleBrand,
            String distributorCode,
            String manufacturerCode
    ) {

        if (vehicleBrand == null || distributorCode == null || manufacturerCode == null) {
            throw new IllegalArgumentException("Parâmetros obrigatórios para geração do SKU.");
        }

        String brandKey = vehicleBrand.toUpperCase();
        String brandAbbreviation = BRAND_ABBREVIATIONS.get(brandKey);

        if (brandAbbreviation == null) {
            throw new IllegalArgumentException(
                    "Abreviação não encontrada para a montadora: " + vehicleBrand
            );
        }

        String prefix = String.format(
                "%s-%s-%s",
                brandAbbreviation,
                distributorCode.toUpperCase(),
                manufacturerCode.toUpperCase()
        );

        SkuSequence sequence = sequenceRepository
                .findByPrefix(prefix)
                .orElseGet(() -> {
                    SkuSequence s = new SkuSequence();
                    s.setPrefix(prefix);
                    s.setLastNumber(0);
                    return s;
                });

        int nextNumber = sequence.getLastNumber() + 1;
        sequence.setLastNumber(nextNumber);
        sequenceRepository.save(sequence);

        String skuValue = prefix + "-" + String.format("%02d", nextNumber);

        // 🔹 Aqui nasce o ProductCode
        ProductCode skuCode = new ProductCode();
        skuCode.setCode(skuValue);
        skuCode.setCodeType(CodeType.INTERNAL);


        return skuCode;
    }
}

package com.wholparts.product_service.service;

import com.opencsv.CSVReader;
import com.wholparts.product_service.model.NcmCest;
import com.wholparts.product_service.repository.NcmCestRepository;
import org.springframework.stereotype.Service;
import java.io.FileReader;

@Service
public class NcmCestLoader {

    private final NcmCestRepository repository;

    public NcmCestLoader(NcmCestRepository repository) {
        this.repository = repository;
    }

    public void loadFromCsv(String filePath) throws Exception {
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                String ncm = line[0];
                String cest = line[1];
                String description = line[2];

                NcmCest entity = new NcmCest();
                entity.setNcm(ncm);
                entity.setCest(cest);
                entity.setDescription(description);

                repository.save(entity);
            }
        }
    }
}


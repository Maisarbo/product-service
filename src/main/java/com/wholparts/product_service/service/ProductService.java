package com.wholparts.product_service.service;

import com.wholparts.product_service.dto.*;
import com.wholparts.product_service.model.*;

import com.wholparts.product_service.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.apache.tomcat.util.http.RequestUtil.normalize;


@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final NcmCestService ncmCestService;


    public ProductViewDTO create(ProductCreationDTO dto) {

        String cest = ncmCestService.getCestByNcm(dto.getNcm());

        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .unit(dto.getUnit())
                .ncm(dto.getNcm())
                .ncmDescription(dto.getNcmDescription())
                .cest(cest)
                .active(true)
                .build();



        product = productRepository.save(product);

        return toViewDTO(product);
    }





    public ProductViewDTO toViewDTO(Product product) {
        ProductViewDTO dto = new ProductViewDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setUnit(product.getUnit() != null ? product.getUnit().name() : null);
        dto.setNcm(product.getNcm());
        dto.setCest(product.getCest());
        dto.setActive(product.getActive());
        dto.setNcmDescription(product.getNcmDescription());
        dto.setGtin(product.getGtin());

        return dto;
    }


    public Product getOrCreate(ProductCreationDTO dto) {

        return productRepository
                .findByNameAndNcm(dto.getName(), dto.getNcm())
                .orElseGet(() -> {
                    // Cria o produto e seta explicitamente os campos
                    Product product = Product.builder()
                            .name(dto.getName())
                            .description(dto.getDescription())
                            .ncm(dto.getNcm())
                            .cest(dto.getCest() != null ? dto.getCest() : "") // evita null
                            .gtin(dto.getGtin())
                            .unit(dto.getUnit())
                            .active(false)
                            .autoCreated(true)
                            .build();

                    return productRepository.save(product);
                });
    }



    @Transactional(readOnly = true)
    public ProductViewDTO findById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Produto não encontrado")
                );

        return toViewDTO(product);
    }


    @Transactional
    public void activate(Long id) {
        Product product = getEntityById(id);
        product.setActive(true);
    }

    @Transactional(readOnly = true)
    public Product getEntityById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Produto não encontrado: " + id)
                );
    }


    @Transactional
    public void desactivate(Long id) {
        Product product = getEntityById(id);
        product.setActive(false);
    }


    @Transactional
    public ProductViewDTO update(Long id, ProductUpdateDTO dto) {

        Product product = getEntityById(id);

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setGtin(dto.getGtin());

        // NCM só muda se permitido
        if (!product.getNcm().equals(dto.getNcm())) {
            product.setNcm(dto.getNcm());
            product.setCest(ncmCestService.getCestByNcm(dto.getNcm()));
        }

        return toViewDTO(product);
    }


    public List<ProductViewDTO> findAllActive() {
        return productRepository.findAllByActiveTrue()
                .stream()
                .map(this::toViewDTO)
                .toList();
    }
    public List<ProductViewDTO> findAll() {
        return productRepository.findAll()
                .stream()
                .map(this::toViewDTO)
                .toList();
    }

    public List<ProductViewDTO> findAutoCreated() {
        return productRepository.findAllByAutoCreatedTrue()
                .stream()
                .map(this::toViewDTO)
                .toList();
    }



}

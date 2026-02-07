package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByNameAndNcm(String name, String ncm);


    List<Product> findAllByActiveTrue();

    List<Product> findAllByAutoCreatedTrue();

}

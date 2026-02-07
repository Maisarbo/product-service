package com.wholparts.product_service.repository;

import com.wholparts.product_service.model.ProductGroup;
import com.wholparts.product_service.model.Subgroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubgroupRepository extends JpaRepository<Subgroup, Long> {

    Optional<Subgroup> findByName(String name);


    Optional<Subgroup> findByNameAndGroup(String subgroupName, ProductGroup group);
}

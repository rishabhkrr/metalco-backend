package com.indona.invento.dao;


import com.indona.invento.entities.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    boolean existsByBrandName(String brandName);
}


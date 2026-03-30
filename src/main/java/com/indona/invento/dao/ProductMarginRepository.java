package com.indona.invento.dao;

import com.indona.invento.entities.ProductMargin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductMarginRepository extends JpaRepository<ProductMargin, Long> {

    @Query(value = "SELECT TOP 1 * FROM product_margin WHERE material_type = :materialType AND product_category = :productCategory", nativeQuery = true)
    Optional<ProductMargin> findByMaterialTypeAndProductCategory(@Param("materialType") String materialType, @Param("productCategory") String productCategory);
}

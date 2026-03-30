package com.indona.invento.dao;


import com.indona.invento.entities.ItemMasterEntity;
import com.indona.invento.entities.StockSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ItemMasterRepository extends JpaRepository<ItemMasterEntity, Long> {

    Optional<ItemMasterEntity> findBySkuDescriptionIgnoreCase(String skuDescription);

    @Query("SELECT i FROM ItemMasterEntity i WHERE LOWER(i.skuDescription) LIKE LOWER(CONCAT('%', :skuDescription, '%'))")
    List<ItemMasterEntity> findSimilarSku(@Param("skuDescription") String skuDescription);

    @Query("SELECT DISTINCT i.productCategory FROM ItemMasterEntity i")
    List<String> findDistinctCategories();

    @Query("SELECT i.skuDescription FROM ItemMasterEntity i WHERE LOWER(i.productCategory) = LOWER(:category)")
    List<String> findDescriptionsByCategory(@Param("category") String category);

    List<ItemMasterEntity> findBySupplierCodeAndSkuDescription(String trim, String trim1);
}


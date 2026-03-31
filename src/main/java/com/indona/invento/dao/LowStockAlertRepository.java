package com.indona.invento.dao;

import com.indona.invento.entities.LowStockAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LowStockAlertRepository extends JpaRepository<LowStockAlertEntity, Long> {

    boolean existsByItemDescriptionAndUnitAndMaterialTypeAndBrand( String itemDescription, String unit, String materialType, String brand );

    @Query("SELECT a FROM LowStockAlertEntity a " + "WHERE a.unit = :unit AND a.itemDescription IN :itemDescs") List<LowStockAlertEntity> findAllByUnitAndItemDescriptions(@Param("unit") String unit, @Param("itemDescs") List<String> itemDescs );
}

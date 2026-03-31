package com.indona.invento.dao;

import com.indona.invento.dto.DeliveryChallanItemDetailsDTO;
import com.indona.invento.entities.DeliveryChallanJWEntity;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryChallanJWRepository extends JpaRepository<DeliveryChallanJWEntity, Long> {


    // Fetch latest MEDC number for the current month (native SQL uses column name medc_number)
    @Query(value = "SELECT TOP 1 medc_number FROM delivery_challan_jw WHERE medc_number LIKE ?1 ORDER BY medc_number DESC", nativeQuery = true)
    String findLatestMedcNumberLike(String prefix);

    DeliveryChallanJWEntity findByMedcNumber(String medcNumber);
    
    @Query("SELECT d FROM DeliveryChallanJWEntity d WHERE d.soNumber = :soNumber AND d.lineNumber = :lineNumber")
    List<DeliveryChallanJWEntity> findBySoNumberAndLineNumber(String soNumber, String lineNumber);
    @Query("SELECT d.medcNumber FROM DeliveryChallanJWEntity d")
    List<String> findAllMedcNumbers();
    
    List<DeliveryChallanJWEntity> findByMedcNumberIn(List<String> medcNumbers);
    
    List<DeliveryChallanJWEntity> findAllByMedcNumber(String medcNumber);
    
    @Query("SELECT d.dimension FROM DeliveryChallanJWEntity d WHERE d.medcNumber = :medcNumber")
    List<String> findDimensionsByMedcNumber(@Param("medcNumber") String medcNumber);
    
    @Query("SELECT new com.indona.invento.dto.DeliveryChallanItemDetailsDTO(" +
            "d.itemDescription, d.orderType, d.productCategory, d.brand, d.grade, " +
            "d.temper, d.dimension, d.quantityKg, d.uomKg, d.quantityNo, d.uomNo) " +
            "FROM DeliveryChallanJWEntity d " +
            "WHERE d.medcNumber = :medcNumber AND d.dimension = :dimension")
    List<DeliveryChallanItemDetailsDTO> findItemDetailsByMedcAndDimension(
            @Param("medcNumber") String medcNumber,
            @Param("dimension") String dimension
    );

 



}
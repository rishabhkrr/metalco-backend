package com.indona.invento.dao;

import com.indona.invento.entities.POGenerationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface POGenerationRepository extends JpaRepository<POGenerationEntity, Long> {
    List<POGenerationEntity> findBySupplierCodeOrSupplierName(String supplierCode, String supplierName);

    boolean existsByPoNumber(String poNumber);

    Optional<POGenerationEntity> findByPoNumber(String poNumber);

    Page<POGenerationEntity> findAll(Pageable pageable);

    List<POGenerationEntity> findByTimeStampBetween(Date fromDate, Date toDate);

    List<POGenerationEntity> findByPoStatus(String poStatus);

    // Find PO by SO line number through items
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT p FROM POGenerationEntity p JOIN p.items i WHERE i.soLineNumber = :soLineNumber")
    List<POGenerationEntity> findBySoLineNumber(@org.springframework.data.repository.query.Param("soLineNumber") String soLineNumber);



    @Query("SELECT po FROM POGenerationEntity po JOIN po.items item " +
            "WHERE po.unit = :unit AND item.itemDescription = :itemDescription AND item.rmReceiptStatus = 'PENDING'")
    List<POGenerationEntity> findPendingPOsByUnitAndItemDescription(@Param("unit") String unit,
                                                                    @Param("itemDescription") String itemDescription);
}


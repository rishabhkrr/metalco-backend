package com.indona.invento.dao;

import com.indona.invento.entities.GRNInterUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface GRNInterUnitRepository extends JpaRepository<GRNInterUnitEntity, Long> {

    Optional<GRNInterUnitEntity> findByInvoiceNumber(String invoice);

    Optional<GRNInterUnitEntity> findByGrnInterUnitRefNumber(String refNumber);

    // FRD: GIS-001 — Summary listing filtered by status
    List<GRNInterUnitEntity> findByStatus(String status);

    // FRD: Filter by unit
    List<GRNInterUnitEntity> findByUnit(String unit);

    // FRD: STI-003 — Only approved IU GRNs for stock transfer
    List<GRNInterUnitEntity> findByStatusAndUnit(String status, String unit);

    // FRD: Status filtering
    List<GRNInterUnitEntity> findByStatusIn(List<String> statuses);

    // FRD: Check if MEDCI already used in this module for a given unit
    boolean existsByMedcNumberAndUnit(String medcNumber, String unit);

    // FRD: Find by MEDC number
    List<GRNInterUnitEntity> findByMedcNumber(String medcNumber);

    // FRD: IUMR-010 — Count for IU GRN number generation
    @Query("SELECT COUNT(g) FROM GRNInterUnitEntity g WHERE g.grnInterUnitRefNumber LIKE :prefix%")
    long countByGrnInterUnitRefNumberStartingWith(@Param("prefix") String prefix);

    // FRD: MRS-002 — Find by MR number for status calculation
    List<GRNInterUnitEntity> findByMrNumber(String mrNumber);
}

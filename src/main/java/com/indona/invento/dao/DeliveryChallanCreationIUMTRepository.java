package com.indona.invento.dao;

import com.indona.invento.entities.DeliveryChallanCreationIUMTEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryChallanCreationIUMTRepository extends JpaRepository<DeliveryChallanCreationIUMTEntity, Long> {

    List<DeliveryChallanCreationIUMTEntity> findByTimestampBetween(Instant from, Instant to);
    List<DeliveryChallanCreationIUMTEntity> findByTimestampAfter(Instant from);
    List<DeliveryChallanCreationIUMTEntity> findByTimestampBefore(Instant to);

    @Query("""
       SELECT DISTINCT d.DCNumber
       FROM DeliveryChallanCreationIUMTEntity d
       WHERE d.DCNumber IS NOT NULL
         AND (
              :mode = 'jobwork'
              OR (:mode = 'inter unit transfer' AND d.vehicleOutStatusPackingAndDispatch = 'pending')
         )
       """)
    List<String> findDCNumbers(@Param("mode") String mode);

    @Query("SELECT DISTINCT d.packingListNumber FROM DeliveryChallanCreationIUMTEntity d")
    List<String> findAllPackingListNos();

    @Query("SELECT COUNT(m) FROM DeliveryChallanCreationIUMTEntity m WHERE m.timestamp >= :start AND m.timestamp < :end")
    int countByDateRange(@Param("start") Instant start, @Param("end") Instant end);
    List<DeliveryChallanCreationIUMTEntity> findByDCNumber(String dcNumber);
    List<DeliveryChallanCreationIUMTEntity> findByMrNumber(String mrNumber);
    List<DeliveryChallanCreationIUMTEntity>findByMrNumberAndDCNumber(String mrNumber, String DCNumber);
    List<DeliveryChallanCreationIUMTEntity> findByDCNumberIn(List<String> dcNumbers);

    // FRD: GRN-001/002 — GRN Status queries
    List<DeliveryChallanCreationIUMTEntity> findByGrnStatus(String grnStatus);

    // FRD: EWB-005 — MEDCI number generation (MEDCI + YYMM + sequential)
    @Query("SELECT COUNT(m) FROM DeliveryChallanCreationIUMTEntity m WHERE m.DCNumber LIKE :prefix%")
    long countByDCNumberStartingWith(@Param("prefix") String prefix);

    // FRD: DCC-004 — Check if packing list number already submitted in DC
    @Query("SELECT DISTINCT d.packingListNumber FROM DeliveryChallanCreationIUMTEntity d WHERE d.packingListNumber IS NOT NULL")
    List<String> findAllSubmittedPackingListNumbers();

    // FRD: GRN-002 — Update GRN status by DC number
    @Query("SELECT d FROM DeliveryChallanCreationIUMTEntity d WHERE d.DCNumber = :dcNumber AND d.grnStatus = 'Pending'")
    List<DeliveryChallanCreationIUMTEntity> findPendingByDCNumber(@Param("dcNumber") String dcNumber);
}

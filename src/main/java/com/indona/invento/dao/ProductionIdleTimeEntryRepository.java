package com.indona.invento.dao;

import com.indona.invento.entities.ProductionIdleTimeEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProductionIdleTimeEntryRepository extends JpaRepository<ProductionIdleTimeEntryEntity, Long> {

    List<ProductionIdleTimeEntryEntity> findByTimestampBetween(Instant from, Instant to);
    List<ProductionIdleTimeEntryEntity> findByTimestampAfter(Instant from);
    List<ProductionIdleTimeEntryEntity> findByTimestampBefore(Instant to);

    // Find idle time entries by machine name and date range
    @Query("""
    SELECT p FROM ProductionIdleTimeEntryEntity p
    WHERE p.machineName = :machineName
    AND p.timestamp BETWEEN :fromDateStart AND :toDateEnd
""")
    List<ProductionIdleTimeEntryEntity> findByMachineNameAndDateRange(
            @Param("machineName") String machineName,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateEnd") Instant toDateEnd
    );

    // Find all idle time entries by machine name (no date filter)
    @Query("""
        SELECT p FROM ProductionIdleTimeEntryEntity p
        WHERE p.machineName = :machineName
    """)
    List<ProductionIdleTimeEntryEntity> findByMachineName(@Param("machineName") String machineName);
}

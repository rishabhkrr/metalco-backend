package com.indona.invento.dao;

import com.indona.invento.dto.ScrapSummaryDto;
import com.indona.invento.entities.ProductionEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Modifying;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductionEntryRepository extends JpaRepository<ProductionEntryEntity, Long> {

    @Query(value = "SELECT pe.unit, pe.dimension, pe.product_category, pe.item_description, pe.brand, pe.grade, pe.temper, pe.machine_name, pe.so_number, pe.line_number, COALESCE(SUM(pe.produced_qty_kg), 0) AS sumKg, COALESCE(SUM(pe.produced_qty_no), 0) AS sumNo, MAX(pe.timestamp) AS maxTs, MAX(pe.start_time) AS maxStartTime FROM production_entry pe WHERE EXISTS (SELECT 1 FROM production_entry_end_piece ep WHERE ep.production_entry_id = pe.id AND LOWER(ep.end_piece_type) = 'scrap') GROUP BY pe.unit, pe.dimension, pe.product_category, pe.item_description, pe.brand, pe.grade, pe.temper, pe.machine_name, pe.so_number, pe.line_number", nativeQuery = true)
    List<Object[]> findAllScrapSummary();

    @Query(value = "SELECT pe.unit, pe.dimension, pe.product_category, pe.item_description, pe.brand, pe.grade, pe.temper, pe.machine_name, pe.so_number, pe.line_number, COALESCE(SUM(pe.produced_qty_kg), 0) AS sumKg, COALESCE(SUM(pe.produced_qty_no), 0) AS sumNo, MAX(pe.timestamp) AS maxTs, MAX(pe.start_time) AS maxStartTime FROM production_entry pe WHERE (CONVERT(date, pe.timestamp) = :date OR (pe.timestamp IS NULL AND CONVERT(date, CAST(pe.start_time AS DATETIME)) = :date)) AND EXISTS (SELECT 1 FROM production_entry_end_piece ep WHERE ep.production_entry_id = pe.id AND LOWER(ep.end_piece_type) = 'scrap') GROUP BY pe.unit, pe.dimension, pe.product_category, pe.item_description, pe.brand, pe.grade, pe.temper, pe.machine_name, pe.so_number, pe.line_number", nativeQuery = true)
    List<Object[]> findScrapSummaryByDate(@Param("date") java.sql.Date date);

    @Query("""
    SELECT DISTINCT p
    FROM ProductionEntryEntity p
    WHERE p.soNumber = :soNumber
      AND CAST(p.lineNumber AS string) = :lineNumber
      AND p.itemDescription = :itemDescription
    """)
    List<ProductionEntryEntity> findBySoLineAndItemWithChildren(
            @Param("soNumber") String soNumber,
            @Param("lineNumber") String lineNumber,
            @Param("itemDescription") String itemDescription
    );

    List<ProductionEntryEntity> findByTimestampBetween(Instant from, Instant to);
    List<ProductionEntryEntity> findByTimestampAfter(Instant from);
    List<ProductionEntryEntity> findByTimestampBefore(Instant to);

    // Find production entries by machine name and date range (using startTime field)
    @Query("""
    SELECT p FROM ProductionEntryEntity p
    WHERE p.machineName = :machineName
    AND p.timestamp BETWEEN :fromDateStart AND :toDateEnd
    ORDER BY p.timestamp DESC
""")
    List<ProductionEntryEntity> findByMachineNameAndDateRange(
            @Param("machineName") String machineName,
            @Param("fromDateStart") Instant fromDateStart,
            @Param("toDateEnd") Instant toDateEnd
    );

    // Find all production entries by machine name (no date filter)
    @Query("""
        SELECT p FROM ProductionEntryEntity p
        WHERE p.machineName = :machineName
        ORDER BY CAST(p.startTime AS date) DESC
    """)
    List<ProductionEntryEntity> findByMachineName(@Param("machineName") String machineName);

    @Query("SELECT DISTINCT pe.soNumber, pe.lineNumber " +
            "FROM ProductionEntryEntity pe " +
            "WHERE pe.soNumber IS NOT NULL AND pe.lineNumber IS NOT NULL")
    List<Object[]> findDistinctSoAndLineNumbers();

    @Query(value = "SELECT TOP 1 pe.end_time FROM production_entry pe ORDER BY pe.id DESC", nativeQuery = true)
    String findLastEntryEndTime();

    @Modifying
    @Query(value = "DELETE FROM production_entry_end_piece WHERE production_entry_id IN (:ids)", nativeQuery = true)
    void deleteEndPiecesByProductionEntryIds(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "DELETE FROM production_idle_time_entry WHERE production_entry_id IN (:ids)", nativeQuery = true)
    void deleteIdleEntriesByProductionEntryIds(@Param("ids") List<Long> ids);

    List<ProductionEntryEntity> findAllByTimestampBetween(Instant startInstant, Instant endInstant);
}

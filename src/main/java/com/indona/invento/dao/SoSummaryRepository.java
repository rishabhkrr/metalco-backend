package com.indona.invento.dao;

import com.indona.invento.entities.SoSummaryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SoSummaryRepository extends JpaRepository<SoSummaryEntity, Long> {

    SoSummaryEntity findBySoNumber(String soNumber);

    // Custom query to fetch SO Summary with items eagerly loaded
    @Query("SELECT DISTINCT s FROM SoSummaryEntity s LEFT JOIN FETCH s.items WHERE s.soNumber = :soNumber")
    SoSummaryEntity findBySoNumberWithItems(@Param("soNumber") String soNumber);

    @Modifying
    @Transactional
    @Query("UPDATE SoSummaryItemEntity i SET i.lrNumberUpdation = :lrNumber WHERE i.lineNumber = :lineNumber AND i.summary.soNumber = :soNumber")
    int updateLrNumber(@Param("soNumber") String soNumber,
                       @Param("lineNumber") String lineNumber,
                       @Param("lrNumber") String lrNumber);


    @Query(
            value = "SELECT i.item_description, FORMAT(i.dispatch_date, 'yyyy-MM') AS month, " +
                    "SUM(i.dispatch_quantity_kg) AS totalKg " +
                    "FROM so_summary_item i " +
                    "JOIN so_summary s ON s.id = i.summary_id " +
                    "WHERE s.unit = :unit " +
                    "AND i.item_description IN (:itemDescs) " +
                    "AND i.dispatch_date >= :startDate " +
                    "GROUP BY i.item_description, FORMAT(i.dispatch_date, 'yyyy-MM')",
            nativeQuery = true
    )
    List<Object[]> getMonthlySaleForItems(
            @Param("unit") String unit,
            @Param("itemDescs") List<String> itemDescs,
            @Param("startDate") LocalDate startDate
    );



}

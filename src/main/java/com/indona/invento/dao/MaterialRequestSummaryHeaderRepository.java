package com.indona.invento.dao;

import com.indona.invento.entities.MaterialRequestSummaryHeader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MaterialRequestSummaryHeaderRepository extends JpaRepository<MaterialRequestSummaryHeader, Long> {


    @Query("SELECT COUNT(m) FROM MaterialRequestHeader m WHERE CAST(m.timestamp AS date) = :date")
    int countByDate(@Param("date") LocalDate date);

    @Query("""
        SELECT h FROM MaterialRequestSummaryHeader h
        JOIN FETCH h.items i
        WHERE LOWER(h.mrNumber) = LOWER(:mrNumber) AND LOWER(i.lineNumber) = LOWER(:lineNumber)
    """)
    List<MaterialRequestSummaryHeader> findByMrNumberAndLineNumberWithItems(@Param("mrNumber") String mrNumber, @Param("lineNumber") String lineNumber);

    @Query("SELECT DISTINCT h FROM MaterialRequestSummaryHeader h LEFT JOIN FETCH h.items WHERE h.mrNumber IN :mrNumbers")
    List<MaterialRequestSummaryHeader> findByMrNumberInWithItems(@Param("mrNumbers") List<String> mrNumbers);

    @Query("SELECT DISTINCT m.unitCode FROM MaterialRequestSummaryHeader m WHERE m.unitCode IS NOT NULL")
    List<String> findUnitCodes();

    @Query("SELECT DISTINCT m.unitName FROM MaterialRequestSummaryHeader m WHERE m.unitName IS NOT NULL")
    List<String> findUnitNames();

    @Query("SELECT h FROM MaterialRequestSummaryHeader h " +
            "LEFT JOIN FETCH h.items " +
            "WHERE h.mrNumber = :mrNumber")
    List<MaterialRequestSummaryHeader> findByMrNumberWithItems(@Param("mrNumber") String mrNumber);
}

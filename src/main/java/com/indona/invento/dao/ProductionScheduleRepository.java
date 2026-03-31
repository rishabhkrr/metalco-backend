package com.indona.invento.dao;

import com.indona.invento.entities.ProductionScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProductionScheduleRepository extends JpaRepository<ProductionScheduleEntity, Long> {

    @Query("""
        SELECT new map(
            p.productCategory as productCategory,
            p.machineName as machineName,
            p.unit as unit,
            p.customerCode as customerCode,
            p.customerName as customerName,
            p.packing as packing,
            p.orderType as orderType,
            p.itemDescription as itemDescription,
            p.uomKg as uomKg,
            p.uomNo as uomNo,
            p.targetDispatchDate as targetDispatchDate
        )
        FROM ProductionScheduleEntity p
        WHERE p.soNumber = :soNumber
          AND CAST(p.lineNumber AS string) = :lineNumber
    """)
    List<Map<String, Object>> findDetailsBySoAndLineNumber(
            @Param("soNumber") String soNumber,
            @Param("lineNumber") String lineNumber
    );

    List<ProductionScheduleEntity> findBySoNumberIn(Set<String> soNumbers);

    @Query("SELECT DISTINCT p.soNumber FROM ProductionScheduleEntity p WHERE p.soNumber IS NOT NULL")
    List<String> findSoNumbers();

    @Query("SELECT DISTINCT p.lineNumber FROM ProductionScheduleEntity p WHERE p.soNumber = :soNumber AND p.lineNumber IS NOT NULL")
    List<String> findLineNumbers(@Param("soNumber") String soNumber);

    @Query("SELECT DISTINCT p.itemDescription FROM ProductionScheduleEntity p WHERE p.itemDescription IS NOT NULL")
    List<String> findRmDescriptions();

    @Query("""
                SELECT new map(
                    p.productCategory as productCategory,
                    p.brand as brand,
                    p.grade as grade,
                    p.temper as temper,
                    p.dimension as dimension,
                    p.requiredQuantityKg as requiredQuantityKg,
                    p.requiredQuantityNo as requiredQuantityNo,
                    p.nextProductionProcess as nextProductionProcess
                )
                FROM ProductionScheduleEntity p
                WHERE p.soNumber = :soNumber
                  AND CAST(p.lineNumber AS string) = :lineNumber
                  AND p.itemDescription = :itemDescription
            """)
    List<Map<String, Object>> findDetailsBySoAndLineNumberAndItemDescription(
            @Param("soNumber") String soNumber,
            @Param("lineNumber") String lineNumber,
            @Param("itemDescription") String itemDescription);

    @Query("SELECT DISTINCT p.soNumber, p.lineNumber " +
            "FROM ProductionScheduleEntity p " +
            "WHERE p.soNumber IS NOT NULL AND p.lineNumber IS NOT NULL AND p.machineName IS NOT NULL")
    List<Object[]> findDistinctSoAndLineNumbers();

    @Query("SELECT p FROM ProductionScheduleEntity p WHERE UPPER(p.status) = 'PENDING' ORDER BY p.soNumber, p.lineNumber")
    List<ProductionScheduleEntity> findPendingSchedules();
}

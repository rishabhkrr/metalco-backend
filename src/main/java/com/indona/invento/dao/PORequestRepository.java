package com.indona.invento.dao;

import com.indona.invento.dto.SupplierCodeNameDTO;
import com.indona.invento.entities.PORequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PORequestRepository extends JpaRepository<PORequestEntity, Long> {

    @Query(value = """
        SELECT COUNT(*) FROM po_request
        WHERE pr_number LIKE :prefix || '%'
        """, nativeQuery = true)
    int countByPrefix(@Param("prefix") String prefix);


    @Query("SELECT p FROM PORequestEntity p WHERE p.supplierCode = :supplierCode OR p.supplierName = :supplierName")
    List<PORequestEntity> findBySupplierCodeOrSupplierName(@Param("supplierCode") String supplierCode,
                                                           @Param("supplierName") String supplierName);


    void deleteByPrNumberIn(List<String> prNumbers);

    @Query("SELECT DISTINCT new com.indona.invento.dto.SupplierCodeNameDTO(p.supplierCode, p.supplierName) FROM PORequestEntity p")
    List<SupplierCodeNameDTO> findAllDistinctSupplierCodeNamePairs();




    @Query("SELECT DISTINCT p.unit FROM PORequestEntity p WHERE p.unit IS NOT NULL")
    List<String> findAllDistinctUnits();


    @Query("SELECT p FROM PORequestEntity p WHERE " +
            "(:supplierCode IS NULL OR :supplierCode = '' OR p.supplierCode = :supplierCode) AND " +
            "(:supplierName IS NULL OR :supplierName = '' OR p.supplierName = :supplierName) AND " +
            "(:unitCode IS NULL OR :unitCode = '' OR p.unitCode = :unitCode) AND " +
            "p.status = 'PENDING'")
    List<PORequestEntity> findBySupplierAndUnitCode(
            @Param("supplierCode") String supplierCode,
            @Param("supplierName") String supplierName,
            @Param("unitCode") String unitCode);



    boolean existsByPrNumber(String prNumber);

    List<PORequestEntity> findByPrNumberIn(List<String> prNumbers);

    List<PORequestEntity> findByTimeStampBetween(Date fromDate, Date toDate);


    Optional<PORequestEntity> findByPrNumber(String prNumber);

    @Query("SELECT p.prNumber FROM PORequestEntity p " + "WHERE p.prNumber LIKE CONCAT(:prefix, '%') " + "ORDER BY p.prNumber DESC LIMIT 1") String findLastPrNumberForMonth(@Param("prefix") String prefix);
}

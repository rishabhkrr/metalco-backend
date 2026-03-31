package com.indona.invento.dao;

import com.indona.invento.entities.CertificateOfConfidenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificateOfConfidenceRepository extends JpaRepository<CertificateOfConfidenceEntity, Long> {
    Optional<CertificateOfConfidenceEntity> findByCocNumber(String cocNumber);
    List<CertificateOfConfidenceEntity> findBySoNumber(String soNumber);
    List<CertificateOfConfidenceEntity> findByInvoiceNumber(String invoiceNumber);
    List<CertificateOfConfidenceEntity> findByCustomerCode(String customerCode);
    List<CertificateOfConfidenceEntity> findBySoNumberAndInvoiceNumberOrderByCreatedAtDesc(String soNumber, String invoiceNumber);

    // Check if COC number exists
    boolean existsByCocNumber(String cocNumber);

    // Count COCs with a specific prefix (e.g., "MECOC2506")
    @Query("SELECT COUNT(c) FROM CertificateOfConfidenceEntity c WHERE c.cocNumber LIKE CONCAT(:prefix, '%')")
    long countByPrefix(@Param("prefix") String prefix);
}


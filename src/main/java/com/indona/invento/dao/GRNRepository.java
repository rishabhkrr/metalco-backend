package com.indona.invento.dao;

import com.indona.invento.entities.GRNEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GRNRepository extends JpaRepository<GRNEntity, Long> {
    Optional<GRNEntity> findByGrnRefNumber(String grnRefNumber);

    List<GRNEntity> findByBinStatus(String binStatus);

    List<GRNEntity> findByBinStatusIn(List<String> binStatuses);

    List<GRNEntity> findByPoNumber(String poNumber);

    Optional<GRNEntity> findByInvoiceNumber(String invoiceNumber);

    List<GRNEntity> findAllByInvoiceNumber(String invoiceNumber);

    // Get all distinct invoice numbers
    @Query("SELECT DISTINCT g.invoiceNumber FROM GRNEntity g WHERE g.invoiceNumber IS NOT NULL")
    List<String> findDistinctInvoiceNumbers();

    @Query("SELECT g.invoiceNumber FROM GRNEntity g WHERE g.invoiceNumber IS NOT NULL")
    List<String> findAllInvoiceNumbersWithGrn();


}
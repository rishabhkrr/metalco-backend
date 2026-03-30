package com.indona.invento.dao;

import com.indona.invento.entities.BillingSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillingSummaryRepository extends JpaRepository<BillingSummaryEntity, Long> {
    List<BillingSummaryEntity> findBySoNumber(String soNumber);
    List<BillingSummaryEntity> findBySoNumberAndInvoiceNumber(String soNumber, String invoiceNumber);

    // Get distinct invoice numbers (TODO: Add packingStatus filter when workflow is ready)
    @Query("SELECT DISTINCT b.invoiceNumber FROM BillingSummaryEntity b WHERE b.invoiceNumber IS NOT NULL")
    List<String> findDistinctInvoiceNumbersByBillingStatusCompleted();

    // Get distinct SO numbers for a given invoice number
    @Query("SELECT DISTINCT b.soNumber FROM BillingSummaryEntity b WHERE b.invoiceNumber = :invoiceNumber AND b.soNumber IS NOT NULL")
    List<String> findDistinctSoNumbersByInvoiceNumber(String invoiceNumber);

    // Get distinct line numbers for a given invoice and SO number
    @Query("SELECT DISTINCT b.lineNumber FROM BillingSummaryEntity b WHERE b.invoiceNumber = :invoiceNumber AND b.soNumber = :soNumber AND b.lineNumber IS NOT NULL")
    List<String> findDistinctLineNumbersByInvoiceAndSo(String invoiceNumber, String soNumber);

    // Get line details for a specific invoice, SO, and line number (returns all matching items including duplicates)
    List<BillingSummaryEntity> findByInvoiceNumberAndSoNumberAndLineNumber(String invoiceNumber, String soNumber, String lineNumber);

    Optional<BillingSummaryEntity> findTopByItemDescriptionAndOrderTypeOrderByTimestampDesc(
            String itemDescription,
            String orderType
    );
}

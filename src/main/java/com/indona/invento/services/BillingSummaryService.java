package com.indona.invento.services;

import com.indona.invento.dto.BillLineDetailsDTO;
import com.indona.invento.dto.BillingSummaryDTO;
import com.indona.invento.entities.BillingSummaryEntity;
import com.indona.invento.entities.PackingSubmission;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BillingSummaryService {
    BillingSummaryEntity saveBilling(BillingSummaryDTO dto);
    List<BillingSummaryEntity> getAllBillings();
    PackingSubmission getPackingDetailsBySoAndLine(String soNumber, String lineNumber);
    void deleteAllBillings();

    // Stock Return APIs
    List<String> getCompletedInvoiceNumbers();
    List<String> getSoNumbersByInvoice(String invoiceNumber);
    List<String> getLineNumbersByInvoiceAndSo(String invoiceNumber, String soNumber);
    BillLineDetailsDTO getLineDetails(String invoiceNumber, String soNumber, String lineNumber);

    // ==================== FRD v3.0 New Methods ====================

    /** Get billing record by ID */
    BillingSummaryEntity getBillingById(Long id);

    /** Update billing entry */
    BillingSummaryEntity updateBilling(Long id, BillingSummaryDTO dto);

    /** BS-BR-001: Get available RFD List Numbers (not already in billing_summary) */
    List<String> getAvailableRfdListNumbers();

    /** BS-BR-008: Update LR Number and auto-propagate to SO Summary */
    BillingSummaryEntity updateLrNumber(Long id, String lrNumber);

    /** Upload Invoice PDF */
    BillingSummaryEntity uploadInvoicePdf(Long id, MultipartFile file);

    /** Get invoices by unit (for Sales Return module) */
    List<String> getInvoicesByUnit(String unit);
}

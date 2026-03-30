package com.indona.invento.services;


import com.indona.invento.dto.BillLineDetailsDTO;
import com.indona.invento.dto.BillingSummaryDTO;
import com.indona.invento.entities.BillingSummaryEntity;
import com.indona.invento.entities.PackingSubmission;

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

}

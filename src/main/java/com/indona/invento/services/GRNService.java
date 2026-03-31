package com.indona.invento.services;

import com.indona.invento.dto.*;

import java.util.List;

public interface GRNService {
    GRNResponseDTO createGRN(GRNRequestDTO request);
    GRNResponseDTO getGRNById(Long id);
    List<GRNResponseDTO> getAllGRNs();
    List<InvoiceDropdownDTO> getInvoiceNumbersForDropdown();
    List<String> getAllInvoiceNumbers(); // New method
    GRNResponseDTO fetchDetailsByInvoice(String invoiceNumber);
    List<String> getPoNumbersByInvoice(String invoiceNumber);
    List<GRNItemRequestDTO> getItemsByPoNumber(String poNumber);
    List<PurchaseLineDetailsDTO> getPurchaseLineDetails(String invoiceNumber, String poNumber);
    GRNResponseDTO markMaterialUnloaded(Long id, String notes);
    List<GRNResponseDTO> getAllPendingGRNs();
    GRNResponseDTO markBinCompleted(Long id);
    InitiateStockTransferResponseDto initiateStockTransferFromGRN(Long grnId);
    List<GRNItemRequestDTO> getItemsByGrnId(Long grnId);
    GRNResponseDTO saveGrnWithPo(GRNRequestDTO request);
    GRNResponseDTO saveGrnWithoutPo(GRNRequestDTO request);
    void deleteAllGRNs();

    // Approve and Reject
    GRNResponseDTO approveGRN(GRNApprovalRequestDTO request);
    GRNResponseDTO rejectGRN(Long id);

    /**
     * Get GRN bundle details by GRN number
     * Returns: GRN Ref No, Time, Quantity KG sum, No sum, Average Item Price, Heat No, Lot No, Test Certificate
     */
    java.util.Map<String, Object> getGrnBundleDetailsByGrnNumber(String grnNumber);

    /**
     * Get GRN bundle details for multiple GRN numbers
     * Returns: Array of bundles with individual details and a summary with aggregated data
     */
    java.util.Map<String, Object> getGrnBundleDetailsForMultipleGrnNumbers(List<String> grnNumbers);

    /**
     * Update material unloading status to COMPLETED for GRN and GateInward
     * by matching gateEntryRefNo + vehicleNumber
     * If vehicleNumber matches multiple, update by refNo only
     */
    java.util.Map<String, Object> updateMaterialUnloadingStatusCompleted(String gateEntryRefNo, String vehicleNumber);

}
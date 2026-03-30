package com.indona.invento.services;

import com.indona.invento.dto.*;
import com.indona.invento.entities.GateInwardEntity;

import java.util.List;

public interface GateInwardService {
	GateInwardResponseDTO createGateInward(GateInwardRequestDTO requestDTO);

	GateInwardResponseDTO getGateInwardById(Long id);

	GateInwardResponseDTO getGateInwardByRefNumber(String gatePassRefNumber);

	List<GateInwardResponseDTO> getAllGateInwards();

	List<GateInwardResponseDTO> getGateInwardsByUnit(String unitCode);

	List<GateInwardResponseDTO> getGateInwardsByStatus(String status);

	GateInwardResponseDTO updateGateInward(Long id, GateInwardRequestDTO requestDTO);

	List<POPlacedDTO> getPOsWithPlacedStatus();

	boolean checkIfPoAlreadyHasGateEntry(String poNumber);

	List<GateInwardInvoiceFetchDTO> getDataByInvoiceNumber(String invoiceNumber);

    List<InvoiceDropdownDTO> getInvoiceNumbersWithVehicleInStatus();

    void markMaterialUnloadingCompleteByInvoice(String invoiceNumber);

    GateInwardEntity updateGateInwardPartialById(Long id, GateInwardPartialUpdateDTO dto);

    List<String> getDistinctInvoiceNumbers();

    void deleteAllGateInwards();

    GateInwardResponseDTO markVehicleOut(Long id);

    /**
     * Get all Gate Entry entries (from both gate_inward and gate_entry_packing_and_dispatch tables)
     * with consolidated summary
     *
     * Returns consolidated data including:
     * - Vehicle No, Purpose, Mode, Gate Entry Ref No, Invoice No
     * - PO Numbers (multiple), MEDC Nos (multiple), MEDCI Nos (multiple), DC No, MEDCP No
     * - Source indicates which table the entry came from (GATE_INWARD or GATE_ENTRY_PACKING_DISPATCH)
     */
    List<GateInwardSummaryDTO> getAllGateInwardSummary();
}

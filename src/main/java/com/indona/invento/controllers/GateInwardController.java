package com.indona.invento.controllers;

import com.indona.invento.dto.*;
import com.indona.invento.entities.GateInwardEntity;
import com.indona.invento.services.GateInwardService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gate-inward")
@RequiredArgsConstructor
public class GateInwardController {

    private final GateInwardService gateInwardService;

    @PostMapping
    public ResponseEntity<GateInwardResponseDTO> createGateInward(@RequestBody GateInwardRequestDTO requestDTO) {
        GateInwardResponseDTO response = gateInwardService.createGateInward(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GateInwardResponseDTO>> getAllGateInwards() {
        List<GateInwardResponseDTO> response = gateInwardService.getAllGateInwards();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GateInwardResponseDTO> getGateInwardById(@PathVariable Long id) {
        GateInwardResponseDTO response = gateInwardService.getGateInwardById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/ref/{gatePassRefNumber}")
    public ResponseEntity<GateInwardResponseDTO> getGateInwardByRefNumber(@PathVariable String gatePassRefNumber) {
        GateInwardResponseDTO response = gateInwardService.getGateInwardByRefNumber(gatePassRefNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/unit/{unitCode}")
    public ResponseEntity<List<GateInwardResponseDTO>> getGateInwardsByUnit(@PathVariable String unitCode) {
        List<GateInwardResponseDTO> response = gateInwardService.getGateInwardsByUnit(unitCode);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<GateInwardResponseDTO>> getGateInwardsByStatus(@PathVariable String status) {
        List<GateInwardResponseDTO> response = gateInwardService.getGateInwardsByStatus(status);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GateInwardResponseDTO> updateGateInward(
            @PathVariable Long id,
            @RequestBody GateInwardRequestDTO requestDTO) {
        GateInwardResponseDTO response = gateInwardService.updateGateInward(id, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/pos/placed")
    public ResponseEntity<List<POPlacedDTO>> getPOsWithPlacedStatus() {
        List<POPlacedDTO> response = gateInwardService.getPOsWithPlacedStatus();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/check-po/{poNumber}")
    public ResponseEntity<Boolean> checkIfPoAlreadyHasGateEntry(@PathVariable String poNumber) {
        boolean exists = gateInwardService.checkIfPoAlreadyHasGateEntry(poNumber);
        return new ResponseEntity<>(exists, HttpStatus.OK);
    }

    @GetMapping("/grn-data/invoice/{invoiceNumber}")
    public ResponseEntity<List<GateInwardInvoiceFetchDTO>> getDataByInvoiceNumber(
            @PathVariable String invoiceNumber
    ) {
        List<GateInwardInvoiceFetchDTO> response =
                gateInwardService.getDataByInvoiceNumber(invoiceNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/invoice-numbers/vehicle-in")
    public ResponseEntity<List<InvoiceDropdownDTO>> getInvoiceNumbersWithVehicleInStatus() {
        List<InvoiceDropdownDTO> invoiceNumbers = gateInwardService.getInvoiceNumbersWithVehicleInStatus();
        return new ResponseEntity<>(invoiceNumbers, HttpStatus.OK);
    }

    @PutMapping("/material-unloading/complete-safe/{invoiceNumber}")
    public ResponseEntity<Map<String, Object>> markMaterialUnloadingSafe(@PathVariable String invoiceNumber) {
        gateInwardService.markMaterialUnloadingCompleteByInvoice(invoiceNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Material unloading marked COMPLETE");
        response.put("invoiceNumber", invoiceNumber);

        return ResponseEntity.ok(response);
    }


    @PutMapping("/update-partial/{id}")
    public ResponseEntity<GateInwardEntity> updateGateInwardPartial(@PathVariable Long id,
                                                                    @RequestBody GateInwardPartialUpdateDTO dto) {
        GateInwardEntity updated = gateInwardService.updateGateInwardPartialById(id, dto);
        return new ResponseEntity<>(updated, HttpStatus.OK);
    }





    @GetMapping("/invoice/list")
    public ResponseEntity<List<String>> getInvoiceDropdown() {
        List<String> invoices = gateInwardService.getDistinctInvoiceNumbers();
        return new ResponseEntity<>(invoices, HttpStatus.OK);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllGateInwards() {
        try {
            gateInwardService.deleteAllGateInwards();
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ All gate inwards deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "❌ Failed to delete all gate inwards",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/mark-vehicle-out/{id}")
    public ResponseEntity<GateInwardResponseDTO> markVehicleOut(@PathVariable Long id) {
        GateInwardResponseDTO response = gateInwardService.markVehicleOut(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Get all Gate Entry entries with consolidated summary
     * Fetches data from BOTH gate_inward and gate_entry_packing_and_dispatch tables
     *
     * Returns: Vehicle No, Purpose, Mode, Gate Entry Ref No, Invoice No,
     * PO Numbers (multiple), MEDC Nos (multiple), MEDCI Nos (multiple), DC No, MEDCP No,
     * with source field indicating which table the entry came from
     *
     * Usage: GET /api/metalco/gate-inward/summary/all
     */
    @GetMapping("/summary/all")
    public ResponseEntity<?> getAllGateInwardSummary() {
        try {
            List<GateInwardSummaryDTO> summaryList = gateInwardService.getAllGateInwardSummary();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalCount", summaryList.size());
            response.put("data", summaryList);
            response.put("message", "Gate entry summary fetched successfully from both GATE_INWARD and GATE_ENTRY_PACKING_DISPATCH tables");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Failed to fetch gate entry summary");
            errorResponse.put("details", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

}


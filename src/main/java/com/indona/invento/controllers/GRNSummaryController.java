package com.indona.invento.controllers;

import com.indona.invento.dto.GRNResponseDTO;
import com.indona.invento.dto.InitiateStockTransferResponseDto;
import com.indona.invento.services.GRNService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/grn-summary")
@RequiredArgsConstructor
public class GRNSummaryController {

    private final GRNService grnService;

    // Fetch all GRNs with binStatus = PENDING
    @GetMapping("/pending")
    public ResponseEntity<List<GRNResponseDTO>> getPendingGRNs() {
        return ResponseEntity.ok(grnService.getAllPendingGRNs());
    }

    // Update binStatus to COMPLETED (called when Stock Transfer save is done)
    @PutMapping("/mark-bin-completed/{id}")
    public ResponseEntity<GRNResponseDTO> markBinCompleted(@PathVariable Long id) {
        return ResponseEntity.ok(grnService.markBinCompleted(id));
    }

    // Initiate Stock Transfer from GRN (called when user clicks "Stock Transfer" button)
    @PostMapping("/initiate-stock-transfer/{grnId}")
    public ResponseEntity<?> initiateStockTransfer(@PathVariable Long grnId) {
        try {
            InitiateStockTransferResponseDto response = grnService.initiateStockTransferFromGRN(grnId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to initiate stock transfer: " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An error occurred: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllGRNs() {
        try {
            grnService.deleteAllGRNs();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All GRN summaries deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all GRN summaries",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Get GRN bundle details by GRN number
     * Returns: GRN Ref No, Time, Total Quantity KG, Total Quantity No, Average Item Price, Heat No, Lot No, Test Certificate
     *
     * Usage: GET /api/metalco/grn-summary/bundle-details/{grnNumber}
     */
    @GetMapping("/bundle-details/{grnNumber}")
    public ResponseEntity<?> getGrnBundleDetails(@PathVariable String grnNumber) {
        try {
            Map<String, Object> response = grnService.getGrnBundleDetailsByGrnNumber(grnNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to fetch GRN bundle details: " + e.getMessage()
            ));
        }
    }

    /**
     * Get GRN bundle details for multiple GRN numbers
     * Returns: Array of bundles with individual details and a summary with aggregated data
     *
     * Request Body:
     * {
     *   "grnNumbers": ["GRN001", "GRN002", "GRN003"]
     * }
     *
     * Response includes:
     * - grnSummaries: Array of individual GRN summaries
     * - bundles: Array of all bundles with details (GRN Ref No, Date of Inward, Qty KG, Qty No, Item Price, Heat No, Lot No, Test Certificate, QR Code)
     * - summary: Aggregated summary with totals and averages
     *
     * Usage: POST /api/metalco/grn-summary/multi-bundle-details
     */
    @PostMapping("/multi-bundle-details")
    public ResponseEntity<?> getMultipleGrnBundleDetails(@RequestBody Map<String, List<String>> request) {
        try {
            List<String> grnNumbers = request.get("grnNumbers");
            if (grnNumbers == null || grnNumbers.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "GRN numbers list cannot be empty"
                ));
            }
            Map<String, Object> response = grnService.getGrnBundleDetailsForMultipleGrnNumbers(grnNumbers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to fetch multi-GRN bundle details: " + e.getMessage()
            ));
        }
    }

    /**
     * Update material unloading status to COMPLETED for GRN and GateInward
     * by matching gateEntryRefNo + vehicleNumber
     * If vehicleNumber matches multiple, update by refNo only
     *
     * Request Body:
     * {
     *   "gateEntryRefNo": "REF123",
     *   "vehicleNumber": "MH12AB1234"
     * }
     *
     * Usage: PUT /api/metalco/grn-summary/update-material-unloading-status
     */
    @PutMapping("/update-material-unloading-status")
    public ResponseEntity<?> updateMaterialUnloadingStatus(@RequestBody Map<String, String> request) {
        try {
            String gateEntryRefNo = request.get("gateEntryRefNo");
            String vehicleNumber = request.get("vehicleNumber");

            if (gateEntryRefNo == null || gateEntryRefNo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "gateEntryRefNo is required"
                ));
            }

            if (vehicleNumber == null || vehicleNumber.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "vehicleNumber is required"
                ));
            }

            Map<String, Object> response = grnService.updateMaterialUnloadingStatusCompleted(gateEntryRefNo, vehicleNumber);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to update material unloading status: " + e.getMessage()
            ));
        }
    }

}

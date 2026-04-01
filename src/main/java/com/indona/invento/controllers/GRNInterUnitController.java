package com.indona.invento.controllers;

import com.indona.invento.entities.GRNInterUnitEntity;
import com.indona.invento.services.GRNInterUnitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * FRD: IUMT-GRN Controller
 * REST API for all Inter-Unit Material Transfer GRN operations
 *
 * Sub-Module 1: GRN Interunit Material Request Entry (IUMR-001 to IUMR-010)
 * Sub-Module 2: Material Request InterUnit Summary (MRS-001 to MRS-007)
 * Sub-Module 3: GRN Interunit Request Summary (GIS-001 to GIS-008)
 * Sub-Module 4: Stock Transfer Integration (STI-001 to STI-010)
 * Sub-Module 5: Bundle Creation & Bin Allocation (ABP, RBA, QRG)
 * Sub-Module 6: Submit Validation (SUB-001 to SUB-009)
 */
@RestController
@RequestMapping("/grn-interunit")
@CrossOrigin(origins = "*")
public class GRNInterUnitController {

    @Autowired
    private GRNInterUnitService grnInterUnitService;

    // =========================================================================
    // Sub-Module 1: GRN Interunit Material Request Entry
    // =========================================================================

    /**
     * FRD: IUMR-001 to IUMR-010
     * Create a new GRN Interunit Material Request with auto-generated IU GRN Number
     */
    @PostMapping
    public ResponseEntity<GRNInterUnitEntity> createInterUnitGRN(@RequestBody GRNInterUnitEntity entity) {
        GRNInterUnitEntity created = grnInterUnitService.createInterUnitGRN(entity);
        return ResponseEntity.ok(created);
    }

    /**
     * Update an existing IU GRN (only if status is Pending)
     */
    @PutMapping("/{id}")
    public ResponseEntity<GRNInterUnitEntity> updateInterUnitGRN(
            @PathVariable Long id,
            @RequestBody GRNInterUnitEntity entity) {
        GRNInterUnitEntity updated = grnInterUnitService.updateInterUnitGRN(id, entity);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get IU GRN by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<GRNInterUnitEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(grnInterUnitService.getById(id));
    }

    /**
     * Get IU GRN by Ref Number (IU GRN Number)
     */
    @GetMapping("/by-ref/{refNumber}")
    public ResponseEntity<GRNInterUnitEntity> getByRefNumber(@PathVariable String refNumber) {
        return ResponseEntity.ok(grnInterUnitService.getByRefNumber(refNumber));
    }

    /**
     * Get all IU GRNs
     */
    @GetMapping
    public ResponseEntity<List<GRNInterUnitEntity>> getAll() {
        return ResponseEntity.ok(grnInterUnitService.getAll());
    }

    // =========================================================================
    // Sub-Module 2: Material Request InterUnit Summary
    // FRD: MRS-001 to MRS-007
    // =========================================================================

    /**
     * FRD: MRS-001 — Summary listing with tri-state status
     * MRS-002/003 — Status auto-calculated: Pending / Partially Received / Received
     */
    @GetMapping("/mr-summary")
    public ResponseEntity<List<Map<String, Object>>> getMaterialRequestSummary(
            @RequestParam(required = false) String unit) {
        return ResponseEntity.ok(grnInterUnitService.getMaterialRequestSummary(unit));
    }

    // =========================================================================
    // Sub-Module 3: GRN Interunit Request Summary
    // FRD: GIS-001 to GIS-008
    // =========================================================================

    /**
     * FRD: GIS-001 — GRN summary listing with filters
     * GIS-007 — Filters: Status, Unit
     */
    @GetMapping("/grn-summary")
    public ResponseEntity<List<GRNInterUnitEntity>> getGrnSummary(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String unit) {
        return ResponseEntity.ok(grnInterUnitService.getGrnSummary(status, unit));
    }

    /**
     * FRD: GIS-004 — Approve IU GRN
     * On Approval: status = "Approved", Stock Transfer button enabled
     */
    @PostMapping("/approve/{id}")
    public ResponseEntity<GRNInterUnitEntity> approveInterUnitGRN(
            @PathVariable Long id,
            @RequestParam String approvedBy) {
        return ResponseEntity.ok(grnInterUnitService.approveInterUnitGRN(id, approvedBy));
    }

    /**
     * FRD: GIS-005 — Reject IU GRN
     * On Rejection: status = "Rejected", creator notified with rejection remarks
     */
    @PostMapping("/reject/{id}")
    public ResponseEntity<GRNInterUnitEntity> rejectInterUnitGRN(
            @PathVariable Long id,
            @RequestParam String rejectedBy,
            @RequestParam String remarks) {
        return ResponseEntity.ok(grnInterUnitService.rejectInterUnitGRN(id, rejectedBy, remarks));
    }

    // =========================================================================
    // Sub-Module 4: Stock Transfer Integration
    // FRD: STI-001 to STI-010
    // =========================================================================

    /**
     * FRD: STI-003 — Get approved IU GRNs for Stock Transfer dropdown
     * Only returns IU GRNs with status = "Approved"
     */
    @GetMapping("/approved")
    public ResponseEntity<List<GRNInterUnitEntity>> getApprovedForStockTransfer(
            @RequestParam String unit) {
        return ResponseEntity.ok(grnInterUnitService.getApprovedForStockTransfer(unit));
    }

    // =========================================================================
    // Sub-Module 5: Rack & Bin Auto-Allocation
    // FRD: RBA-001 to RBA-008
    // =========================================================================

    /**
     * FRD: RBA-001 to RBA-008 — Auto-allocate rack and bin location
     * Algorithm: Filter1(Store+Category) → Filter2(Capacity) → Sort(Order,Distance) → First result
     *
     * For "Rejected" material: Skips algorithm, returns Rejection/Rejection/Common
     * Fallback: Warehouse/Common/Common
     */
    @PostMapping("/allocate-bin")
    public ResponseEntity<Map<String, Object>> allocateRackBin(
            @RequestParam(defaultValue = "Warehouse") String storageType,
            @RequestParam String itemCategory,
            @RequestParam double requiredWeight,
            @RequestParam(defaultValue = "General") String materialAcceptance) {
        Map<String, Object> allocation = grnInterUnitService.allocateRackBin(
                storageType, itemCategory, requiredWeight, materialAcceptance);
        return ResponseEntity.ok(allocation);
    }

    // =========================================================================
    // Sub-Module 6: Submit Validation
    // FRD: SUB-001 to SUB-009
    // =========================================================================

    /**
     * FRD: SUB-001 to SUB-003 — Validate all conditions before Submit
     * Checks: All items covered + Qty matches + All scans complete
     */
    @PostMapping("/validate-submit")
    public ResponseEntity<Map<String, Object>> validateSubmit(
            @RequestParam Long stockTransferId,
            @RequestParam String iuGrnNumber) {
        return ResponseEntity.ok(grnInterUnitService.validateSubmit(stockTransferId, iuGrnNumber));
    }

    // =========================================================================
    // MEDCI Filtering
    // =========================================================================

    /**
     * FRD: IUMR-002 — Get available MEDCI numbers for GRN Interunit Request
     * Returns MEDCI from Gate Entry Inward with Mode = "Interunit transfer",
     * excluding those already entered in this module for the current unit.
     */
    @GetMapping("/available-medci")
    public ResponseEntity<List<String>> getAvailableMedciNumbers(
            @RequestParam(required = false) String unit) {
        return ResponseEntity.ok(grnInterUnitService.getAvailableMedciNumbers(unit));
    }

    /**
     * FRD: Show only MEDCI numbers not already used in this module for the current unit
     */
    @GetMapping("/medci-check")
    public ResponseEntity<Map<String, Object>> checkMedci(
            @RequestParam String medcNumber,
            @RequestParam String unit) {
        boolean used = grnInterUnitService.isMedciUsed(medcNumber, unit);
        Map<String, Object> result = Map.of(
                "medcNumber", medcNumber,
                "unit", unit,
                "alreadyUsed", used,
                "available", !used
        );
        return ResponseEntity.ok(result);
    }
}

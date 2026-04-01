package com.indona.invento.controllers;

import com.indona.invento.dto.PackingListTransferDTO;
import com.indona.invento.dto.RfdListSummaryDTO;
import com.indona.invento.dto.RfdListItemDTO;
import com.indona.invento.dto.RfdListBatchDTO;
import com.indona.invento.entities.PackingListTransferEntity;
import com.indona.invento.entities.PackingSubmission;
import com.indona.invento.entities.SalesOrder;
import com.indona.invento.dao.PackingSubmissionRepository;
import com.indona.invento.dao.SalesOrderRepository;
import com.indona.invento.services.PackingListTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/packing-list")
public class PackingListTransferController {

    @Autowired
    private PackingListTransferService service;

    @Autowired
    private PackingSubmissionRepository packingSubmissionRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @PostMapping("/save")
    public ResponseEntity<List<PackingListTransferEntity>> save(@RequestBody List<PackingListTransferDTO> dtos) {
        System.out.println("\n✅ ========== /api/packing-list/save ENDPOINT CALLED ==========");
        System.out.println("   Received " + dtos.size() + " packing list DTOs");
        dtos.forEach(dto -> {
            System.out.println("   📦 SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber() +
                    " | Unit: " + dto.getUnit() + " | Customer: " + dto.getCustomerName());
        });
        System.out.println("==============================================================\n");

        List<PackingListTransferEntity> saved = service.savePackingList(dtos);

        System.out.println("\n✅ ========== SAVE OPERATION COMPLETE ==========");
        System.out.println("   Total saved: " + saved.size());
        System.out.println("==============================================\n");

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PackingListTransferEntity>> getAll() {
        return ResponseEntity.ok(service.getAllPackingLists());
    }

    // ═══════════════════════════════════════════════════════════════
    //  NEW: RFD List Summary — 3-Level Hierarchy Endpoints
    // ═══════════════════════════════════════════════════════════════

    /**
     * Level 1: Get RFD List Summary (one row per RFD List Number)
     */
    @GetMapping("/rfd-summary")
    public ResponseEntity<List<RfdListSummaryDTO>> getRfdListSummary() {
        System.out.println("\n📊 GET /api/packing-list/rfd-summary called");
        List<RfdListSummaryDTO> summaries = service.getRfdListSummary();
        return ResponseEntity.ok(summaries);
    }

    /**
     * Level 2: Get items for a specific RFD List (one row per SO/Line)
     */
    @GetMapping("/rfd-summary/{packingListNumber}/items")
    public ResponseEntity<?> getRfdListItems(@PathVariable String packingListNumber) {
        System.out.println("\n📦 GET /api/packing-list/rfd-summary/" + packingListNumber + "/items called");
        List<RfdListItemDTO> items = service.getRfdListItems(packingListNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("rfdListNumber", packingListNumber);
        response.put("items", items);

        return ResponseEntity.ok(response);
    }

    /**
     * Level 3: Get batch details for a specific item within an RFD List
     */
    @GetMapping("/rfd-summary/{packingListNumber}/items/{itemId}/batch-details")
    public ResponseEntity<?> getRfdListBatchDetails(
            @PathVariable String packingListNumber,
            @PathVariable Long itemId) {
        System.out.println("\n🔍 GET /api/packing-list/rfd-summary/" + packingListNumber +
                "/items/" + itemId + "/batch-details called");
        List<RfdListBatchDTO> batches = service.getRfdListBatchDetails(packingListNumber, itemId);

        Map<String, Object> response = new HashMap<>();
        response.put("packingListNumber", packingListNumber);
        response.put("itemId", itemId);
        response.put("batches", batches);

        return ResponseEntity.ok(response);
    }

    /**
     * Check packing status for a given SO/Line
     * Required for Gate 6 validation — packing must be "Completed"
     */
    @GetMapping("/packing-status")
    public ResponseEntity<?> getPackingStatus(
            @RequestParam String soNumber,
            @RequestParam String lineNumber) {
        PackingSubmission submission = packingSubmissionRepository
                .findBySoNumberAndLineNumber(soNumber, lineNumber);

        Map<String, Object> result = new HashMap<>();
        if (submission != null) {
            result.put("packingStatus", submission.getPackingStatus());
            result.put("found", true);
        } else {
            result.put("packingStatus", "Not Started");
            result.put("found", false);
        }
        result.put("soNumber", soNumber);
        result.put("lineNumber", lineNumber);

        return ResponseEntity.ok(result);
    }

    /**
     * Get charges and customer details from Sales Order for a given SO Number
     * Returns item rate (price), charges, billing/shipping addresses, PO details
     */
    @GetMapping("/so-charges")
    public ResponseEntity<?> getSoCharges(@RequestParam String soNumber) {
        Optional<SalesOrder> optionalSo = salesOrderRepository.findBySoNumber(soNumber);
        if (optionalSo.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Sales Order not found: " + soNumber
            ));
        }

        SalesOrder so = optionalSo.get();
        Map<String, Object> result = new HashMap<>();
        result.put("soNumber", so.getSoNumber());
        result.put("customerCode", so.getCustomerCode());
        result.put("customerName", so.getCustomerName());
        result.put("billingAddress", so.getBillingAddress());
        result.put("shippingAddress", so.getShippingAddress());
        result.put("customerPoNo", so.getCustomerPoNo());
        result.put("customerPoFile", so.getCustomerPoFile());

        // Charges (order-level)
        result.put("packingCharges", so.getPackingCharges());
        result.put("freightCharges", so.getFreightCharges());
        result.put("cuttingCharges", so.getCuttingCharges());
        result.put("laminationCharges", so.getLaminationCharges());
        result.put("hamaliCharges", so.getHamaliCharges());
        result.put("cgst", so.getCgst());
        result.put("sgst", so.getSgst());
        result.put("igst", so.getIgst());

        // Get item-level price from SO line items
        if (so.getItems() != null) {
            so.getItems().forEach(item -> {
                // Return prices keyed by line number
                Map<String, Object> lineData = new HashMap<>();
                lineData.put("price", item.getCurrentPrice());
                lineData.put("orderType", item.getOrderType());
                lineData.put("orderQuantityKg", item.getQuantityKg());
                lineData.put("orderQuantityNo", item.getQuantityNos());
                result.put("line_" + item.getLineNumber(), lineData);
            });
        }

        return ResponseEntity.ok(result);
    }
}

package com.indona.invento.controllers;

import com.indona.invento.dto.ResponseDto;
import com.indona.invento.entities.DeliveryChallanCreationIUMTEntity;
import com.indona.invento.entities.unitAddressEntity;
import com.indona.invento.services.DeliveryChallanCreationIUMTService;
import com.indona.invento.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/delivery-challan-iumt")
public class DeliveryChallanCreationIUMTController {

    @Autowired
    private DeliveryChallanCreationIUMTService deliveryChallanCreationIUMTService;

    @PostMapping("/create")
    public ResponseEntity <List<DeliveryChallanCreationIUMTEntity>> createDeliveryChallanIUMT(@RequestBody List<DeliveryChallanCreationIUMTEntity> entities) {
        List<DeliveryChallanCreationIUMTEntity> created = deliveryChallanCreationIUMTService.createDeliveryChallanIUMT(entities);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<DeliveryChallanCreationIUMTEntity>> getAllDeliveryChallansIUMT(
            @RequestParam(value = "fromDate", required = false) String fromDate,
            @RequestParam(value = "toDate", required = false) String toDate) {

        List<DeliveryChallanCreationIUMTEntity> allEntities =
                deliveryChallanCreationIUMTService.getAllDeliveryChallansIUMT(fromDate, toDate);
        return ResponseEntity.ok(allEntities);
    }

    @GetMapping("/unit-codes")
    public ResponseEntity<Map<String, Object>> getUnitCodes() {
        List<Map<String, String>> rm = deliveryChallanCreationIUMTService.getUnitCodes();
        return ResponseEntity.ok(Map.of("data", rm, "status", "success"));
    }

    @GetMapping("/unit-names")
    public ResponseEntity<Map<String, Object>> getUnitNames() {
        List<Map<String, String>> rm = deliveryChallanCreationIUMTService.getUnitNames();
        return ResponseEntity.ok(Map.of("data", rm, "status", "success"));
    }

    @GetMapping("/dc-numbers")
    public ResponseEntity<Map<String, Object>> getDCNumbers(@RequestParam String mode) {
        List<Map<String, String>> rm = deliveryChallanCreationIUMTService.getDCNumbers(mode);
        return ResponseEntity.ok(Map.of("data", rm, "status", "success"));
    }

    @GetMapping("/item-price")
    public Map<String, Object> getItemPrice(
            @RequestParam String unit,
            @RequestParam String description) {

        Map<String, Object> response = deliveryChallanCreationIUMTService.getItemPrice(unit, description);
        return response;
    }

    @GetMapping("/primary-address")
    public ResponseEntity<ResponseDto<List<unitAddressEntity>>> getPrimaryAddresses(
            @RequestParam String unitCode, @RequestParam String unitName) {

        List<unitAddressEntity> result = deliveryChallanCreationIUMTService.getPrimaryAddresses(unitCode, unitName);
        return ResponseUtil.success(result);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllDeliveryChallanIUMT() {
        try {
            deliveryChallanCreationIUMTService.deleteAll();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All delivery challan IUMT deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all delivery challan IUMT",
                    "details", e.getMessage()
            ));
        }
    }

    // =========================================================================
    // FRD: GRN Status Management (GRN-001 to GRN-005)
    // =========================================================================

    /**
     * FRD: GRN-002 — Update GRN status to Completed
     * Called when receiving unit saves and approves GRN
     */
    @PostMapping("/update-grn-status")
    public ResponseEntity<Map<String, Object>> updateGrnStatus(
            @RequestParam String dcNumber,
            @RequestParam(defaultValue = "Completed") String status) {
        deliveryChallanCreationIUMTService.updateGrnStatusToCompleted(dcNumber);
        return ResponseEntity.ok(Map.of(
                "message", "GRN Status updated to Completed for DC: " + dcNumber,
                "status", "success"
        ));
    }

    // =========================================================================
    // FRD: DC Summary IUMT (ITS-001 to GRN-005)
    // =========================================================================

    /**
     * FRD: ITD-001 to ITD-004 — DC Summary grouped by DC Number with drill-down
     */
    @GetMapping("/dc-summary-grouped")
    public ResponseEntity<List<Map<String, Object>>> getDcSummaryGrouped() {
        return ResponseEntity.ok(deliveryChallanCreationIUMTService.getDcSummaryGrouped());
    }

    /**
     * FRD: ITD-002 — Get detail view for a specific DC
     */
    @GetMapping("/dc-detail/{dcNumber}")
    public ResponseEntity<Map<String, Object>> getDcDetail(@PathVariable String dcNumber) {
        List<DeliveryChallanCreationIUMTEntity> items =
                deliveryChallanCreationIUMTService.getAllDeliveryChallansIUMT(null, null)
                        .stream()
                        .filter(d -> dcNumber.equals(d.getDCNumber()))
                        .toList();

        if (items.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        DeliveryChallanCreationIUMTEntity first = items.get(0);
        Map<String, Object> detail = new HashMap<>();
        detail.put("dcNumber", dcNumber);
        detail.put("timestamp", first.getTimestamp());
        detail.put("packingListNumber", first.getPackingListNumber());
        detail.put("unit", first.getUnit());
        detail.put("requestingCode", first.getRequestingCode());
        detail.put("requestingName", first.getRequestingName());
        detail.put("requestingBillingAddress", first.getRequestingBillingAddress());
        detail.put("requestingShippingAddress", first.getRequestingShippingAddress());
        detail.put("subTotalAmount", first.getSubTotalAmount());
        detail.put("igstPercent", first.getIgstPercent());
        detail.put("igstAmount", first.getIgstAmount());
        detail.put("totalAmount", first.getTotalAmount());
        detail.put("vehicleNumber", first.getVehicleNumberPackingAndDispatch());
        detail.put("ewayBillNumber", first.getEwayBillNumber());
        detail.put("grnStatus", first.getGrnStatus());
        detail.put("senderUnitGst", first.getSenderUnitGst());
        detail.put("senderUnitState", first.getSenderUnitState());
        detail.put("requestingUnitGst", first.getRequestingUnitGst());
        detail.put("requestingUnitState", first.getRequestingUnitState());
        detail.put("items", items);
        return ResponseEntity.ok(detail);
    }

    // =========================================================================
    // FRD: DCC-002/003/004 — Available Packing List Numbers
    // =========================================================================

    /**
     * FRD: DCC-004 — Get RFD list numbers NOT yet submitted in DC
     */
    @GetMapping("/available-packing-lists")
    public ResponseEntity<Map<String, Object>> getAvailablePackingLists(
            @RequestParam(required = false) String unit) {
        List<Map<String, String>> plns = deliveryChallanCreationIUMTService.getAvailablePackingListNumbers(unit);
        return ResponseEntity.ok(Map.of("data", plns, "status", "success"));
    }
}

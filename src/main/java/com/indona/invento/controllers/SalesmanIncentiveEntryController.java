package com.indona.invento.controllers;

import com.indona.invento.dto.CustomerOverdueResponseDTO;
import com.indona.invento.dto.SalesmanIncentiveUpdateDTO;
import com.indona.invento.entities.SalesmanIncentiveEntryEntity;
import com.indona.invento.services.SalesmanIncentiveEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/incentives")
@RequiredArgsConstructor
public class SalesmanIncentiveEntryController {

    private final SalesmanIncentiveEntryService service;

    @GetMapping
    public ResponseEntity<List<SalesmanIncentiveEntryEntity>> getAllIncentives() {
        List<SalesmanIncentiveEntryEntity> list = service.getAllIncentives();
        return ResponseEntity.ok(list);
    }

    @PutMapping("/update-payment")
    public ResponseEntity<SalesmanIncentiveUpdateDTO> updatePaymentDetails(@RequestBody SalesmanIncentiveUpdateDTO dto) {
        boolean updated = service.updatePaymentDetails(dto);
        if (updated) {
            return ResponseEntity.ok(dto); // ✅ Return DTO as response
        } else {
            return ResponseEntity.status(404).build(); // ❌ No body if not found
        }
    }

    @GetMapping(value = "/customer-overdue", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CustomerOverdueResponseDTO> getCustomerOverdueDetails(
            @RequestParam String customerName,
            @RequestParam String customerCode) {
        CustomerOverdueResponseDTO response = service.getCustomerOverdueDetails(customerName, customerCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-and-update-quotation-status")
    public ResponseEntity<?> checkOverdueAndUpdateQuotationStatus(
            @RequestParam String quotationNo,
            @RequestParam String customerName,
            @RequestParam String customerCode) {
        try {
            // Check customer overdue details
            CustomerOverdueResponseDTO overdue = service.getCustomerOverdueDetails(customerName, customerCode);

            // Determine status based on overdue amount
            boolean hasOverdue = overdue.getDueAmount() != null &&
                                 overdue.getDueAmount().compareTo(java.math.BigDecimal.ZERO) > 0;
            String statusToSet = hasOverdue ? "Hold" : "Active";

            // Call service to update quotation status
            boolean updated = service.updateQuotationStatus(quotationNo, statusToSet, hasOverdue);

            if (updated) {
                return ResponseEntity.ok(java.util.Map.of(
                        "message", "✅ Quotation status updated successfully",
                        "quotationNo", quotationNo,
                        "status", statusToSet,
                        "hasOverdue", hasOverdue,
                        "dueAmount", overdue.getDueAmount()
                ));
            } else {
                return ResponseEntity.status(404).body(java.util.Map.of(
                        "error", "❌ Quotation not found: " + quotationNo
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "❌ Error updating quotation: " + e.getMessage()
            ));
        }
    }
}

package com.indona.invento.controllers;

import com.indona.invento.dto.BillLineDetailsDTO;
import com.indona.invento.dto.BillingSummaryDTO;
import com.indona.invento.entities.BillingSummaryEntity;
import com.indona.invento.entities.PackingSubmission;
import com.indona.invento.services.BillingSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/billing-summary")
public class BillingSummaryController {

    @Autowired
    private BillingSummaryService service;

    @PostMapping("/save")
    public ResponseEntity<BillingSummaryEntity> save(@RequestBody BillingSummaryDTO dto) {
        BillingSummaryEntity saved = service.saveBilling(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BillingSummaryEntity>> getAll() {
        List<BillingSummaryEntity> allEntries = service.getAllBillings();
        return ResponseEntity.ok(allEntries);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            BillingSummaryEntity entity = service.getBillingById(id);
            if (entity == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(entity);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/packing-lookup")
    public ResponseEntity<PackingSubmission> getPackingDetails(
            @RequestParam String soNumber,
            @RequestParam String lineNumber
    ) {
        PackingSubmission result = service.getPackingDetailsBySoAndLine(soNumber, lineNumber);
        if (result == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllBillings() {
        service.deleteAllBillings();
        return ResponseEntity.ok("✅ All billing summary entries deleted successfully.");
    }

    // ==================== FRD v3.0: New Endpoints ====================

    /**
     * BS-BR-001: Get available RFD List Numbers (not already used in billing_summary)
     */
    @GetMapping("/available-rfd-list-numbers")
    public ResponseEntity<List<String>> getAvailableRfdListNumbers() {
        List<String> available = service.getAvailableRfdListNumbers();
        return ResponseEntity.ok(available);
    }

    /**
     * BS-BR-008: Update LR Number — auto-populates SO Summary
     */
    @PutMapping("/{id}/lr-number")
    public ResponseEntity<?> updateLrNumber(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String lrNumber = body.get("lrNumber");
            if (lrNumber == null || lrNumber.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "LR Number is required"));
            }
            BillingSummaryEntity updated = service.updateLrNumber(id, lrNumber.trim());
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Upload Invoice PDF
     */
    @PostMapping("/{id}/upload-invoice")
    public ResponseEntity<?> uploadInvoice(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "File is required"));
            }
            // Validate PDF format
            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Only PDF files are allowed"));
            }
            BillingSummaryEntity updated = service.uploadInvoicePdf(id, file);
            return ResponseEntity.ok(Map.of("success", true, "invoicePdfUrl", updated.getInvoicePdfUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Get Invoice PDF URL
     */
    @GetMapping("/{id}/invoice-pdf")
    public ResponseEntity<?> getInvoicePdf(@PathVariable Long id) {
        try {
            BillingSummaryEntity entity = service.getBillingById(id);
            if (entity == null || entity.getInvoicePdfUrl() == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(Map.of("success", true, "invoicePdfUrl", entity.getInvoicePdfUrl()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update billing entry
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBilling(@PathVariable Long id, @RequestBody BillingSummaryDTO dto) {
        try {
            BillingSummaryEntity updated = service.updateBilling(id, dto);
            return ResponseEntity.ok(Map.of("success", true, "data", updated));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    // ==================== Stock Return APIs (existing) ====================

    @GetMapping("/stock-return/invoice-numbers")
    public ResponseEntity<List<String>> getCompletedInvoiceNumbers() {
        List<String> invoiceNumbers = service.getCompletedInvoiceNumbers();
        return ResponseEntity.ok(invoiceNumbers);
    }

    @GetMapping("/stock-return/so-numbers")
    public ResponseEntity<List<String>> getSoNumbersByInvoice(@RequestParam String invoiceNumber) {
        List<String> soNumbers = service.getSoNumbersByInvoice(invoiceNumber);
        return ResponseEntity.ok(soNumbers);
    }

    @GetMapping("/stock-return/line-numbers")
    public ResponseEntity<List<String>> getLineNumbers(
            @RequestParam String invoiceNumber,
            @RequestParam String soNumber) {
        List<String> lineNumbers = service.getLineNumbersByInvoiceAndSo(invoiceNumber, soNumber);
        return ResponseEntity.ok(lineNumbers);
    }

    @GetMapping("/stock-return/line-details")
    public ResponseEntity<BillLineDetailsDTO> getLineDetails(
            @RequestParam String invoiceNumber,
            @RequestParam String soNumber,
            @RequestParam String lineNumber) {
        BillLineDetailsDTO details = service.getLineDetails(invoiceNumber, soNumber, lineNumber);
        return ResponseEntity.ok(details);
    }

    /**
     * Get invoices by unit (for Sales Return — Module 3)
     */
    @GetMapping("/invoices-by-unit")
    public ResponseEntity<List<String>> getInvoicesByUnit(@RequestParam String unit) {
        List<String> invoices = service.getInvoicesByUnit(unit);
        return ResponseEntity.ok(invoices);
    }
}

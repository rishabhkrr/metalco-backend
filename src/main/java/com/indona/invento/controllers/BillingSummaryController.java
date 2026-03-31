package com.indona.invento.controllers;


import com.indona.invento.dto.BillLineDetailsDTO;
import com.indona.invento.dto.BillingSummaryDTO;
import com.indona.invento.entities.BillingSummaryEntity;
import com.indona.invento.entities.PackingSubmission;
import com.indona.invento.services.BillingSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    // ========== Stock Return APIs ==========

    /**
     * Get all completed invoice numbers for stock return dropdown
     */
    @GetMapping("/stock-return/invoice-numbers")
    public ResponseEntity<List<String>> getCompletedInvoiceNumbers() {
        List<String> invoiceNumbers = service.getCompletedInvoiceNumbers();
        return ResponseEntity.ok(invoiceNumbers);
    }

    /**
     * Get SO numbers for a given invoice number
     */
    @GetMapping("/stock-return/so-numbers")
    public ResponseEntity<List<String>> getSoNumbersByInvoice(@RequestParam String invoiceNumber) {
        List<String> soNumbers = service.getSoNumbersByInvoice(invoiceNumber);
        return ResponseEntity.ok(soNumbers);
    }

    /**
     * Get line numbers for a given invoice and SO number
     */
    @GetMapping("/stock-return/line-numbers")
    public ResponseEntity<List<String>> getLineNumbers(
            @RequestParam String invoiceNumber,
            @RequestParam String soNumber) {
        List<String> lineNumbers = service.getLineNumbersByInvoiceAndSo(invoiceNumber, soNumber);
        return ResponseEntity.ok(lineNumbers);
    }

    /**
     * Get line details for auto-filling the stock return form
     */
    @GetMapping("/stock-return/line-details")
    public ResponseEntity<BillLineDetailsDTO> getLineDetails(
            @RequestParam String invoiceNumber,
            @RequestParam String soNumber,
            @RequestParam String lineNumber) {
        BillLineDetailsDTO details = service.getLineDetails(invoiceNumber, soNumber, lineNumber);
        return ResponseEntity.ok(details);
    }

}

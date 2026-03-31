package com.indona.invento.controllers;

import com.indona.invento.dto.*;
import com.indona.invento.services.GRNService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grn")
@RequiredArgsConstructor
public class GRNController {

    private final GRNService grnService;

    // invoice dropdown
    @GetMapping("/invoice-numbers")
    public ResponseEntity<List<InvoiceDropdownDTO>> invoiceNumbers() {
        return ResponseEntity.ok(grnService.getInvoiceNumbersForDropdown());
    }
    
    // New endpoint to get all invoice numbers from GRN summary
    @GetMapping("/all-invoice-numbers")
    public ResponseEntity<List<String>> allInvoiceNumbers() {
        return ResponseEntity.ok(grnService.getAllInvoiceNumbers());
    }

    @GetMapping("/fetch-details-by-invoice")
    public ResponseEntity<GRNResponseDTO> fetchByInvoice(@RequestParam String invoiceNumber) {
        return ResponseEntity.ok(grnService.fetchDetailsByInvoice(invoiceNumber));
    }

    @GetMapping("/po-numbers")
    public ResponseEntity<List<String>> poNumbersByInvoice(@RequestParam String invoiceNumber) {
        return ResponseEntity.ok(grnService.getPoNumbersByInvoice(invoiceNumber));
    }

    @GetMapping("/purchase-line-details")
    public ResponseEntity<List<PurchaseLineDetailsDTO>> purchaseLineDetails(
            @RequestParam String invoiceNumber,
            @RequestParam String poNumber) {
        return ResponseEntity.ok(grnService.getPurchaseLineDetails(invoiceNumber, poNumber));
    }

    // get PO items for table population
    @GetMapping("/items/by-po")
    public ResponseEntity<List<GRNItemRequestDTO>> itemsByPo(@RequestParam String poNumber) {
        return ResponseEntity.ok(grnService.getItemsByPoNumber(poNumber));
    }

    // get GRN items by GRN ID
    @GetMapping("/{id}/items")
    public ResponseEntity<List<GRNItemRequestDTO>> getItemsByGrnId(@PathVariable Long id) {
        return ResponseEntity.ok(grnService.getItemsByGrnId(id));
    }

    // create grn
    @PostMapping("/create")
    public ResponseEntity<GRNResponseDTO> create(@RequestBody GRNRequestDTO request) {
        return ResponseEntity.ok(grnService.createGRN(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GRNResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(grnService.getGRNById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<GRNResponseDTO>> getAll() {
        return ResponseEntity.ok(grnService.getAllGRNs());
    }

    // material unloading (button)
    @PutMapping("/mark-unloaded/{id}")
    public ResponseEntity<GRNResponseDTO> markUnloaded(@PathVariable Long id, @RequestParam(required = false) String notes) {
        return ResponseEntity.ok(grnService.markMaterialUnloaded(id, notes));
    }

    @PostMapping("/save-with-po")
    public ResponseEntity<GRNResponseDTO> saveWithPo(@RequestBody GRNRequestDTO request) {
        return ResponseEntity.ok(grnService.saveGrnWithPo(request));
    }

    @PostMapping("/save-without-po")
    public ResponseEntity<GRNResponseDTO> saveWithoutPo(@RequestBody GRNRequestDTO request) {
        return ResponseEntity.ok(grnService.saveGrnWithoutPo(request));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<GRNResponseDTO> approveGRN(@PathVariable Long id, @RequestBody GRNApprovalRequestDTO request) {
        // Set the GRN ID from path variable
        request.setGrnId(id);
        return ResponseEntity.ok(grnService.approveGRN(request));
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<GRNResponseDTO> rejectGRN(@PathVariable Long id) {
        return ResponseEntity.ok(grnService.rejectGRN(id));
    }
}

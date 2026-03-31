package com.indona.invento.controllers;

import com.indona.invento.dto.PurchaseCreditDebitNoteDTO;
import com.indona.invento.entities.PurchaseCreditDebitNoteEntity;
import com.indona.invento.services.PurchaseCreditDebitNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase-credit-debit-note")
@RequiredArgsConstructor
public class PurchaseCreditDebitNoteController {

    private final PurchaseCreditDebitNoteService service;

    @PostMapping("/create")
    public ResponseEntity<PurchaseCreditDebitNoteEntity> create(@RequestBody PurchaseCreditDebitNoteDTO dto) {
        return ResponseEntity.ok(service.createCreditDebitNote(dto));
    }

    @GetMapping("/summary")
    public ResponseEntity<List<PurchaseCreditDebitNoteEntity>> getAllCreditDebitNotes() {
        return ResponseEntity.ok(service.getAllCreditDebitNotes());
    }

    @GetMapping("/by-invoice")
    public ResponseEntity<List<PurchaseCreditDebitNoteEntity>> getByInvoice(@RequestParam String invoiceNumber) {
        return ResponseEntity.ok(service.getByInvoiceNumber(invoiceNumber));
    }

    @GetMapping("/by-po")
    public ResponseEntity<List<PurchaseCreditDebitNoteEntity>> getByPo(@RequestParam String poNumber) {
        return ResponseEntity.ok(service.getByPoNumber(poNumber));
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<PurchaseCreditDebitNoteEntity>> getByType(@RequestParam String transactionType) {
        return ResponseEntity.ok(service.getByTransactionType(transactionType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseCreditDebitNoteEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
}


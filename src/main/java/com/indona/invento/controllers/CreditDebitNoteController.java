package com.indona.invento.controllers;

import com.indona.invento.dto.CreditDebitNoteDTO;
import com.indona.invento.entities.CreditDebitNoteEntity;
import com.indona.invento.services.CreditDebitNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Credit Note and Debit Note operations (except sales return)
 */
@RestController
@RequestMapping("/credit-debit-note")
@CrossOrigin(origins = "*")
public class CreditDebitNoteController {

    @Autowired
    private CreditDebitNoteService service;

    /**
     * Create a new credit or debit note
     * POST /api/metalco/credit-debit-note/create
     */
    @PostMapping("/create")
    public ResponseEntity<CreditDebitNoteEntity> createCreditDebitNote(@RequestBody CreditDebitNoteDTO dto) {
        try {
            CreditDebitNoteEntity created = service.createCreditDebitNote(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Get all credit/debit notes
     * GET /api/metalco/credit-debit-note/summary
     */
    @GetMapping("/summary")
    public ResponseEntity<List<CreditDebitNoteEntity>> getAllCreditDebitNotes() {
        List<CreditDebitNoteEntity> notes = service.getAllCreditDebitNotes();
        return ResponseEntity.ok(notes);
    }

    /**
     * Get credit/debit notes by invoice number
     * GET /api/metalco/credit-debit-note/by-invoice?invoiceNumber=XXX
     */
    @GetMapping("/by-invoice")
    public ResponseEntity<List<CreditDebitNoteEntity>> getCreditDebitNotesByInvoice(@RequestParam String invoiceNumber) {
        List<CreditDebitNoteEntity> notes = service.getCreditDebitNotesByInvoice(invoiceNumber);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get credit/debit notes by SO number
     * GET /api/metalco/credit-debit-note/by-so?soNumber=XXX
     */
    @GetMapping("/by-so")
    public ResponseEntity<List<CreditDebitNoteEntity>> getCreditDebitNotesBySo(@RequestParam String soNumber) {
        List<CreditDebitNoteEntity> notes = service.getCreditDebitNotesBySo(soNumber);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get credit/debit notes by transaction type
     * GET /api/metalco/credit-debit-note/by-type?transactionType=CREDIT
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<CreditDebitNoteEntity>> getCreditDebitNotesByType(@RequestParam String transactionType) {
        List<CreditDebitNoteEntity> notes = service.getCreditDebitNotesByType(transactionType);
        return ResponseEntity.ok(notes);
    }

    /**
     * Get credit/debit note by ID
     * GET /api/metalco/credit-debit-note/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CreditDebitNoteEntity> getCreditDebitNoteById(@PathVariable Long id) {
        try {
            CreditDebitNoteEntity note = service.getCreditDebitNoteById(id);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllCreditDebitNotes() {
        try {
            service.deleteAll();
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ All credit/debit notes deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "❌ Failed to delete all credit/debit notes",
                    "details", e.getMessage()
            ));
        }
    }
}

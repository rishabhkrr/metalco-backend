package com.indona.invento.controllers;

import com.indona.invento.dto.BillStockReturnDTO;
import com.indona.invento.entities.BillStockReturnEntity;
import com.indona.invento.services.BillStockReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bill-stock-return")
public class BillStockReturnController {

    @Autowired
    private BillStockReturnService service;

    /**
     * Create a new stock return record
     */
    @PostMapping("/create")
    public ResponseEntity<BillStockReturnEntity> createStockReturn(@RequestBody BillStockReturnDTO dto) {
        try {
            BillStockReturnEntity created = service.createStockReturn(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace to console
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Get all stock return records for summary display
     */
    @GetMapping("/summary")
    public ResponseEntity<List<BillStockReturnEntity>> getAllStockReturns() {
        List<BillStockReturnEntity> stockReturns = service.getAllStockReturns();
        return ResponseEntity.ok(stockReturns);
    }

    /**
     * Get stock returns by invoice number
     */
    @GetMapping("/by-invoice")
    public ResponseEntity<List<BillStockReturnEntity>> getByInvoice(@RequestParam String invoiceNumber) {
        List<BillStockReturnEntity> stockReturns = service.getStockReturnsByInvoice(invoiceNumber);
        return ResponseEntity.ok(stockReturns);
    }

    /**
     * Get stock returns by SO number
     */
    @GetMapping("/by-so")
    public ResponseEntity<List<BillStockReturnEntity>> getBySo(@RequestParam String soNumber) {
        List<BillStockReturnEntity> stockReturns = service.getStockReturnsBySo(soNumber);
        return ResponseEntity.ok(stockReturns);
    }

    /**
     * Get stock returns by type (REJECTION/GENERAL)
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<BillStockReturnEntity>> getByType(@RequestParam String stockSelection) {
        List<BillStockReturnEntity> stockReturns = service.getStockReturnsByType(stockSelection);
        return ResponseEntity.ok(stockReturns);
    }

    /**
     * Get stock return by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BillStockReturnEntity> getById(@PathVariable Long id) {
        try {
            BillStockReturnEntity stockReturn = service.getStockReturnById(id);
            return ResponseEntity.ok(stockReturn);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllStockReturns() {
        try {
            service.deleteAllStockReturns();
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ All bill stock returns deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "❌ Failed to delete all bill stock returns",
                    "details", e.getMessage()
            ));
        }
    }
}


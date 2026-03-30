package com.indona.invento.controllers;

import com.indona.invento.dto.PurchaseReturnDTO;
import com.indona.invento.entities.PurchaseReturnEntity;
import com.indona.invento.services.PurchaseReturnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchase-return")
public class PurchaseReturnController {

    @Autowired
    private PurchaseReturnService service;

    /**
     * Create a new purchase return record
     */
    @PostMapping("/create")
    public ResponseEntity<PurchaseReturnEntity> createPurchaseReturn(@RequestBody PurchaseReturnDTO dto) {
        try {
            PurchaseReturnEntity created = service.createPurchaseReturn(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Get all purchase return records (summary)
     */
    @GetMapping("/all")
    public ResponseEntity<List<PurchaseReturnEntity>> getAllPurchaseReturns() {
        List<PurchaseReturnEntity> allReturns = service.getAllPurchaseReturns();
        return ResponseEntity.ok(allReturns);
    }

    /**
     * Get purchase return by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseReturnEntity> getPurchaseReturnById(@PathVariable Long id) {
        try {
            PurchaseReturnEntity purchaseReturn = service.getPurchaseReturnById(id);
            return ResponseEntity.ok(purchaseReturn);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}


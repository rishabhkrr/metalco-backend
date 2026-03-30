package com.indona.invento.controllers;

import com.indona.invento.dto.DeliveryChallanItemDetailsDTO;
import com.indona.invento.dto.DeliveryChallanMergedDTO;
import com.indona.invento.entities.DeliveryChallanJWEntity;
import com.indona.invento.services.DeliveryChallanJWService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/delivery-challan-jw")
public class DeliveryChallanJWController {

    @Autowired
    private DeliveryChallanJWService service;

    // Multiple save
    @PostMapping("/bulk-save")
    public ResponseEntity<List<DeliveryChallanJWEntity>> bulkSave(@RequestBody List<DeliveryChallanJWEntity> challans) {
        return ResponseEntity.ok(service.saveAll(challans));
    }

    // Single save
    @PostMapping("/save")
    public ResponseEntity<DeliveryChallanJWEntity> save(@RequestBody DeliveryChallanJWEntity challan) {
        return ResponseEntity.ok(service.save(challan));
    }

    //  Get all challans
    @GetMapping("/all")
    public ResponseEntity<List<DeliveryChallanJWEntity>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // Get challan by MEDC Number
    @GetMapping("/by-medc-number")
    public ResponseEntity<DeliveryChallanJWEntity> getByMedcNumber(@RequestParam String medcNumber) {
        return ResponseEntity.ok(service.getByMedcNumber(medcNumber));
    }
    
    // Update challan by ID (PUT)
    @PutMapping("/update/{id}")
    public ResponseEntity<DeliveryChallanJWEntity> updateById(
            @PathVariable Long id,
            @RequestBody DeliveryChallanJWEntity updatedChallan) {
        return ResponseEntity.ok(service.updateById(id, updatedChallan));
    }

    // Delete challan by ID (DELETE)
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteById(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.ok("Delivery Challan deleted successfully with id: " + id);
    }
    
    // Get challan by ID
    @GetMapping("/by-id/{id}")
    public ResponseEntity<DeliveryChallanJWEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }
    
    @GetMapping("/medc-numbers")
    public ResponseEntity<List<String>> getAllMedcNumbers() {
        return ResponseEntity.ok(service.getAllMedcNumbers());
    }
    
    @GetMapping("/merged-by-medc")
    public ResponseEntity<List<DeliveryChallanMergedDTO>> getMergedByMedcNumbers(
            @RequestParam List<String> medcNumbers) {
        return ResponseEntity.ok(service.getMergedDataByMedcNumbers(medcNumbers));
    }
    
    @GetMapping("/item-details")
    public ResponseEntity<List<DeliveryChallanItemDetailsDTO>> getItemDetailsByMedcNumber(@RequestParam String medcNumber) {
        return ResponseEntity.ok(service.getItemDetailsByMedcNumber(medcNumber));
    }
    
    @GetMapping("/dimensions-by-medc")
    public ResponseEntity<List<String>> getDimensionsByMedc(@RequestParam String medcNumber) {
        return ResponseEntity.ok(service.getDimensionsByMedcNumber(medcNumber));
    }
    
    @GetMapping("/item-details-by-medc-dimension")
    public ResponseEntity<List<DeliveryChallanItemDetailsDTO>> getItemDetailsByMedcAndDimension(
            @RequestParam String medcNumber,
            @RequestParam String dimension) {

        return ResponseEntity.ok(service.getItemDetailsByMedcAndDimension(medcNumber, dimension));
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllDeliveryChallanJW() {
        try {
            service.deleteAll();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All delivery challan JW deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all delivery challan JW",
                    "details", e.getMessage()
            ));
        }
    }

}
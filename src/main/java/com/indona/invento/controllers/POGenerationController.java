package com.indona.invento.controllers;

import com.indona.invento.dto.POGenerationDTO;
import com.indona.invento.dto.POGenerationResponseDTO;
import com.indona.invento.entities.POGenerationEntity;
import com.indona.invento.services.POGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/po-generation")
@RequiredArgsConstructor
public class POGenerationController {

    private final POGenerationService service;

    @PostMapping("/create")
    public ResponseEntity<POGenerationEntity> createPO(@RequestBody POGenerationDTO dto) {
        POGenerationEntity saved = service.savePOGeneration(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/by-supplier")
    public ResponseEntity<List<POGenerationEntity>> getPOsBySupplier(
            @RequestParam String supplierCode,
            @RequestParam String supplierName
    ) {
        List<POGenerationEntity> matchingPOs = service.getPOsBySupplier(supplierCode, supplierName);
        return ResponseEntity.ok(matchingPOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<POGenerationEntity> getPOById(@PathVariable Long id) {
        POGenerationEntity po = service.getPOById(id);
        return ResponseEntity.ok(po);
    }

    @GetMapping("/by-po-number")
    public ResponseEntity<?> getPOByPoNumber(@RequestParam String poNumber) {
        try {
            POGenerationEntity po = service.getPOByPoNumber(poNumber);
            return ResponseEntity.ok(po);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<POGenerationResponseDTO>> getAllPOs() {
        List<POGenerationResponseDTO> result = service.getAllPOsWithoutPagination();
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-remarks")
    public ResponseEntity<?> updateRemarksByPoNumber(
            @RequestParam String poNumber,
            @RequestParam String remarks
    ) {
        try {
            POGenerationEntity updated = service.updateRemarks(poNumber, remarks);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePO(@PathVariable Long id, @RequestBody POGenerationDTO dto) {
        try {
            POGenerationEntity updated = service.updatePOGeneration(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<List<POGenerationEntity>> filterPOGenerationsByDate(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        List<POGenerationEntity> filtered = service.getPOGenerationsBetweenDates(from, to);
        return ResponseEntity.ok(filtered);
    }


    @PutMapping("/place/{poNumber}")
    public ResponseEntity<POGenerationEntity> placePO(@PathVariable String poNumber) {
        POGenerationEntity updatedPO = service.updatePOStatusAndItems(poNumber);
        return ResponseEntity.ok(updatedPO);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllPOGenerations() {
        try {
            service.deleteAllPOGenerations();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All PO generations deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all PO generations",
                    "details", e.getMessage()
            ));
        }
    }
}

package com.indona.invento.controllers;

import com.indona.invento.dto.PORequestDTO;
import com.indona.invento.dto.SupplierCodeNameDTO;
import com.indona.invento.dto.UnitNameCodeDTO;
import com.indona.invento.dto.PORequestDetailsDto;
import com.indona.invento.entities.PORequestEntity;
import com.indona.invento.services.PORequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/po")
public class PORequestController {

    @Autowired
    private PORequestService service;

    @PostMapping
    public ResponseEntity<PORequestEntity> create(@RequestBody PORequestDTO request) {
        return ResponseEntity.ok(service.createPORequest(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PORequestEntity> update(@PathVariable Long id, @RequestBody PORequestDTO request) {
        return ResponseEntity.ok(service.updatePORequest(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PORequestEntity> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.deletePORequest(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PORequestEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getPORequestById(id));
    }

    @GetMapping
    public ResponseEntity<List<PORequestEntity>> getAll() {
        return ResponseEntity.ok(service.getAllPORequests());
    }

    @GetMapping("/po-by-supplier")
    public ResponseEntity<List<PORequestEntity>> getPOsBySupplier(
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String unitCode
    ) {
        List<PORequestEntity> matchingPOs = service.getPOsBySupplier(supplierCode, supplierName, unitCode);
        return ResponseEntity.ok(matchingPOs);
    }


    @GetMapping("/supplier-codes")
    public ResponseEntity<List<SupplierCodeNameDTO>> getAllSupplierCodeNamePairs() {
        return ResponseEntity.ok(service.getAllSupplierCodeNamePairs());
    }


    @GetMapping("/unit-names-with-codes")
    public ResponseEntity<List<UnitNameCodeDTO>> getUnitNamesWithCodes() {
        return ResponseEntity.ok(service.getAllUnitNamesWithCodes());
    }


    @GetMapping("/filter")
    public ResponseEntity<List<PORequestEntity>> filterPORequestsByDate(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date to) {

        List<PORequestEntity> filtered = service.getPORequestsBetweenDates(from, to);
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/details")
    public ResponseEntity<?> getPORequestDetails(@RequestParam String prNumber) {
        try {
            PORequestDetailsDto details = service.getPORequestDetailsByPrNumber(prNumber);
            return ResponseEntity.ok(details);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllPORequests() {
        try {
            service.deleteAllPORequests();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All PO requests deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all PO requests",
                    "details", e.getMessage()
            ));
        }
    }

}

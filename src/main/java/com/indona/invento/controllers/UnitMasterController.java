package com.indona.invento.controllers;

import com.indona.invento.dto.UnitMasterDto;
import com.indona.invento.entities.UnitMasterEntity;
import com.indona.invento.services.UnitMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/unit-master")
public class UnitMasterController {

    @Autowired
    private UnitMasterService service;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody UnitMasterDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody UnitMasterDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Unit deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Unit not found with ID: " + id));
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllUnits() {
        try {
            List<UnitMasterEntity> units = service.getAllWithoutPagination();
            return ResponseEntity.ok(units);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/code/{unitCode}")
    public ResponseEntity<?> getByUnitCode(@PathVariable String unitCode) {
        try {
            UnitMasterEntity entity = service.getByUnitCode(unitCode);
            return ResponseEntity.ok(entity);  // <-- return full object
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/name/{unitName}")
    public ResponseEntity<?> getByUnitName(@PathVariable String unitName) {
        try {
            UnitMasterEntity entity = service.getByUnitName(unitName);
            return ResponseEntity.ok(entity); // ✅ return full object instead of just unitCode
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/codes")
    public ResponseEntity<List<String>> getAllUnitCodes() {
        List<String> codes = service.getAllApprovedUnitCodes();
        return ResponseEntity.ok(codes);
    }


    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<UnitMasterEntity>> getAllUnitsWithoutPagination() {
        try {
            List<UnitMasterEntity> units = service.getAllWithoutPagination();
            return ResponseEntity.ok(units);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/supplier-codes-by-name")
    public ResponseEntity<List<String>> getSupplierCodesByName(@RequestParam String unitName) {
        List<String> codes = service.getUnitCodesByName(unitName);
        return ResponseEntity.ok(codes);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveUnit(@PathVariable Long id) {
        try {
            UnitMasterEntity approvedUnit = service.approveUnit(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ Unit approved successfully",
                    "id", approvedUnit.getId(),
                    "unitName", approvedUnit.getUnitName(),
                    "status", approvedUnit.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to approve unit",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectUnit(@PathVariable Long id) {
        try {
            UnitMasterEntity rejectedUnit = service.rejectUnit(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "❌ Unit rejected successfully",
                    "id", rejectedUnit.getId(),
                    "unitName", rejectedUnit.getUnitName(),
                    "status", rejectedUnit.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to reject unit",
                    "details", e.getMessage()
            ));
        }
    }

}

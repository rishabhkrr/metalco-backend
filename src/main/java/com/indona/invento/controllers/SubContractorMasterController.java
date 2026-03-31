package com.indona.invento.controllers;

import com.indona.invento.dto.SubContractorMasterDto;
import com.indona.invento.entities.SubContractorMasterEntity;
import com.indona.invento.services.SubContractorMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sub-contractor-master")
public class SubContractorMasterController {

    @Autowired
    private SubContractorMasterService service;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody SubContractorMasterDto dto) {
        return ResponseEntity.ok(service.save(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SubContractorMasterDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSubContractors() {
        try {
            List<SubContractorMasterEntity> subcontractors = service.getAllWithoutPagination();
            return ResponseEntity.ok(subcontractors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Sub-contractor deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Sub-contractor not found with ID: " + id));
        }
    }

    @GetMapping("/checkName")
    public ResponseEntity<Map<String, Object>> checkSubContractorName(@RequestParam String name) {
        boolean exists = service.isSubContractorNameExists(name);
        return ResponseEntity.ok(Map.of(
                "subContractorName", name,
                "exists", exists
        ));
    }

    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<SubContractorMasterEntity>> getAllSubContractorsWithoutPagination() {
        try {
            List<SubContractorMasterEntity> subcontractors = service.getAllWithoutPagination();
            return ResponseEntity.ok(subcontractors);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
 // Get all SubContractor Codes
    @GetMapping("/dropdown/codes")
    public ResponseEntity<List<String>> getAllCodes() {
        return ResponseEntity.ok(service.getAllApprovedSubContractorCodes());
    }

    // Get all SubContractor Names
    @GetMapping("/dropdown/names")
    public ResponseEntity<List<String>> getAllNames() {
        return ResponseEntity.ok(service.getAllSubContractorNames());
    }

    // Get details by SubContractor Code → returns Name + Primary
    @GetMapping("/dropdown/by-code")
    public ResponseEntity<Map<String, Object>> getByCode(@RequestParam String code) {
        return ResponseEntity.ok(service.getDetailsByCode(code));
    }

    //  Get details by SubContractor Name → returns Code + Primary
    @GetMapping("/dropdown/by-name")
    public ResponseEntity<Map<String, Object>> getByName(@RequestParam String name) {
        return ResponseEntity.ok(service.getDetailsByName(name));
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveSubContractor(@PathVariable Long id) {
        try {
            SubContractorMasterEntity approvedSubContractor = service.approveSubContractor(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ SubContractor approved successfully",
                    "id", approvedSubContractor.getId(),
                    "subContractorName", approvedSubContractor.getSubContractorName(),
                    "status", approvedSubContractor.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to approve subcontractor",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectSubContractor(@PathVariable Long id) {
        try {
            SubContractorMasterEntity rejectedSubContractor = service.rejectSubContractor(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "❌ SubContractor rejected successfully",
                    "id", rejectedSubContractor.getId(),
                    "subContractorName", rejectedSubContractor.getSubContractorName(),
                    "status", rejectedSubContractor.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to reject subcontractor",
                    "details", e.getMessage()
            ));
        }
    }

}

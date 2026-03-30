package com.indona.invento.controllers;

import com.indona.invento.dto.SalesmanMasterDto;
import com.indona.invento.entities.SalesmanMasterEntity;
import com.indona.invento.services.SalesmanMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/salesman-master")
public class SalesmanMasterController {

    @Autowired
    private SalesmanMasterService service;

    @PostMapping("/create")
    public ResponseEntity<SalesmanMasterEntity> createSalesman(@RequestBody SalesmanMasterDto dto) {
        SalesmanMasterEntity saved = service.createSalesman(dto);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalesmanMasterEntity> update(@PathVariable Long id,
                                                       @RequestBody SalesmanMasterDto dto) {
        SalesmanMasterEntity updated = service.update(id, dto);
        return ResponseEntity.ok(updated);
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllSalesmen() {
        try {
            List<SalesmanMasterEntity> salesmen = service.getAllWithoutPagination();
            return ResponseEntity.ok(salesmen);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<SalesmanMasterEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> delete(@PathVariable Long id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<SalesmanMasterEntity>> getAllSalesmenWithoutPagination() {
        try {
            List<SalesmanMasterEntity> salesmen = service.getAllWithoutPagination();
            return ResponseEntity.ok(salesmen);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveSalesman(@PathVariable Long id) {
        try {
            SalesmanMasterEntity approvedSalesman = service.approveSalesman(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ Salesman approved successfully",
                    "id", approvedSalesman.getId(),
                    "userName", approvedSalesman.getUserName(),
                    "status", approvedSalesman.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to approve salesman",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectSalesman(@PathVariable Long id) {
        try {
            SalesmanMasterEntity rejectedSalesman = service.rejectSalesman(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "❌ Salesman rejected successfully",
                    "id", rejectedSalesman.getId(),
                    "userName", rejectedSalesman.getUserName(),
                    "status", rejectedSalesman.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to reject salesman",
                    "details", e.getMessage()
            ));
        }
    }

}


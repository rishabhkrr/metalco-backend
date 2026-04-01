package com.indona.invento.controllers;

import com.indona.invento.dto.ProductionScheduleDto;
import com.indona.invento.entities.ProductionScheduleEntity;
import com.indona.invento.services.ProductionScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product-schedule")
public class ProductionScheduleController {

    @Autowired
    private ProductionScheduleService productScheduleService;

    @PostMapping
    public ResponseEntity<ProductionScheduleEntity> createProduction(@RequestBody ProductionScheduleEntity production) {
        ProductionScheduleEntity createdProduction = productScheduleService.createProduction(production);
        return new ResponseEntity<>(createdProduction, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduction(@PathVariable Long id) {
        productScheduleService.deleteProduction(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Get distinct product categories for filter dropdown
     */
    @GetMapping("/product-categories")
    public ResponseEntity<List<String>> getDistinctProductCategories() {
        List<String> categories = productScheduleService.getDistinctProductCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/all")
    public List<ProductionScheduleEntity> getAllProductionSchedule(
            @RequestParam(required = false) String productCategory) {
        if (productCategory != null && !productCategory.isBlank()) {
            return productScheduleService.getAllProductionScheduleByCategory(productCategory.trim());
        }
        return productScheduleService.getAllProductionSchedule();
    }

    @GetMapping("/marking-cutting-product-schedules")
    public List<ProductionScheduleEntity> getMarkingAndCuttingSchedules() {
        return productScheduleService.getMarkingAndCuttingSchedules();
    }

    @GetMapping("/machine-names-dropdown")
    public ResponseEntity<Map<String, Object>> getMachineNames() {
        List<Map<String, String>> name = productScheduleService.getMachineNames();
        return ResponseEntity.ok(Map.of("data", name, "status", "success"));
    }

    @PutMapping("/production-schedules/{id}")
    public ProductionScheduleEntity updateProduction(
            @PathVariable Long id,
            @RequestBody ProductionScheduleDto dto) {

        return productScheduleService.updateProductionSchedule(id, dto);
    }

    @GetMapping("/machine-details")
    public ResponseEntity<Map<String, Object>> getMachineDetails(@RequestParam String machineName) {
        Map<String, Object> result = productScheduleService.getMachineDetails(machineName);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all PENDING production schedules with SO Number + Line Number
     * Usage: GET /product-schedule/pending-so-line-numbers
     */
    @GetMapping("/pending-so-line-numbers")
    public ResponseEntity<?> getPendingSoAndLineNumbers() {
        try {
            List<Map<String, String>> pendingSchedules = productScheduleService.getPendingSoAndLineNumbers();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "totalCount", pendingSchedules.size(),
                    "data", pendingSchedules
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Failed to fetch pending schedules",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Update production schedule machine details (machineName, targetBladeSpeed, targetFeed)
     * Usage: PUT /product-schedule/update-machine-details?soNumber=...&lineNumber=...&machineName=...&targetBladeSpeed=2500&targetFeed=50
     */
    @PutMapping("/update-machine-details")
    public ResponseEntity<?> updateMachineDetails(
            @RequestParam String soNumber,
            @RequestParam String lineNumber,
            @RequestParam String machineName,
            @RequestParam Double targetBladeSpeed,
            @RequestParam Double targetFeed) {
        try {
            ProductionScheduleEntity updated = productScheduleService.updateMachineDetails(
                    soNumber,
                    lineNumber,
                    machineName,
                    targetBladeSpeed,
                    targetFeed
            );

            if (updated == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "error", "Production schedule not found for SO: " + soNumber + ", Line: " + lineNumber
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Machine details updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Failed to update machine details",
                    "details", e.getMessage()
            ));
        }
    }
}

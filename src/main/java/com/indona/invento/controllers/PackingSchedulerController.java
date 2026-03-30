package com.indona.invento.controllers;

import com.indona.invento.dto.PackingInstructionDTO;
import com.indona.invento.dto.PackingScheduleDetailsDTO;
import com.indona.invento.entities.PackingEntityScheduler;
import com.indona.invento.services.PackingSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packing-scheduler")
public class PackingSchedulerController {

    @Autowired
    private PackingSchedulerService packingSchedulerService;

    @GetMapping("/all")
    public List<PackingEntityScheduler> getAllPackingSchedules() {
        return packingSchedulerService.getAllPackingSchedules();
    }

    /**
     * Get packing schedule details by SO Number and Line Number
     * Returns: customerCode, customerName, orderType, productCategory, brand, grade, temper, dimension
     *
     * Usage: GET /api/packing-scheduler/details?soNumber=SO-001&lineNumber=1
     */
    @GetMapping("/details")
    public ResponseEntity<?> getDetailsBySoNumberAndLineNumber(
            @RequestParam String soNumber,
            @RequestParam String lineNumber) {
        try {
            PackingScheduleDetailsDTO details = packingSchedulerService.getDetailsBySoNumberAndLineNumber(soNumber, lineNumber);

            if (details == null) {
                return ResponseEntity.status(404).body(Map.of(
                        "success", false,
                        "message", "No packing schedule found for SO Number: " + soNumber + " and Line Number: " + lineNumber
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", details
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to fetch packing schedule details: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{soNumber}")
    public PackingInstructionDTO getPackingInstruction(@PathVariable String soNumber) {
        return packingSchedulerService.getPackingInstructionBySoNumber(soNumber);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllPackingSchedules() {
        try {
            packingSchedulerService.deleteAllPackingSchedules();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All packing schedules deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all packing schedules",
                    "details", e.getMessage()
            ));
        }
    }

}

package com.indona.invento.controllers;

import com.indona.invento.services.CleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/cleanup")
public class CleanupController {

    @Autowired
    private CleanupService cleanupService;

    /**
     * DELETE ALL DATA FROM SYSTEM
     * WARNING: This is a DESTRUCTIVE operation - clears all records from all modules
     * Usage: DELETE /cleanup/clear-all
     */
    @DeleteMapping("/clear-all")
    public ResponseEntity<?> clearAllData() {
        try {
            log.warn("DANGEROUS OPERATION: System-wide data cleanup initiated!");

            Map<String, Object> result = cleanupService.clearAllData();

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "System-wide cleanup completed successfully",
                    "details", result
            ));

        } catch (Exception e) {
            log.error("Cleanup operation failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Cleanup operation failed",
                    "message", e.getMessage()
            ));
        }
    }
}


package com.indona.invento.controllers;

import com.indona.invento.dto.PackingSubmissionDTO;
import com.indona.invento.dto.PackingSubmissionResponseDTO;
import com.indona.invento.entities.PackingSubmission;
import com.indona.invento.services.PackingSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/packing-submission")
public class PackingSubmissionController {

    @Autowired
    private PackingSubmissionService packingSubmissionService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitPackingForms(@RequestBody List<PackingSubmissionDTO> dtos) {
        PackingSubmissionResponseDTO response = packingSubmissionService.submitPackingForms(dtos);
        return ResponseEntity.ok(Map.of(
                "data", response,
                "status", "success"));
    }

    @GetMapping("/all")
    public ResponseEntity<List<PackingSubmission>> getAllPackingSubmissions() {
        return ResponseEntity.ok(packingSubmissionService.getAllPackingSubmissions());
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllPackingSubmissions() {
        try {
            packingSubmissionService.deleteAllPackingSubmissions();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All packing submissions deleted successfully",
                    "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all packing submissions",
                    "details", e.getMessage()));
        }
    }

    @PutMapping("/update-pdf")
    public ResponseEntity<?> updatePackingPdf(
            @RequestParam String packingId,
            @RequestParam String pdf) {

        try {
            packingSubmissionService.updatePackingPdf(packingId, pdf);

            return ResponseEntity.ok(Map.of(
                    "message", "PDF updated successfully",
                    "status", "success"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Failed to update PDF.",
                    "details", e.getMessage()));
        }
    }

}

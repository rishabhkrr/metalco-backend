package com.indona.invento.controllers;

import com.indona.invento.dto.JobworkMergedDTO;
import com.indona.invento.dto.SoSummaryDTO;
import com.indona.invento.entities.SoSummaryEntity;
import com.indona.invento.entities.SoSummaryItemEntity;
import com.indona.invento.services.SoSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/so-summary")
public class SoSummaryController {

    @Autowired
    private SoSummaryService service;

    @PostMapping
    public ResponseEntity<SoSummaryEntity> saveSummary(@RequestBody SoSummaryDTO dto) {
        return ResponseEntity.ok(service.saveSummary(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<SoSummaryEntity>> getAllSummaries() {
        return ResponseEntity.ok(service.getAllSummaries());
    }

    @PutMapping("/update-lr")
    public ResponseEntity<String> updateLrNumber(
            @RequestParam String soNumber,
            @RequestParam String lineNumber,
            @RequestParam String lrNumberUpdation) {

        int updated = service.updateLrNumber(soNumber, lineNumber, lrNumberUpdation);
        if (updated > 0) {
            return ResponseEntity.ok("✅ LR Number updated successfully");
        } else {
            return ResponseEntity.status(404).body("⚠️ No matching SO Summary item found");
        }
    }
    
    @GetMapping("/jobwork-merged")
    public ResponseEntity<List<JobworkMergedDTO>> getJobworkMergedData() {
        return ResponseEntity.ok(service.getJobworkMergedData());
    }



    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllSummaries() {
        service.deleteAllSummaries();
        return ResponseEntity.ok("All SO summaries deleted successfully.");
    }
}

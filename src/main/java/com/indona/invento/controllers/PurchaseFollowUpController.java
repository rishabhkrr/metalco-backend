package com.indona.invento.controllers;



import com.indona.invento.dao.PurchaseFollowUpV2Repository;
import com.indona.invento.dto.PurchaseFollowUpUpdateDTO;
import com.indona.invento.entities.PurchaseFollowUpEntityV2;

import com.indona.invento.services.PurchaseFollowUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/purchasefollowup")
@RequiredArgsConstructor
public class PurchaseFollowUpController {

    private final PurchaseFollowUpV2Repository purchaseFollowUpV2Repository;

    @Autowired
    private PurchaseFollowUpService service;

    @GetMapping("/all")
    public ResponseEntity<List<PurchaseFollowUpEntityV2>> getAllFollowUps() {
        List<PurchaseFollowUpEntityV2> allFollowUps = purchaseFollowUpV2Repository.findAll();
        return ResponseEntity.ok(allFollowUps);
    }

    @PutMapping("/update-status")
    public ResponseEntity<PurchaseFollowUpUpdateDTO> markFollowUpCompleted(@RequestBody PurchaseFollowUpUpdateDTO dto) {
        boolean updated = service.markFollowUpCompleted(dto);
        if (updated) {
            return ResponseEntity.ok(dto); // ✅ return DTO as response
        } else {
            return ResponseEntity.status(404).build(); // ❌ no body if not found
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllFollowUps() {
        try {
            service.deleteAllFollowUps();
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ All purchase follow-ups deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "❌ Failed to delete all follow-ups",
                    "details", e.getMessage()
            ));
        }
    }

}

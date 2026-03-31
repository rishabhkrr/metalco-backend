package com.indona.invento.controllers;

import com.indona.invento.dto.POManagementApprovalDto;
import com.indona.invento.entities.POGenerationEntity;
import com.indona.invento.entities.POManagementApprovalEntity;
import com.indona.invento.services.POManagementApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/po-approval")
@RequiredArgsConstructor
public class POManagementApprovalController {

    private final POManagementApprovalService service;

    // ✅ GET ALL API
    @GetMapping("/all")
    public ResponseEntity<List<POManagementApprovalEntity>> getAllApprovals() {
        List<POManagementApprovalEntity> approvals = service.getAllApprovals();
        return ResponseEntity.ok(approvals);
    }

    @PutMapping("/approve/{poNumber}")
    public ResponseEntity<POGenerationEntity> approvePO(@PathVariable String poNumber) {
        POGenerationEntity approvedPO = service.approvePOByNumber(poNumber);
        return ResponseEntity.ok(approvedPO);
    }

    @PutMapping("/reject/{poNumber}")
    public ResponseEntity<POGenerationEntity> rejectPO(@PathVariable String poNumber) {
        POGenerationEntity rejectedPO = service.rejectPOByNumber(poNumber);
        return ResponseEntity.ok(rejectedPO);
    }

    @PutMapping("/remarks/by-po")
    public ResponseEntity<POManagementApprovalEntity> updateRemarksByPoNumber(
            @RequestParam String poNumber,
            @RequestParam String remarks) {
        POManagementApprovalEntity updated = service.updateRemarksByPoNumber(poNumber, remarks);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/cancel/{poNumber}")
    public ResponseEntity<POGenerationEntity> cancelPO(@PathVariable String poNumber) {
        POGenerationEntity cancelledPO = service.cancelPOByNumber(poNumber);
        return ResponseEntity.ok(cancelledPO);
    }


}

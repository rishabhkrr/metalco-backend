package com.indona.invento.controllers;

import com.indona.invento.entities.AuditLogEntity;
import com.indona.invento.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-log")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<List<AuditLogEntity>> getAuditTrail(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.getAuditTrail(entityType, entityId));
    }

    @GetMapping("/module/{moduleName}")
    public ResponseEntity<Page<AuditLogEntity>> getModuleAudit(
            @PathVariable String moduleName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(auditLogService.getAuditByModule(moduleName, pageable));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<AuditLogEntity>> getRecentActivity(
            @RequestParam(required = false) String unitCode) {
        if (unitCode != null && !unitCode.isEmpty()) {
            return ResponseEntity.ok(auditLogService.getRecentActivity(unitCode));
        }
        return ResponseEntity.ok(auditLogService.getRecentActivityAll());
    }

    @GetMapping("/user/{performedBy}")
    public ResponseEntity<List<AuditLogEntity>> getUserActivity(@PathVariable String performedBy) {
        return ResponseEntity.ok(auditLogService.getUserActivity(performedBy));
    }
}

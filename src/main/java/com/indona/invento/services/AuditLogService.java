package com.indona.invento.services;

import com.indona.invento.entities.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditLogService {

    AuditLogEntity logAction(String action, String moduleName, String entityType,
                             Long entityId, String entityRef, String previousValue,
                             String newValue, String description, String performedBy,
                             String unitCode);

    List<AuditLogEntity> getAuditTrail(String entityType, Long entityId);

    Page<AuditLogEntity> getAuditByModule(String moduleName, Pageable pageable);

    List<AuditLogEntity> getRecentActivity(String unitCode);

    List<AuditLogEntity> getRecentActivityAll();

    List<AuditLogEntity> getUserActivity(String performedBy);
}

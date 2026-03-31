package com.indona.invento.services.impl;

import com.indona.invento.dao.AuditLogRepository;
import com.indona.invento.entities.AuditLogEntity;
import com.indona.invento.services.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public AuditLogEntity logAction(String action, String moduleName, String entityType,
                                     Long entityId, String entityRef, String previousValue,
                                     String newValue, String description, String performedBy,
                                     String unitCode) {
        AuditLogEntity log = AuditLogEntity.builder()
                .action(action)
                .moduleName(moduleName)
                .entityType(entityType)
                .entityId(entityId)
                .entityRef(entityRef)
                .previousValue(previousValue)
                .newValue(newValue)
                .description(description)
                .performedBy(performedBy)
                .performedAt(LocalDateTime.now())
                .unitCode(unitCode)
                .build();

        return auditLogRepository.save(log);
    }

    @Override
    public List<AuditLogEntity> getAuditTrail(String entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityIdOrderByPerformedAtDesc(entityType, entityId);
    }

    @Override
    public Page<AuditLogEntity> getAuditByModule(String moduleName, Pageable pageable) {
        return auditLogRepository.findByModuleNameOrderByPerformedAtDesc(moduleName, pageable);
    }

    @Override
    public List<AuditLogEntity> getRecentActivity(String unitCode) {
        return auditLogRepository.findTop50ByUnitCodeOrderByPerformedAtDesc(unitCode);
    }

    @Override
    public List<AuditLogEntity> getRecentActivityAll() {
        return auditLogRepository.findTop50ByOrderByPerformedAtDesc();
    }

    @Override
    public List<AuditLogEntity> getUserActivity(String performedBy) {
        return auditLogRepository.findByPerformedByOrderByPerformedAtDesc(performedBy);
    }
}

package com.indona.invento.dao;

import com.indona.invento.entities.AuditLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByEntityTypeAndEntityIdOrderByPerformedAtDesc(String entityType, Long entityId);

    Page<AuditLogEntity> findByModuleNameOrderByPerformedAtDesc(String moduleName, Pageable pageable);

    List<AuditLogEntity> findByPerformedByOrderByPerformedAtDesc(String performedBy);

    List<AuditLogEntity> findByUnitCodeOrderByPerformedAtDesc(String unitCode);

    List<AuditLogEntity> findTop50ByUnitCodeOrderByPerformedAtDesc(String unitCode);

    List<AuditLogEntity> findTop50ByOrderByPerformedAtDesc();
}

package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_audit_module", columnList = "moduleName"),
    @Index(name = "idx_audit_performed", columnList = "performedAt"),
    @Index(name = "idx_audit_user", columnList = "performedBy")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String action; // CREATE, UPDATE, DELETE, APPROVE, REJECT, STATUS_CHANGE, LOGIN

    @Column(nullable = false, length = 50)
    private String moduleName; // SUPPLIER_MASTER, SALES_ORDER, GRN, GATE_ENTRY, etc.

    @Column(nullable = false, length = 100)
    private String entityType; // SupplierMaster, SalesOrder, GRN, etc.

    private Long entityId;

    @Column(length = 100)
    private String entityRef; // Human-readable ref: SO-001, GRN-001, etc.

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String previousValue; // JSON snapshot of previous state (for UPDATE)

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String newValue; // JSON snapshot of new state

    @Column(length = 200)
    private String description; // Human-readable description: "Supplier XYZ approved by Admin"

    @Column(nullable = false, length = 100)
    private String performedBy; // Username or userId

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime performedAt = LocalDateTime.now();

    @Column(length = 50)
    private String unitCode;

    @PrePersist
    public void prePersist() {
        if (performedAt == null) performedAt = LocalDateTime.now();
    }
}

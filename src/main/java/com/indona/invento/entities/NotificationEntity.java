package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notif_recipient", columnList = "recipientUserId"),
    @Index(name = "idx_notif_read", columnList = "recipientUserId, isRead"),
    @Index(name = "idx_notif_created", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String type; // APPROVAL_REQUIRED, STATUS_CHANGE, LOW_STOCK, PRICE_UPDATE, ALERT, INFO

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(nullable = false)
    private Long recipientUserId;

    @Column(length = 50)
    private String recipientRole; // Optional: role-based notification

    @Column(length = 50)
    private String moduleName; // e.g., SUPPLIER_MASTER, SALES_ORDER, GRN, etc.

    @Column(length = 100)
    private String entityType; // e.g., SupplierMaster, SalesOrder, GRN

    private Long entityId; // ID of the related entity

    @Column(length = 50)
    private String unitCode;

    @Builder.Default
    @Column(nullable = false)
    private Boolean isRead = false;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime readAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (isRead == null) isRead = false;
    }
}

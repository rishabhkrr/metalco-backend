package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FRD v3.0 Module 2: Certificate of Conformance (CoC) Entity
 * 
 * Key changes from original:
 * - COC Number format: MECOC + YYMM + Sequential ID (e.g., MECOC26020001)
 * - SO Number REMOVED from header — MOVED to CocLineItemEntity
 * - Added PO Number/Date (aggregated, comma-separated if multiple)
 * - Added poQuantityKg, dispatchQuantityKg, dispatchQuantityNo
 * - Added billing_summary_id FK
 * - Added representative fields
 * - Added remarks
 * - Added cocPdfUrl
 */
@Entity
@Table(name = "certificate_of_confidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateOfConfidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CoC Header Information
    // Format: MECOC + YYMM + Sequential ID (zero-padded 4 digits)
    // Example: MECOC26020001
    private String cocNumber;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;

    // FK to Billing Summary
    private Long billingSummaryId;

    // Unit Information
    private String unit;

    // PO Information (aggregated, comma-separated if multiple) — COC-BR-004
    @Column(length = 2000)
    private String poNumberDate;

    // Invoice Information
    private String invoiceNumber;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDateTime invoiceDate;

    // Quantities — COC-BR-005, COC-BR-006
    @Column(precision = 18, scale = 3)
    private BigDecimal poQuantityKg;          // SUM of Order Quantity from SO Summary

    @Column(precision = 18, scale = 3)
    private BigDecimal dispatchQuantityKg;    // SUM of Quantity Kg from item table

    private Integer dispatchQuantityNo;       // SUM of Quantity No

    // Dispatched Quantity (backward compat)
    private String dispatchedQuantity;

    // Customer Information
    private String customerCode;
    private String customerName;
    @Column(length = 2000)
    private String customerBillingAddress;
    @Column(length = 2000)
    private String customerShippingAddress;
    private String customerEmail;
    private String customerPhone;

    // Declaration text (fixed text)
    @Column(columnDefinition = "TEXT")
    private String declaration;

    // Representative Information
    private String representativeName;
    private String representativeTitle;

    // Remarks
    @Column(length = 2000)
    private String remarks;

    // PDF URL
    @Column(length = 1000)
    private String cocPdfUrl;

    // CoC Line Items
    @OneToMany(mappedBy = "coc", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("coc-items")
    private List<CocLineItemEntity> lineItems;

    // Status: DRAFT, GENERATED, SENT
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // ==================== Backward Compatibility Fields ====================
    // These were in the original entity, kept for existing data
    private String soNumber;                     // DEPRECATED: Moved to line items
    private String customerPONumber;
    private LocalDateTime customerPODate;
    private String customerPOQuantity;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

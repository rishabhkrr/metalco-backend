package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FRD v3.0 Module 1: Billing Summary Header Entity
 * Restructured from flat entity to header + items + batch details.
 * 
 * Key changes from original:
 * - Added rfdListNumber (FK to RFD List Transfer)
 * - Added eWaybillNumber, lrNumber (moved from SO Summary)
 * - Added invoicePdfUrl, cocId
 * - Added totalInvoiceAmount (calculated from items)
 * - Added hamaliCharges (new charge type)
 * - Items are now stored in BillingSummaryItemEntity (child relationship)
 * 
 * Backward Compatibility: Retains original flat fields (soNumber, lineNumber, etc.)
 * so existing data and APIs continue to work. New items-based flow uses the child entities. 
 */
@Entity
@Table(name = "billing_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // ==================== NEW FRD v3.0 Fields ====================

    // RFD List Number — FK to RFD List Transfer (filtered to exclude used numbers, BS-BR-001)
    private String rfdListNumber;

    // E-Waybill Number (manual input — Tally integration green field)
    private String eWaybillNumber;

    // LR Number — moved FROM SO Summary TO here (BS-BR-008)
    private String lrNumber;

    // Invoice PDF URL (uploaded PDF)
    @Column(length = 1000)
    private String invoicePdfUrl;

    // COC reference
    private Long cocId;

    // Total Invoice Amount = SUM(totalAmount) across all items (BS-BR-007)
    @Column(precision = 18, scale = 2)
    private BigDecimal totalInvoiceAmount;

    // Hamali charges (new charge type per FRD v3.0)
    @Column(precision = 18, scale = 2)
    private BigDecimal hamaliCharges;

    // ==================== Original Fields (Backward Compatibility) ====================

    private String invoiceNumber;
    private String soNumber;
    private String lineNumber;
    private String unit;
    private String customerCode;
    private String customerName;

    @Column(length = 2000)
    private String customerBillingAddress;
    @Column(length = 2000)
    private String customerShippingAddress;

    private String packingStatus;
    private String orderType;
    private String productCategory;

    @Column(length = 500)
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantityKg;
    private String uomKg;

    private Integer quantityNo;
    private String uomNo;

    @Column(precision = 18, scale = 2)
    private BigDecimal itemPrice;
    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(precision = 18, scale = 2)
    private BigDecimal cgst;
    @Column(precision = 18, scale = 2)
    private BigDecimal sgst;
    @Column(precision = 18, scale = 2)
    private BigDecimal igst;

    @Column(precision = 18, scale = 2)
    private BigDecimal transportationCharges;
    @Column(precision = 18, scale = 2)
    private BigDecimal packingCharges;
    @Column(precision = 18, scale = 2)
    private BigDecimal cuttingCharges;
    @Column(precision = 18, scale = 2)
    private BigDecimal laminationCharges;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    private String billingStatus;
    @Column(precision = 18, scale = 2)
    private BigDecimal price;
    private Integer creditPeriodDays;
    private String coc;

    // ==================== Child Items (FRD v3.0) ====================

    @OneToMany(mappedBy = "billingSummary", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BillingSummaryItemEntity> items;

    @PrePersist
    public void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }

    /**
     * BS-BR-007: Recalculate totalInvoiceAmount = SUM(totalAmount) across all items
     */
    public void recalculateTotalInvoiceAmount() {
        if (items != null && !items.isEmpty()) {
            this.totalInvoiceAmount = items.stream()
                    .map(item -> item.getTotalAmount() != null ? item.getTotalAmount() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}

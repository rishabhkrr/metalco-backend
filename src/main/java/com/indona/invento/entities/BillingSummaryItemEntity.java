package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * FRD v3.0 Module 1: Billing Summary Item Entity
 * Represents individual line items within a billing summary record.
 * Each item corresponds to a SO Number + Line Number combination.
 */
@Entity
@Table(name = "billing_summary_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingSummaryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_summary_id", nullable = false)
    @JsonBackReference
    private BillingSummaryEntity billingSummary;

    // SO & Line reference
    private String soNumber;
    private String lineNumber;

    // Product details (auto-populated from RFD List Transfer)
    private String orderType;
    private String productCategory;
    @Column(length = 500)
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    // Quantities
    @Column(precision = 18, scale = 3)
    private BigDecimal totalQuantityKg;
    private String uomKg;
    private Integer totalQuantityNo;
    private String uomNo;

    // Pricing — FRD: Item Price = Amount / Total Quantity (Kg) [auto-calculated]
    @Column(precision = 18, scale = 2)
    private BigDecimal itemPrice;

    // FRD: Amount is MANDATORY manual input (GREEN field)
    @Column(precision = 18, scale = 2)
    private BigDecimal amount;

    // Tax fields — at least one set (CGST+SGST or IGST) required
    @Column(precision = 18, scale = 2)
    private BigDecimal cgst;
    @Column(precision = 18, scale = 2)
    private BigDecimal sgst;
    @Column(precision = 18, scale = 2)
    private BigDecimal igst;

    // Additional charges — optional, default ₹0.00
    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal transportationCharges = BigDecimal.ZERO;
    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal packingCharges = BigDecimal.ZERO;
    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal cuttingCharges = BigDecimal.ZERO;
    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal hamaliCharges = BigDecimal.ZERO;
    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal laminationCharges = BigDecimal.ZERO;

    // Total Amount = amount + cgst + sgst + igst + all charges
    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    // Credit Period
    private Integer creditPeriodDays;

    // Batch Details (child relationship)
    @OneToMany(mappedBy = "billingSummaryItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("item-batch")
    private List<BillingBatchDetailEntity> batchDetails;

    /**
     * BS-BR-003: Auto-calculate item_price = amount / total_quantity_kg
     * BS-BR-006: total_amount = amount + taxes + charges
     */
    @PrePersist
    @PreUpdate
    public void calculateDerivedFields() {
        // BS-BR-003: Item Price = Amount / Total Quantity Kg
        if (amount != null && totalQuantityKg != null && totalQuantityKg.compareTo(BigDecimal.ZERO) > 0) {
            this.itemPrice = amount.divide(totalQuantityKg, 2, java.math.RoundingMode.HALF_UP);
        }

        // BS-BR-006: Total Amount = amount + all taxes + all charges
        BigDecimal total = amount != null ? amount : BigDecimal.ZERO;
        total = total.add(cgst != null ? cgst : BigDecimal.ZERO);
        total = total.add(sgst != null ? sgst : BigDecimal.ZERO);
        total = total.add(igst != null ? igst : BigDecimal.ZERO);
        total = total.add(transportationCharges != null ? transportationCharges : BigDecimal.ZERO);
        total = total.add(packingCharges != null ? packingCharges : BigDecimal.ZERO);
        total = total.add(cuttingCharges != null ? cuttingCharges : BigDecimal.ZERO);
        total = total.add(hamaliCharges != null ? hamaliCharges : BigDecimal.ZERO);
        total = total.add(laminationCharges != null ? laminationCharges : BigDecimal.ZERO);
        this.totalAmount = total;
    }
}

package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * FRD v3.0 Module 1: Billing Batch Detail Entity
 * Stores batch-level details for each billing summary item.
 * Shows: Item Description, Item Dimension, Batch Number, Date of Inward,
 * Quantity (Kg/No), QR Code (FG), Test Certificate
 */
@Entity
@Table(name = "billing_batch_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingBatchDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_item_id", nullable = false)
    @JsonBackReference("item-batch")
    private BillingSummaryItemEntity billingSummaryItem;

    @Column(length = 500)
    private String itemDescription;

    private String itemDimension;

    private String batchNumber;

    private LocalDate dateOfInward;

    @Column(precision = 18, scale = 3)
    private BigDecimal quantityKg;

    private Integer quantityNo;

    // QR Code (FG) image URL
    @Column(length = 1000)
    private String qrCodeFg;

    // Test Certificate URL (per GRN Purchase)
    @Column(length = 1000)
    private String testCertificateUrl;
}

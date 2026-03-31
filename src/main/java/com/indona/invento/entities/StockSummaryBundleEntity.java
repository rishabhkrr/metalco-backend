package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity to store individual bundle details for StockSummary
 * This replaces the JSON grnNumbers field with proper relational mapping
 */
@Entity
@Table(name = "stock_summary_bundles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryBundleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foreign Key to StockSummaryEntity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_summary_id", nullable = false)
    private StockSummaryEntity stockSummary;

    // GRN Details
    private String grnNumber;
    private Long grnId;
    private Long stockTransferId;
    private String transferNumber;
    private String transferType;

    // Bundle Details
    private String slNo;
    private String itemDescription;
    private String productCategory;
    private String sectionNumber;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;  // Dimension field

    // Weighment Details
    private String weighment;  // AUTO or MANUAL
    private BigDecimal weightmentQuantityKg;
    private String uomNetWeight;  // UOM for Net Weight (e.g., KGS)
    private Integer weightmentQuantityNo;
    private String uomNo;  // UOM for Quantity Number (e.g., PCS, NOS)

    private BigDecimal itemPrice;
    // Warehouse Storage Fields
    private String materialAcceptance;
    private String currentStore;
    private String recipientStore;
    private String storageArea;
    private String rackColumnBinNumber;
    private String rackStatus;

    // QR Code
    private String qrCodeUrl;

    // PO Number
    private String poNumber;

    // GRN Additional Details (fetched from GRN Summary)
    private String heatNo;
    private String lotNo;
    private String testCertificate;

    // User Info
    private String userId;
    private String unitId;

    // Status
    private String status;

    // Audit Fields
    private String createdBy;
    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}


package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "grn_line_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrnLineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // GRN Reference
    private String grnNumber;
    private Long grnId;

    // Stock Transfer Reference
    private Long stockTransferId;
    private String transferNumber;

    private  String inputType;

    // Item Details
    private String slNo;
    private String itemDescription;
    private String productCategory;
    private String sectionNumber;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;  // Dimension field for bundle

    // Quantity Details
    private String weighment; // AUTO or MANUAL
    private BigDecimal weightmentQuantityKg;
    private String uomNetWeight; // UOM for Net Weight (e.g., KGS)
    private Integer weightmentQuantityNo;
    private String uomNo; // UOM for Quantity Number (e.g., PCS, NOS)

    // Warehouse Storage Fields (per-bundle)
    private String materialAcceptance;
    private String currentStore;
    private String recipientStore;
    private String storageArea;
    private String rackColumnBinNumber;
    private String rackStatus;

    // FRD: ABP-003 Stock Type
    @Builder.Default
    private String stockType = "Goods";

    // FRD: ABP-004 Batch tracking
    private String batchNumber;
    private String heatNumber;
    private String lotNumber;
    private BigDecimal itemPrice;

    // FRD: SUB-007 Allocation Status
    @Builder.Default
    private String allocationStatus = "Pending"; // Pending / Completed

    // PO Number
    private String poNumber;

    // User and Unit Information
    private String userId;
    private String unitId;

    // QR Details
    @Column(length = 500)
    private String qrCode;

    @Column(length = 10000)
    private String qrCodeImageUrl;

    @Builder.Default
    private Boolean qrGenerated = false;


    // Status
    @Builder.Default
    private String status = "PENDING"; // PENDING, ADDED, QR_GENERATED

    // Audit
    private String createdBy;
    private Date createdDate;
    private String updatedBy;
    private Date updatedDate;

    // After Stock Transfer - Remaining quantity after returnable deduction
    private BigDecimal afterStockTransferQtyKg;
    private Integer afterStockTransferQtyNo;

    @PrePersist
    public void prePersist() {
        this.createdDate = new Date();
        this.status = "PENDING";
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = new Date();
    }
}

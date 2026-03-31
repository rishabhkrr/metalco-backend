package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrnLineItemDto {

    private Long id;

    // GRN Reference
    private String grnNumber;
    private Long grnId;

    // Stock Transfer Reference
    private Long stockTransferId;
    private String transferNumber;
    private String transferType; // For response display only

    // Item Details
    private String slNo;
    private String itemDescription;
    private String productCategory;
    private String sectionNumber;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;  // Dimension field

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
    private String rackStatus;  // ✅ Added

    // PO Number
    private String poNumber;

    // User and Unit Information
    private String userId;
    private String unitId;

    private String inputType;
    // QR Details
    private String qrCode;
    private String qrCodeImageUrl;
    private Boolean qrGenerated;

    // Status
    private String status;

    // Audit
    private String createdBy;
    private Date createdDate;
    private String updatedBy;
    private Date updatedDate;
}

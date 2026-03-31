package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for returning Stock Summary with complete bundle/GRN details
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockSummaryWithBundlesDTO {

    // Stock Summary fields
    private Long id;
    private String unit;
    private String store;
    private String storageArea;
    private String rackColumnShelfNumber;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private BigDecimal itemPrice;
    private String materialType;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal thickness;
    private String batchNumber;
    private String itemGroup;
    private Boolean reprintQr;
    private String sectionNo;
    private String qrCode;
    private Boolean pickListLocked;
    private String grnNumbers;  // Legacy JSON array field

    // Complete bundle/GRN data
    private List<BundleDTO> bundles;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class BundleDTO {
        private Long id;
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

        // Weighment Details
        private String weighment;
        private BigDecimal weightmentQuantityKg;
        private String uomNetWeight;
        private Integer weightmentQuantityNo;
        private String uomNo;

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

        // GRN Additional Details
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
        private String createdDate;
    }
}


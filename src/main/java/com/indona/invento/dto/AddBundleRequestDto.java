package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddBundleRequestDto {

    private String grnNumber;
    private Long grnId;
    private Long stockTransferId;
    private String transferNumber;
    private String transferType;
    private String createdBy;
    private String inputType;
    private String userId;      // ✨ नई field - array के बाहर
    private String unitId;        // ✨ नई field - array के बाहर

    // Default warehouse storage fields
    private String store;           // Default: "Warehouse"
    private String storageArea;     // Default: "Common"

    // List of items to add as bundles
    private List<BundleItemDto> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BundleItemDto {
        private String slNo;
        private String itemDescription;
        private String productCategory;
        private String sectionNumber;
        private String brand;
        private String grade;
        private String temper;
        private String dimension;  // Dimension field for bundle
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

        private BigDecimal itemPrice;

        // QR Code URL
        private String qrCodeUrl;

        // PO Number
        private String poNumber;
    }
}

package com.indona.invento.dto;


import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PickListDTOs {
    private String unit;
    private String soNumber;
    private String lineNumber;
    private String nextProcess;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
    private String storageArea;
    private String rackColumnShelfNumber;
    private String store;

    // New fields for bundle-based picklist
    private String storageType;
    private String selectionReason;
    private BigDecimal totalSelectedQuantityKg;
    private Integer totalBundlesSelected;
    private List<SelectedBundleDTO> selectedBundles;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SelectedBundleDTO {
        private Long bundleId;
        private Integer slNo;
        private String grnRefNo;
        private Long grnTimestamp;
        private String store;
        private String storageArea;
        private String rack;
        private String itemDescription;
        private String productCategory;
        private String brand;
        private String grade;
        private String temper;
        private BigDecimal quantityKg;
        private Integer quantityNo;
        private String qrCode;
        private String qrCodeImageUrl;
    }
}

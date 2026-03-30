package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockRetrievalEntrySchedulerDTO {
    // Bundle and Stock Summary IDs for exact identification
    private Long bundleId;
    private Long stockSummaryId;

    // Location
    private String store;
    private String storageArea;
    private String rackColumnBin;

    // Retrieval quantities
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;

    // Batch info
    private String batchNumber;
    private String dateOfInward;

    // Dimension
    private String dimension;

    // QR codes
    private String qrCodeRM;
    private String scanQrCode;

    // Retrieved quantities
    private BigDecimal retrievedQuantityKg;
    private Integer retrievedQuantityNo;

    // Weighed quantities
    private BigDecimal weighedQuantityKg;
    private Integer weighedQuantityNo;

    // Generate QR (FG)
    private String generatePrintQrFG;

    // Returnable quantities
    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;
}

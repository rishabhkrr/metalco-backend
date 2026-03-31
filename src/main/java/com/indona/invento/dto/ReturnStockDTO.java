package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnStockDTO {
    private String unit;
    private String itemDescription;

    // Multiple return entries
    private List<ReturnEntryDTO> returnEntries;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ReturnEntryDTO {
        private String batchNumber;
        private String dateOfInward;

        private BigDecimal returnQuantityKg;
        private Integer returnQuantityNo;

        private String generatePrintQrFG;

        private String returnStore;
        private String storageArea;
        private String rackColumnBin;

        private String dimension;  // Dimension field

        private String scanLocation;
        private String allocationStatus;
        private String qrCode;  // QR Code field
    }
}


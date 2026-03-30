package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockTransferSchedulerDTO {
    private String soNumber;
    private String lineNumber;
    private String nextProcess;

    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;

    private BigDecimal weighmentQuantityKg;
    private Integer weighmentQuantityNo;

    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;

    private BigDecimal scrapQuantityKg;
    private Integer scrapQuantityNo;

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String unit;

    private Boolean generateQr;
    private String qrCode;


    private List<WarehouseStockRetrievalEntrySchedulerDTO> retrievalEntries;
}

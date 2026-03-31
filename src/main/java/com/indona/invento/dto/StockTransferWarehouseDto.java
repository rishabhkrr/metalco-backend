package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockTransferWarehouseDto {
    private Long id; // optional - for updates; ignore/null for create
    private String mrNumber;
    private String lineNumber;

    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
    private BigDecimal requiredQuantity;

    private BigDecimal weighmentQuantityKg;
    private Integer weighmentQuantityNo;

    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String unit;
    private String nextProcess;

    private Boolean generateQr;
    private String qrCode;
    private String generatedQrImage;

    private List<StockTransferWHRetrievalQtyDto> retrievalQtyDto;
}

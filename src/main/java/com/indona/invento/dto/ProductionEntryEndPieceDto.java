package com.indona.invento.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionEntryEndPieceDto {

    private Long id;
    private String endPieceDimension;

    private BigDecimal calculatedWeightKg;
    private BigDecimal endPieceQuantityKg;
    private BigDecimal endPieceQuantityNo;

    private String endPieceType;
    private Boolean qrGenerate;
    private String qrCode;

    // New fields
    private String parentDimension;
    private String batchNumber;
    private BigDecimal width;
    private BigDecimal thickness;
    private BigDecimal length;
}
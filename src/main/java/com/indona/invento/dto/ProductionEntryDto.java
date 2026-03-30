package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionEntryDto {

    private Long id;
    private String soNumber;
    private String lineNumber;
    private String machineName;
    private String unit;
    private String customerCode;
    private String customerName;
    private Boolean packing;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;
    private BigDecimal calculatedWeight;

    private BigDecimal producedQtyKg;
    private Integer producedQtyNo;

    private String generatedQr;

    private Boolean machineBreakDown;
    private Double targetBladeSpeed;
    private Double targetFeed;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private Instant timestamp;

    private String startTime;
    private String endTime;
    private Double totalMetresCut;
    private String nextProductionProcess;

    private List<ProductionEntryEndPieceDto> endPieces;
    private List<ProductionIdleTimeEntryDto> idleEntries;

    private BigDecimal endPiecesQtyNo;
}

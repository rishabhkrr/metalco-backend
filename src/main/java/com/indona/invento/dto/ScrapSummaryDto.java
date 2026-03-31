package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScrapSummaryDto {
    private String unit;
    private String dimension;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String machineName;
    private String soNumber;
    private String lineNumber;
    private BigDecimal producedQtyKg;
    private BigDecimal producedQtyNo;

    // 🆕 End Piece dimensions
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal thickness;
    private String batchNumber;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private Instant timestamp;
}
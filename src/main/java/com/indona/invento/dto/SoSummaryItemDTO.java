package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoSummaryItemDTO {
    private String lineNumber;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String productionStrategy;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal orderQuantityKg;
    private String uomKg;
    private Integer orderQuantityNo;
    private String uomNo;
    private Integer creditDays;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDate targetDispatchDate;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDate dispatchDate;
    private BigDecimal dispatchQuantityKg;
    private Integer dispatchQuantityNo;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private String invoiceNumber;
    private String lrNumberUpdation;
    private String soStatus;
}

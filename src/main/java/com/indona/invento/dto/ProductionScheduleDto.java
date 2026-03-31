package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductionScheduleDto {

    private String nextProcess;
    private String machineName;
    private String soNumber;
    private String lineNumber;
    private String unit;
    private String customerCode;
    private String customerName;
    private Boolean packing;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private BigDecimal rmQuantityKg;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;
    private Double targetBladeSpeed;
    private Double targetFeed;
    private String startTime;
    private String endTime;
    private String nextProductionProcess;
    private LocalDate targetDispatchDate;
    private String uomKg;
    private String uomNo;
}
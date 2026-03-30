package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLineItemDTO {
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private Double quantityKg;
    private String uomKg;
    private Double quantityNos;
    private String uomNos;
    private String orderMode;
    private String productionStrategy;
    private Double currentPrice;
    private String status;
    private Integer creditPeriod;
    private String lineNumber;
    private BigDecimal totalPrice;

    private Object priceSnapshot;
    private Object stockSummary;

    private LocalDate targetDispatchDate;
}

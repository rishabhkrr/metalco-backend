package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderItemDTO {
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private Integer quantityKg;
    private String uomKg;
    private Integer quantityNos;
    private String uomNos;
    private String orderMode;
    private String productionStrategy;
    private Double currentPrice;
    private Integer creditPeriodDays;
    private LocalDate targetDispatchDate;
}

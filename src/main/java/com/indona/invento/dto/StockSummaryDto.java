package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockSummaryDto {

    private String unit;
    private String store;
    private String storageArea;
    private String rackColumnShelfNumber;

    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal quantityKg;
    private Integer quantityNo;
    private BigDecimal itemPrice;
    private String materialType;

    private String dimension1;
    private String dimension2;
    private String dimension3;

    private Boolean reprintQr;
    private String sectionNo;
}


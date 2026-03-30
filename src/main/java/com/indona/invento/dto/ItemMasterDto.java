package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMasterDto {
    private String productCategory;
    private String materialType;
    private String hsnCode;
    private String sectionNumber;
    private String dimension1;
    private String dimension2;
    private String dimension3;
    private String dimension;
    private String skuDescription;
    private String grade;
    private String temper;
    private String brand;
    private String narration;
    private String supplierCode;
    private String supplierName;
    private String primaryUom;
    private String altUomApplicable;
    private String altUom;
    private String reportingUom;
    private Integer leadTimeDays;
    private BigDecimal moq;
    private String gstApplicable;
    private BigDecimal gstRate;
    private BigDecimal openingStockInKgs;
    private BigDecimal openingStockInNos;
    private BigDecimal itemPrice;
    private String unitName;
}

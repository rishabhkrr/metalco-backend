package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEnquiryStockCheckDto {
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal requiredQuantity;
    private String materialType;
    private String uom; // "Kg" or "No"
    private String unit;
    private String orderType;

}

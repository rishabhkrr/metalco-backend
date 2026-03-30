package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for returning line item details to auto-fill the stock return form
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillLineDetailsDTO {

    private String unit;
    private String customerCode;
    private String customerName;
    private String customerBillingAddress;
    private String customerShippingAddress;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String uomKg;
    private String uomNo;
    // itemPrice is NOT auto-filled - it's a user entry field
}


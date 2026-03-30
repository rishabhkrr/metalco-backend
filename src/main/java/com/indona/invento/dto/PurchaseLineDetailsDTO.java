package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseLineDetailsDTO {
    private String unit;
    private String supplierCode;
    private String supplierName;
    private String supplierBillingAddress;
    private String supplierShippingAddress;
    private String itemDescription;
    private String productCategory;
    private String sectionNumber;
    private String brand;
    private String grade;
    private String temper;
}


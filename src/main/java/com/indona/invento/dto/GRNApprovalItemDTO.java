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
public class GRNApprovalItemDTO {

    private String itemDescription;        // Item description
    private BigDecimal netQuantityKg;      // Net quantity in KG
    private Integer netQuantityNo;         // Net quantity in numbers
    private BigDecimal itemPrice;          // Item price

    // Optional fields for each item
    private String productCategory;        // Product category
    private String brand;                  // Brand
    private String grade;                  // Grade
    private String temper;                 // Temper
    private String materialType;           // Material type
    private String sectionNo;              // Section number
    private String dimension;              // Item dimension
    private Double rate;
    private String heatNumber;
    private String lotNumber;
    private String testCertificateNumber;
}


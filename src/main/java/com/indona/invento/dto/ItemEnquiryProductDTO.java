package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEnquiryProductDTO {
    private Integer productSelectedId;
    private String category;
    private String description;
    private double thickness;
    private double width;
    private double length;
    private String brand;
    private String grade;
    private String temper;
    private String materialType;
    private double quantity;
    private String dimension;
    private String uom;
    private double price;
    private Double currentSellingPrice;
    private Integer quantityInNo;

    private String requiredCategory;
    private double requiredThickness;
    private double requiredWidth;
    private double requiredLength;
    private String requiredBrand;
    private String requiredGrade;
    private String requiredTemper;
    private double requiredQuantity;
    private String requiredUom;

    private String orderType;
}

package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MaterialRequestSummaryItemDTO {
    private String itemDescription;
    private String materialType;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private Integer requiredQuantity;
    private String uom;
    private String status;
    private String lineNumber;

}


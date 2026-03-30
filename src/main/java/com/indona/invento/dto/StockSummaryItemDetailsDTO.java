package com.indona.invento.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockSummaryItemDetailsDTO {
    private String itemDescription;
    private String materialType;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private String uom;
    private String sectionNo;
}

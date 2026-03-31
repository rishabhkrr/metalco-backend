package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExcelRow {
	private String categoryId;
    private String skuCode;
    private String skuName;
    private String skuQuantity;
    private String binName;
    private String servicePrice;
    private String retailPrice;
    private String pgmPrice;
    private String dealerPrice;
}

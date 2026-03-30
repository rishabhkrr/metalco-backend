package com.indona.invento.dto;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockInReportDto { 
  
    private Object skuCode; 
    private Object skuName;
    private Object storeName;
    private Object quantity;
}
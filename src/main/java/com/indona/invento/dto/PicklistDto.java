package com.indona.invento.dto;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PicklistDto { 
  
    private String skuCode; 
    private String skuName;
    private Long quantity;
    private Long orderedQuantity;
    private String binName;
    private String location;
    private String picked;
}
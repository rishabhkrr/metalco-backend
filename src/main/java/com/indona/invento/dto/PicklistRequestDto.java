package com.indona.invento.dto;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PicklistRequestDto { 
  
    private String skuId; 
    private String storeId; 
    private String quantity;
    private String refNo;
}
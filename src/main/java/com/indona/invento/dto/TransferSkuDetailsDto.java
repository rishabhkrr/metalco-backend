package com.indona.invento.dto;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferSkuDetailsDto { 
  
    private String transferQuantity;
	
    private String transferSkuName;
    
    private String transferSkuCode;
    
    private String recievedQuantity;
    
    private String dispatchQuantity;
    
    private String returnedQuantity;
    
    private String skuPrice;
    
    private String skuTotal;
    
    private String picked;
}
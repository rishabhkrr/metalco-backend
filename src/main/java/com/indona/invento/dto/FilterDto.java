package com.indona.invento.dto;

import java.util.Date;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterDto { 
  
    private Date fromDate; 
    private Date toDate;
    private Long storeId;
}
package com.indona.invento.dto;

import java.util.Date;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeletedDataDto { 
	
	private String refNo;
	  
	private String type;
    
    private Date date;
    
    private Long store;
    
    private String remark;
    
    
}
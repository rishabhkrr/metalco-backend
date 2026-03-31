package com.indona.invento.dto;

import java.util.Date;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LedgerDto { 
  
	private Long id;
    private String dealerName;
    private String expenseName;
    private String expenseUser;
    private String refNo;
    private String receiptNo;
    private String type;
    private String credit;
    private String debit;
    private String userName;
    private String store;
    private String file;
    private Date dateTime;
    
}

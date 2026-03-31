package com.indona.invento.dto;

import java.util.Date;

import lombok.AllArgsConstructor; 
import lombok.Data; 
import lombok.NoArgsConstructor; 
  
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceSummaryDto { 
  
	private Date date;
    private Long totalInvoice; 
    private Long recievedPayment;
    private Long returnedPayment;
    private Long totalExpenses;
    private Long totalCredit;
    private Long totalDebit;
    private Long cashInHand;
}
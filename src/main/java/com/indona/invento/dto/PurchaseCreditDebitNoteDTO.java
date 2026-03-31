package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseCreditDebitNoteDTO {
    private String transactionType;
    private String receiver;
    private String invoiceNumber;
    private String poNumber;
    private String unit;
    private String supplierCode;
    private String supplierName;
    private String supplierBillingAddress;
    private String supplierShippingAddress;
    private String productCategory;
    private String itemDescription;
    private String sectionNumber;
    private String brand;
    private String grade;
    private BigDecimal itemPrice;
    private BigDecimal transactionAmount;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalAmount;
    private String createdBy;
}


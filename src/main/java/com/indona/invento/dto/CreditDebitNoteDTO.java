package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for creating Credit Note and Debit Note requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDebitNoteDTO {

    // Transaction type: "CREDIT" or "DEBIT"
    private String transactionType;

    private String receiver;

    // Reference fields from billing
    private String invoiceNumber;
    private String soNumber;
    private String lineNumber;

    // Unit and customer details
    private String unit;
    private String customerCode;
    private String customerName;
    private String customerBillingAddress;
    private String customerShippingAddress;

    // Product details
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    // Financial fields
    private BigDecimal itemPrice;
    private BigDecimal transactionAmount;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalAmount;

    // Audit
    private String createdBy;
}


package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillStockReturnDTO {

    private Long id;
    private LocalDateTime timestamp;
    private String salesReturnNumber; // Format: MECRD(YY)(MM)(0001)

    // Reference fields
    private String invoiceNumber;
    private String soNumber;
    private String lineNumber;

    // Auto-filled fields from BillingSummary
    private String unit;
    private String customerCode;
    private String customerName;
    private String customerBillingAddress;
    private String customerShippingAddress;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String uomKg;
    private String uomNo;
    private BigDecimal itemPrice;

    // User-entered fields
    private BigDecimal returnQuantityKg;
    private BigDecimal returnQuantityNo;
    private BigDecimal amount;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal transportationCharges;
    private BigDecimal totalAmount;

    // Stock selection: "REJECTION" or "GENERAL"
    private String stockSelection;

    // Audit fields
    private String createdBy;
    private LocalDateTime createdAt;
}


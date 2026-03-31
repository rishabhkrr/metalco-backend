package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingSummaryDTO {

    private LocalDateTime timestamp;

    private String invoiceNumber;
    private String soNumber;
    private String lineNumber;
    private String unit;
    private String customerCode;
    private String customerName;
    private String customerBillingAddress;
    private String customerShippingAddress;

    private String packingStatus;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal price;

    private BigDecimal quantityKg;
    private String uomKg;

    private Integer quantityNo;
    private String uomNo;

    private BigDecimal itemPrice;
    private BigDecimal amount;

    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;

    private BigDecimal transportationCharges;
    private BigDecimal packingCharges;
    private BigDecimal cuttingCharges;
    private BigDecimal laminationCharges;

    private BigDecimal totalAmount;

    private String billingStatus;
    private Integer creditPeriodDays;
    private String coc;
}

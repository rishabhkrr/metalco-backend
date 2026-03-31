package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private BigDecimal price;
    private Integer creditPeriodDays;
    private String coc;
}

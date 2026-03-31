package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "salesman_incentive_entry")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesmanIncentiveEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String marketingExecutiveName;
    private String userId;
    private String unit;
    private String customerCode;
    private String customerName;
    private String soNumber;
    private String lineNumber;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal orderQuantityKg;
    private String uomKg;
    private Integer orderQuantityNo;
    private String uomNo;
    private LocalDate dispatchDate;
    private BigDecimal dispatchQuantityKg;
    private Integer dispatchQuantityNo;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private Integer creditDays;

    private LocalDate targetDateOfPayment;
    private BigDecimal incentiveRate;
    private BigDecimal incentiveAmount;
    private String paymentStatus;
    private BigDecimal amountReceived;
    private Integer numberOfDaysLapse;
    private LocalDate dateOfPayment;
    private BigDecimal lapseInterestRate;
    private BigDecimal lapseInterestAmount;
    private BigDecimal finalIncentiveAmount;
}


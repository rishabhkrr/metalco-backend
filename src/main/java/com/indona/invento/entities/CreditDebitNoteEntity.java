package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for Credit Note and Debit Note (except sales return)
 */
@Entity
@Table(name = "credit_debit_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditDebitNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    // Auto-generated: MECRD(YY)(MM)(0001) for CREDIT or MEDB(YY)(MM)(0001) for DEBIT
    private String transactionNumber;

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
    
    @Column(length = 2000)
    private String customerBillingAddress;
    
    @Column(length = 2000)
    private String customerShippingAddress;

    // Product details
    private String orderType;
    private String productCategory;
    
    @Column(length = 500)
    private String itemDescription;
    
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    // Financial fields
    @Column(precision = 18, scale = 2)
    private BigDecimal itemPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal transactionAmount;

    @Column(precision = 18, scale = 2)
    private BigDecimal cgst;

    @Column(precision = 18, scale = 2)
    private BigDecimal sgst;

    @Column(precision = 18, scale = 2)
    private BigDecimal igst;

    @Column(precision = 18, scale = 2)
    private BigDecimal totalAmount;

    // Audit fields
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}


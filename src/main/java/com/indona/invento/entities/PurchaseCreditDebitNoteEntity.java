package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "purchase_credit_debit_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseCreditDebitNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String transactionNumber;

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


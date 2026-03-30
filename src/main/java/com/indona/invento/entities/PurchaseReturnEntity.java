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
@Table(name = "purchase_return")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String purchaseReturnNumber; // Format: MEDB(YY)(MM)(SEQUENCE) e.g., MEDB25100001

    // Fields from QR scan
    private String itemNumber;
    private String unit;
    private String supplierCode;
    private String supplierName;
    private String sectionNumber;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;

    private BigDecimal quantityKg;
    private String uomKg;

    private Integer quantityNo;
    private String uomNo;

    // Audit field (nullable)
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


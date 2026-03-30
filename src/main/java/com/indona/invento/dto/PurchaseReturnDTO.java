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
public class PurchaseReturnDTO {

    private Long id;
    private LocalDateTime timestamp;
    private String purchaseReturnNumber; // Format: MEDB(YY)(MM)(SEQUENCE)

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
    private LocalDateTime createdAt;
}


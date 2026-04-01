package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * FRD v3.0 Module 2: CoC Line Item Entity
 * 
 * Key changes:
 * - SO Number MOVED from CoC header to here (first column in table)
 * - Added heatLotNumber (from Stock Summary — Inventorywise, comma-separated if multiple)
 * - Added quantityNo
 */
@Entity
@Table(name = "coc_line_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CocLineItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // NEW: SO Number moved FROM CoC header TO here
    private String soNumber;

    // Line Item Details
    private String lineNumber;
    private String productCategory;
    @Column(length = 500)
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    // Quantity in Kg
    @Column(precision = 18, scale = 3)
    private BigDecimal quantityKgDecimal;
    private String quantityKg;  // backward compat (string)

    // NEW: Quantity in No
    private Integer quantityNo;

    // NEW: Heat/Lot Number — from Stock Summary Inventorywise by Item Description & Batch Number
    // Comma-separated if multiple (COC-BR-007, COC-BR-008)
    @Column(length = 2000)
    private String heatLotNumber;

    // CoC Generated Fields
    private String cocNumber;
    private LocalDateTime cocTimestamp;

    // Reference to parent CoC
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coc_id")
    @JsonBackReference("coc-items")
    private CertificateOfConfidenceEntity coc;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    // Line Item Details
    private String lineNumber;                   // Line Number
    private String productCategory;              // Product Category
    private String itemDescription;              // Item Description
    private String brand;                        // Brand
    private String grade;                        // Grade
    private String temper;                       // Temper
    private String dimension;                    // Dimension (D x L format)
    private String quantityKg;                   // Quantity in Kg

    // CoC Generated Fields
    private String cocNumber;                    // CoC Number for this line item
    private LocalDateTime cocTimestamp;          // Timestamp when CoC was generated

    // Reference to parent CoC
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coc_id")
    @JsonIgnore
    private CertificateOfConfidenceEntity coc;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


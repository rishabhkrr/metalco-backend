package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "scrap_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScrapSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unit;
    private String dimension;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String machineName;
    private String soNumber;
    private String lineNumber;

    private BigDecimal producedQtyKg;
    private Integer producedQtyNo;

    // 🆕 End Piece dimensions
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal thickness;
    private String batchNumber;

    private Instant timestamp;
}


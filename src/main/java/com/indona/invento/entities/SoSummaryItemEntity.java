package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "so_summary_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoSummaryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lineNumber;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String productionStrategy;
    private BigDecimal orderQuantityKg;
    private String uomKg;
    private Integer orderQuantityNo;
    private String uomNo;
    private Integer creditDays;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
    private LocalDate targetDispatchDate;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "Asia/Kolkata")
    private LocalDate dispatchDate;
    private BigDecimal dispatchQuantityKg;
    private Integer dispatchQuantityNo;
    private BigDecimal amount;
    private BigDecimal totalAmount;
    private String invoiceNumber;
    private String lrNumberUpdation;
    private String soStatus;
    private BigDecimal price;

    @ManyToOne
    @JoinColumn(name = "summary_id")
    @JsonBackReference
    private SoSummaryEntity summary;
}


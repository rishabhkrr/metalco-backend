package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonRawValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lineNumber; // Auto-generated
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private double quantityKg;
    private String uomKg;
    private double quantityNos;
    private String uomNos;
    private double currentPrice;
    private String orderMode;
    private String productionStrategy;

    private Integer creditPeriod;
    private LocalDate targetDispatchDate;
    private String status;
    private Boolean packing;

    private BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "sales_order_id")
    @JsonBackReference
    private SalesOrder salesOrder;

    // @PrePersist
    // public void onCreate() {
    // this.lineNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    //
    // }

    @Column(name = "sl_no")
    private Integer slNo;

    @Column(columnDefinition = "VARCHAR(MAX)")
    @JsonRawValue
    private String priceSnapshot;

    @Column(columnDefinition = "VARCHAR(MAX)")
    @JsonRawValue
    private String stockSummary;

}

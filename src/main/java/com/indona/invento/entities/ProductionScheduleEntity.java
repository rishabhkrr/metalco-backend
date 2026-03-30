package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "production_schedule")
public class ProductionScheduleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nextProcess;

    private String machineName;

    private String soNumber;

    private String lineNumber;
    private String unit;
    private String customerCode;
    private String customerName;
    private Boolean packing;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private BigDecimal rmQuantityKg;
    private String brand;
    private String grade;
    private String temper;

    private String dimension;
    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;

    private Double targetBladeSpeed;
    private Double targetFeed;

    private String uomKg;
    private String uomNo;

    private String startTime;

    private String endTime;

    private String nextProductionProcess;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDispatchDate;

    // Status: PENDING, IN_PROGRESS, COMPLETED, CANCELLED
    @Column(columnDefinition = "VARCHAR(255) DEFAULT 'PENDING'")
    private String status = "PENDING";
}

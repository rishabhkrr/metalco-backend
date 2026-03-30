package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "packing_scheduler")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackingEntityScheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String soNumber;
    private String lineNumber;
    private String unit;
    private String customerCode;
    private String customerName;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal quantityKg;
    private String uomKg;
    private Integer quantityNo;
    private String uomNo;

    private LocalDate targetDateOfDispatch;

    private String packingInstructions; // null initially
    private String packingStatus;       // default: "PENDING"
}

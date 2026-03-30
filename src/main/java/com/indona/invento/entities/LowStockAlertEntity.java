package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Entity
@Table(name = "low_stock_alert")
public class LowStockAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private BigDecimal quantityKg;
    private BigDecimal reorderLevel;
    private BigDecimal reorderQuantity;
    private String unit;
    private String productCategory;
    private String materialType;
    private Date createdAt;

    private String prNumber;

    // getters and setters
}

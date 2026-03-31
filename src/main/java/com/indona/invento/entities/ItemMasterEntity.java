package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productCategory;
    private String materialType;
    private String hsnCode;

    private String sectionNumber;
    private String dimension1;
    private String dimension2;
    private String dimension3;
    private String dimension;  // Combined dimension like "76X120X1800"

    private String grade;
    private String temper;
    private String brand;
    private String narration;

    private String skuDescription;

    private String supplierCode;
    private String supplierName;

    private String primaryUom;
    private String altUomApplicable;
    private String altUom;
    private String reportingUom;

    private Integer leadTimeDays;
    private BigDecimal moq;

    private String unitName;
    private String gstApplicable;
    private BigDecimal gstRate;
    private BigDecimal openingStockInKgs;
    private BigDecimal openingStockInNos;
    private BigDecimal itemPrice;
    private String status;           // Pending / APPROVED / REJECTED (default: Pending)
}
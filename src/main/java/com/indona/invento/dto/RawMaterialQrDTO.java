package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialQrDTO {

    private String slNo;
    private String grnNumber;
    private String itemDescription;
    private String productCategory;
    private String sectionNumber;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String store;
    private String storageArea;
    private String rackNo;
    private String columnNo;
    private String binNo;
    private String rackColumnBin;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private String batchNumber;

    /** Unit code for FRD QR format */
    private String unitCode;

    /** Item code from Item Master for FRD QR format */
    private String itemCode;
}

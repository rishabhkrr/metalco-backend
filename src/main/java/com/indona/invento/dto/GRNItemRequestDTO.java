package com.indona.invento.dto;

import lombok.Data;

@Data
public class GRNItemRequestDTO {
    private String itemDescription;
    private String sectionNumber;
    private String grade;
    private String temper;
    private Double poQuantityKg;
    private String uom;
    private Double rate;
    private Double value;

    private Double receivedGrossWeight;
    private Double receivedNetWeight;
    private Integer receivedNo;
    private String heatNumber;
    private String lotNumber;

    private String testCertificateNumber;
    private String productCategory;
    private String brand;
    private Double requestedQty;
    private String materialType;
    private String poNumber;
}

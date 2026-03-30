package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNItemResponseDTO {
    private Long id; // optional if persisted separately
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
    private String requestedQty;
    private String materialType;
    private String poNumber;
}

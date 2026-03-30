package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingSubmissionDTO {
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
    private String packingType;
    private String pdf;
    private BigDecimal weighmentQuantityKg;
    private Integer weighmentQuantityNo;

    // OneToMany - List of batch details
    private List<PackingBatchDetailDTO> batchDetails;
}

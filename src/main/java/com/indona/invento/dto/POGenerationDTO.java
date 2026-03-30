package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class POGenerationDTO {
    private String supplierCode;
    private String supplierName;
    private Date timeStamp;
    private String poGeneratedBy;
    private String billingAddress;
    private String shippingAddress;
    private String remarks;
    private String dispatchThrough;
    private String otherReference;
    private List<String> termsOfDelivery;
    private String pdflink;
    private String unit;
    private LocalDate poValidity;  // PO Validity Date

    private List<POGenerationItemDTO> items;
}

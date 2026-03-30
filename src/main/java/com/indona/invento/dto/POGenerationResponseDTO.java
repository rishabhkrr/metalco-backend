package com.indona.invento.dto;

import com.indona.invento.entities.POGenerationItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class POGenerationResponseDTO {
    private Long id;
    private String supplierCode;
    private String supplierName;
    private Date timeStamp;
    private String poNumber;
    private Double poQuantityKg;
    private String poGeneratedBy;
    private String billingAddress;
    private String shippingAddress;
    private String poStatus;
    private String remarks;
    private String pdflink;
    private String dispatchThrough;
    private String otherReference;
    private String unit;
    private Double rmReceivedQty;
    private Double poReceivedQty;
    private List<String> termsOfDelivery;
    private List<POGenerationItemEntity> items;
    private String rmReceiptStatus;
    private LocalDate poValidity;  // PO Validity Date
}


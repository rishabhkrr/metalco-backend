package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class POGenerationItemDTO {
    private String prNumber;
    private String prCreatedBy;
    private String unit;
    private String deliveryAddress;
    private String sectionNo;
    private String itemDescription;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private Double requiredQuantity;
    private String uom;
    private String prTypeAndReasonverification;
    private String rmReceiptStatus;
    private String soLineNumber;

}

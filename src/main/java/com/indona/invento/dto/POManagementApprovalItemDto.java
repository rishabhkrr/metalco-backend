package com.indona.invento.dto;

import lombok.Data;

@Data
public class POManagementApprovalItemDto {
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
    private Integer requiredQuantity;
    private String uom;
    private String prTypeAndReasonVerifiaction;
}

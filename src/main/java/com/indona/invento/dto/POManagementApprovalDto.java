package com.indona.invento.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class POManagementApprovalDto {
    private String poNumber;
    private String unit;
    private LocalDate orderDate;
    private String productCategory;
    private Integer quantity;
    private String supplier;
    private Integer supplierLeadTime;
    private Integer supplierMOQ;
    private String poGeneratedBy;
    private String billingAddress;
    private String shippingAddress;
    private String remarks;

    private List<POManagementApprovalItemDto> items;
}

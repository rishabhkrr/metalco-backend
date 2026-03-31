package com.indona.invento.dto;

import lombok.Data;

import java.util.Date;


@Data
public class PurchaseFollowUpDTO {
    private String itemDescription;
    private String poNumber;
    private String supplierName;
    private String unit;
    private String status;
    private Integer requiredQuantity;
    private Date orderDate;
}

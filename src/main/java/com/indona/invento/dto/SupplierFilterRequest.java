package com.indona.invento.dto;

import lombok.Data;

@Data
public class SupplierFilterRequest {
    private String gstRegistrationType;
    private String supplierCategory;
    private String supplierType;
    private String supplierCode;
    private String supplierName;
    private String mailingBillingName;
    private String supplierNickname;
    private Boolean multipleAddress;
    private String gstOrUin;
    private String gstStateCode;
    private String pan;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;
    private String interestCalculation;
    private Double rateOfInterest;
    private String brand;
}


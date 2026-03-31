package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerRegistrationVerificationDto {
    private String customerName;
    private String customerAddress;
    private String customerPhoneNo;
    private String customerEmail;
    private String customerArea;
    private String state;
    private String country;
    private String pincode;
    private String gstUin;
    private String altEmail;
    private String altPhone;
    private String pan;
    private String gstCertificate;
    private String remarks;
    private String unit;
    private Integer creditDays;
    private Double creditLimit;
    private String customerCategory;
    private String additionalRemarks;
}

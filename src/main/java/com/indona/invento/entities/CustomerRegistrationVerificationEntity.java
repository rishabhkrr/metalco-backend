package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customer_registration_verification")
@Data
public class CustomerRegistrationVerificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String customerCode;

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

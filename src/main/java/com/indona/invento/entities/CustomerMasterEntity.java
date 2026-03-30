package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "customer_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerMasterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String gstRegistrationType;
    private String gstOrUin;         // GST/UIN or UNREGISTERED
    private String customerCategory;                    // Yes/No from dropdown

//    @Column(unique = true, nullable = false)
    private String customerCode;             // Auto-generated code like MECU0001

    private String customerName;             // Uppercase manual
    private String mailingBillingName;       // Manual
    private String customerNickname;         // Manual
    private Boolean multipleAddress;

    private Double creditLimitAmount;
    private Integer creditLimitDays;
    private Integer lockPeriodDays;

    private String pan;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;
    private String gstStateCode;


    private String gstCertificateLink;       // manual file link
    private String typeOfIndustry;           // Dropdown: Manufacturer/Trader
    private String applicationOfIndustry;    // Dropdown: Aerospace/Defence
    private String interestCalculation;     // Yes/No
    private Double rateOfInterest;           // % per annum
    private String otherDocuments;

    private String typeOfEntity;             // Type of entity

    // New fields for customer relationship and file uploads
    private String relatedStatus;    // Related / Non-Related
    private String panFileUpload;    // Pan file upload link/path
    private String udyamFileUpload;  // Udyam file upload link/path
    private String status;           // Pending / APPROVED / REJECTED (default: Pending)

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerAddressEntity> addressDetails;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerContactEntity> contactDetails;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomerBankDetailEntity> bankDetails;


}

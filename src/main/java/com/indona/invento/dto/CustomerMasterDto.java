package com.indona.invento.dto;

import lombok.*;

import java.util.List;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerMasterDto {

    private String gstRegistrationType;
    private String gstOrUin;       // GST/UIN or UNREGISTERED
    private String customerCategory;                  // Yes/No from dropdown
    private String customerCode;           // Auto-generated code like MECU0001
    private String customerName;           // Manual input - UPPER
    private String mailingBillingName;     // Manual input - UPPER
    private String customerNickname;       // Manual
    private Boolean multipleAddress;       // Yes/No

    private Double creditLimitAmount;
    private Integer creditLimitDays;
    private Integer lockPeriodDays;

    private String pan;
    private String gstStateCode;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;

    private String gstCertificateLink;     // File path or URL
    private String otherDocuments;         // Optional documents
    private String typeOfIndustry;         // Manufacturer / Trader
    private String applicationOfIndustry;  // Aerospace / Defence

    private String interestCalculation;
    private Double rateOfInterest;

    private String typeOfEntity;  // Type of entity (Customer/Dealer/etc)

    // New fields for customer relationship and file uploads
    private String relatedStatus;    // Related / Non-Related
    private String panFileUpload;    // Pan file upload link/path
    private String udyamFileUpload;  // Udyam file upload link/path

    private List<CustomerAddressDTO> addressDetails; // 🏠 Address list
    private List<CustomerContactDTO>  contactDetails;
    private List<CustomerBankDetailDTO>  bankDetails;
}

package com.indona.invento.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorMasterDto {

    private String gstRegistrationType;
    private String supplierCategory;
    private String subContractorType;
    private String subContractorCode;
    private String subContractorName;
    private String mailingBillingName;
    private String supplierNickname;
    private Boolean multipleAddresses;

    private String gstOrUin;
    private String gstStateCode;
    private String pan;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;

    private String gstCertificatePath;
    private String otherDocumentsPath;

    private String interestCalculation;
    private Double rateOfInterest;

    private String typeOfEntity;  // Type of entity (SubContractor/Vendor/etc)

    // New fields for sub-contractor relationship and file uploads
    private String relatedStatus;    // Related / Non-Related
    private String panFileUpload;    // Pan file upload link/path
    private String udyamFileUpload;  // Udyam file upload link/path

    private List<SubContractorAddressDto> addressDetails;
    private List<SubContractorContactDto> contactDetails;
    private List<SubContractorBankDto> bankDetails;
}

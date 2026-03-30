package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewCustomerDto {
    private String gstRegistrationType;
    private String gstOrUin;
    private String customerCategory;
    private String customerName;
    private String mailingBillingName;
    private String customerNickname;
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
    private String gstCertificateLink;
    private String typeOfIndustry;
    private String applicationOfIndustry;
    private String interestCalculation;
    private Double rateOfInterest;
    private String otherDocuments;
    private String status;
    private String customerBased;

    private List<CustomerAddressDTO> addressDetails;
    private List<CustomerContactDTO> contactDetails;
    private List<CustomerBankDetailDTO> bankDetails;
}


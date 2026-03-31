package com.indona.invento.dto;

import lombok.*;

import java.util.List;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitMasterDto {

    private String gstRegistrationType;
    private String unitCode;
    private String unitName;
    private String unitAddress;
    private String area;

    private String state;
    private String country;
    private String pincode;
    private String mapLocation;

    private String gstOrUin;
    private String gstStateCode;

    private String pan;
    private String isTanAvailable;
    private String tanNumber;
    private String isUdyamAvailable;
    private String udyamNumber;
    private String isIecAvailable;
    private String iecCode;

    private String gstCertificate;       // path or file reference
    private String otherDocuments;       // folder path or zipped ref

    private String typeOfEntity;  // Type of entity (Unit/Branch/etc)

    private List<UnitContactDetailsDto> contactDetails;
    private List<UnitBankDetailsDto> bankDetails;
    private List<UnitAddressDetailsDto> addressDetails;
}

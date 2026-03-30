package com.indona.invento.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitAddressDetailsDto {

    private Boolean Primary;
    private String branchName;
    private String address;
    private String supplierArea;
    private String state;
    private String country;
    private String pincode;
    private String mapLocation;
}

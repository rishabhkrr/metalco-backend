package com.indona.invento.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDetailsDto {
    private String branchName;
    private String address;
    private String supplierArea;
    private String state;
    private String country;
    private String pincode;
    private String mapLocation;
    private boolean Primary;
}

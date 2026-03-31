package com.indona.invento.dto;


import lombok.Data;

@Data

public class SupplierCodeNameDTO {
    private String supplierCode;
    private String supplierName;

    public SupplierCodeNameDTO(String supplierCode, String supplierName) {
        this.supplierCode = supplierCode;
        this.supplierName = supplierName;
    }

    // Getters and setters
}


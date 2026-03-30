package com.indona.invento.dto;

import lombok.Data;

import java.util.List;

@Data
public class PORequestDTO {

    private Long id;

    private String orderType;
    private String supplierCode;
    private String supplierName;
    private String unit;
    private String unitCode;
    private String soNumberLineNumber;
    private String prCreatedBy;
    private String reasonForRequest;
    private List<POProductDTO> products;
    private String status;


}


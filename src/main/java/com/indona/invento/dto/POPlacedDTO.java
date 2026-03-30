package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class POPlacedDTO {
    private Long id;
    private String poNumber;
    private String supplierCode;
    private String supplierName;
    private Date timeStamp;
    private String billingAddress;
    private String shippingAddress;
}


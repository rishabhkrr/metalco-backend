package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesOrderAddressCreditDTO {
    private String shippingAddress;
    private String billingAddress;
    private String creditPeriod;



}


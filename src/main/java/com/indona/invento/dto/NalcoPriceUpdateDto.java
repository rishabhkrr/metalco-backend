package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NalcoPriceUpdateDto {
    private BigDecimal nalcoPrice;
    private String pricePdf;
    private String uom;

    // Getters and Setters
}
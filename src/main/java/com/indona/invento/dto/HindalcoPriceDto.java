package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HindalcoPriceDto {
    private BigDecimal price;
    private String uom;
    private String pricePdfPath;
}

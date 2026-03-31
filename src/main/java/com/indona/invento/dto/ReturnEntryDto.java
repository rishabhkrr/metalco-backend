package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnEntryDto {

    private Long id;
    private Integer slNo;
    private String returnStore;
    private BigDecimal weighmentQuantityKg;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String unit;
}

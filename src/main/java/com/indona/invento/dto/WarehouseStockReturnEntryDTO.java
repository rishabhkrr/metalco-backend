package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockReturnEntryDTO {
    private Integer slNo;
    private String returnStore;
    private BigDecimal weighmentQuantityKg;

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String unit;
}


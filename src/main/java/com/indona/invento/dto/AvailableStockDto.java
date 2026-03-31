package com.indona.invento.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
@Builder
@Data
public class AvailableStockDto {
    private Long id;
    private String unit;
    private String brand;
    private String grade;
    private String temper;
    private String itemDescription;
    private String dimension;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private BigDecimal itemPrice;
    private String inventoryType; // End Piece, Loose Piece, Bulk Inventory
}

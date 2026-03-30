package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBlockDTO {
    private String category;
    private String description;
    private String brand;
    private String grade;
    private String temper;
    private Double quantity;
    private String uom;
}

package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning packing schedule details by SO Number and Line Number
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackingScheduleDetailsDTO {
    private String customerCode;
    private String customerName;
    private String orderType;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String itemDescription;
    private Boolean packing;
}


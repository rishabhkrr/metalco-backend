package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RfdListItemDTO {
    private Long id;
    private String soNumber;
    private String lineNumber;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private int batchCount;  // Number of batches for this SO/Line
}

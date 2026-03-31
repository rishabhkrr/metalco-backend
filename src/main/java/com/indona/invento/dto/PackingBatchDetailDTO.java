package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingBatchDetailDTO {
    private String itemDescription;
    private String itemDimensions;
    private String batchNumber;
    private String dateOfInward;
    private BigDecimal qtyKg;
    private Integer qtyNo;
}


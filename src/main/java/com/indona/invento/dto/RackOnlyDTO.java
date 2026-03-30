package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RackOnlyDTO {
    private String rackColumnShelfNumber;
    private String store;
    private String storageArea;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private BigDecimal itemPrice;
}
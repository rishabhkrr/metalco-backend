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
public class RackWiseDTO {
    private String rackColumnShelfNumber;
    private String store;
    private String storageArea;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private BigDecimal itemPrice;
    private Object grnNumbers;  // ✅ Added - array of GRN numbers for this rack
}
package com.indona.invento.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockRetrievalEntryDTO {
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
}


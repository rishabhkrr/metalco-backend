package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockTransferWHRetrievalQtyDto {
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
}

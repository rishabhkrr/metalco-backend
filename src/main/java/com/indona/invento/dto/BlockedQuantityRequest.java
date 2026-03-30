package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockedQuantityRequest {
    private String quotationNo;
    private String customerName;
    private String marketingExecutiveName;
    private String pdflink;
    private List<ProductRequest> products;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductRequest {
        private String itemDescription;
        private BigDecimal availableQuantityKg;
    }
}

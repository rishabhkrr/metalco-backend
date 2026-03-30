package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllocateReturnRackDTO {
    private String unit;
    private String itemDescription;
    private String productCategory;
    private BigDecimal returnQuantityKg;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SuggestedRackDTO {
        private String store;  // Always "LOOSE PIECE"
        private String storageArea;
        private String rackColumnBin;
        private Double availableCapacity;
        private Double distance;
        private Integer storageAreaOrder;
        private String itemCategory;
        private Boolean isAllocated;  // true if allocated
    }
}


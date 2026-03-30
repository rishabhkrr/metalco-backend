package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockSummaryFormattedDTO {
    private String itemDescription;
    private String unit;
    private String store;
    private String storageArea;
    private String rackColumnShelfNumber;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private Boolean reprintQr;
    private BigDecimal totalQuantityKg;
    private Integer totalQuantityNo;
    private BigDecimal averageItemPrice;
    private String materialType;
    private String itemGroup;  // SFG (Semi Finished Goods) or FG (Finished Goods)
    private List<RackWiseDTO> rackWise;
}

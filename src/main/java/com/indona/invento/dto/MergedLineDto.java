package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergedLineDto {
    private String itemDescription;
    private String poNumber;

    // ✅ New representative fields
    private String slNo;
    private String productCategory;
    private String sectionNumber;
    private String brand;
    private String grade;
    private String temper;

    // ✅ Totals
    private BigDecimal totalQuantityKg;
    private Integer totalQuantityNo;
    private Integer numberOfBundles;

    // ✅ Nested original items
    private List<GrnLineItemDto> mergedItems;
}

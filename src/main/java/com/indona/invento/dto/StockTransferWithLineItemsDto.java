package com.indona.invento.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class StockTransferWithLineItemsDto {
    private Long id;
    private String transferNumber;
    private String unitId;
    private String userId;
    private String unitCode;
    private String transferType;
    private String transferQuantity;
    private String grnRefNumber;
    private String invoiceNumber;
    private String unit;
    private String itemDescription;
    private String sectionNumber;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;

    private Double grnQuantityNetWeight;
    private Double addQuantityNetWeight;

    private String grnQuantityNetWeightUom;
    private String addQuantityNetWeightUom;

    private Integer grnQuantityNo;
    private Integer addQuantityNo;

    private String grnQuantityNoUom;
    private String addQuantityNoUom;

    private String currentStore;
    private String storageArea;
    private String rackColumnBinNumber;

    private BigDecimal totalQuantityKg;
    private Integer totalQuantityNo;
    private Integer numberOfBundles;




    // ✅ Attach GRN line items here
    private List<GrnLineItemDto> lineItems;

    private List<MergedLineDto> mergedLines;
}

package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockTransferSummaryDto {

    // Stock Transfer Basic Info
    private Long id;
    private String transferNumber;
    private String transferType;
    private String transferStage;
    private Integer status;
    private Date dateTime;
    private Date dispatchDate;
    private Date recievingDate;
    private String createdBy;

    // GRN Reference Data (Copy-pasted from GRN)
    private String grnRefNumber;
    private String invoiceNumber;
    private String unit;
    private String itemDescription;
    private String sectionNumber;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;

    // GRN Quantities
    private Double grnQuantityNetWeight;
    private String grnQuantityNetWeightUom;
    private Integer grnQuantityNo;
    private String grnQuantityNoUom;

    // Added Quantities (from Add Bundles)
    private Double addedQuantityNetWeight;
    private String addedQuantityNetWeightUom;
    private Integer addedQuantityNo;
    private String addedQuantityNoUom;
    private Integer numberOfBundles;

    // Warehouse Storage Fields
    private String currentStore;
    private String recipientStore;
    private String storageArea;
    private String rackColumnBinNumber;

    // Store/Warehouse Info
    private Long fromStore;
    private Long toStore;

    // Bundles for this Stock Transfer (for "View Bundles" button)
    private List<GrnLineItemDto> bundles;
}

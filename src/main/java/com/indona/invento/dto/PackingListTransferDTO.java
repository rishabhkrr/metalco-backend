package com.indona.invento.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingListTransferDTO {
    private String transferType;
    private String soNumber;
    private String lineNumber;
    private String unit;
    private String customerCode;
    private String customerName;
    private String packingStatus;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal quantityKg;
    private String uomKg;

    private Integer quantityNo;
    private String uomNo;

    // Customer & Dispatch details
    private String customerBillingAddress;
    private String customerShippingAddress;
    private String customerPoNumber;
    private String customerPoDate;
    private String vehicleNumber;
    private String dispatchThrough;

    // Charges
    private BigDecimal itemRate;
    private BigDecimal taxableValue;
    private BigDecimal packingCharges;
    private BigDecimal freightCharges;
    private BigDecimal cuttingCharges;
    private BigDecimal laminationCharges;
    private BigDecimal hamaliCharges;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal totalValue;

    // Batch details
    private String batchDetails; // JSON string
}

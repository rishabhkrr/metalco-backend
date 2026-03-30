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
public class ItemEnquiryDTO {
    private String userId;
    private String unitId;
    private Integer productSelectedId;
    private String marketingExecutiveName;
    private String unitName;
    private String unitCode;
    private String unitGstNumber;
    private String unitEmail;
    private String unitAddress;
    private String pdfLink;

    private String branchName;
    private String address;
    private String supplierArea;
    private String state;
    private String country;
    private Integer pincode;

    private String customerName;
    private String customerCode;
    private String customerType;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;

    private String note1;
    private String note2;
    private String materialType;
    private String productDetailNote;
    private Double currentSellingPrice;

    private String orderType;
    private int deliveryDays;
    private List<String> additionalCharges;
    private List<ItemEnquiryMoqDTO> moq;
    private String taxes;
    private boolean pvcApplicable;
    private String paymentTerms;
    private int validityHours;
    private String remarks;

    private String status;

    // Tax and Charges - simple fields
    private BigDecimal cgst;              // CGST (9%)
    private BigDecimal sgst;              // SGST (9%)
    private BigDecimal igst;              // IGST (18%)
    private BigDecimal freightCharges;    // Freight charges
    private BigDecimal hamaliCharges;     // Hamali charges
    private BigDecimal packingCharges;    // Packing Charges
    private BigDecimal cuttingCharges;    // Cutting Charges
    private BigDecimal laminationCharges; // Lamination Charges
    private BigDecimal subTotalAmount;    // Sub Total Amount
    private BigDecimal totalAmount;       // Total Amount

    private List<ItemEnquiryProductDTO> products;

    private String updatedBy;
}

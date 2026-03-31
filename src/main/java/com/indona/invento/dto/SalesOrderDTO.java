package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrderDTO {
    private String quotationNo;
    private String userId;
    private String unit;
    private String unitCode;
    private String customerPoNo;
    private String customerPoFile;
    private String customerCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String billingAddress;
    private String shippingAddress;
    private Boolean sameAsBillingAddress;
    private String marketingExecutiveName;
    private String managementAuthority;
    private Boolean packingRequired;
    private String pdflink;
    private String status;

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


    private Boolean acknowledgementSent;
    private Boolean approvalLinkSent;

    // SO Approval for Overdue Customers
    private Boolean customerOverdue;

    private PackingInstructionDTO packingInstruction;

    private List<SalesOrderLineItemDTO> items;
}


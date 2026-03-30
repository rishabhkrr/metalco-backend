package com.indona.invento.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateOfConfidenceDTO {

    private Long id;
    private String cocNumber;
    private LocalDateTime timestamp;

    // Bill Summary Information
    private String soNumber;
    private String invoiceNumber;
    private String dispatchedQuantity;
    private String unit;

    // Customer PO Information
    private String customerPONumber;
    private LocalDateTime customerPODate;
    private String customerPOQuantity;

    // Customer Information
    private String customerCode;
    private String customerName;
    private String customerBillingAddress;
    private String customerShippingAddress;
    private String customerEmail;
    private String customerPhone;

    // Declaration
    private String declaration;

    // Line Items
    private List<CocLineItemDTO> lineItems;

    // Available line items for selection (used in form)
    private List<CocLineItemDTO> availableLineItems;

    // Selected line items (used when saving)
    private List<CocLineItemDTO> selectedLineItems;

    // Status
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


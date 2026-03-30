package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "certificate_of_confidence")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateOfConfidenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CoC Header Information
    private String cocNumber;                    // Auto-generated CoC Number
    private LocalDateTime timestamp;             // CoC Generation Timestamp

    // Bill Summary Information
    private String soNumber;                     // Sales Order Number
    private String invoiceNumber;                // Invoice Number
    private String dispatchedQuantity;           // Dispatched Quantity
    private String unit;                         // Unit

    // Customer PO Information
    private String customerPONumber;             // Customer PO Number
    private LocalDateTime customerPODate;        // Customer PO Date
    private String customerPOQuantity;           // Customer PO Quantity

    // Customer Information
    private String customerCode;                 // Customer Code
    private String customerName;                 // Customer Name
    private String customerBillingAddress;       // Customer Billing Address
    private String customerShippingAddress;      // Customer Shipping Address
    private String customerEmail;                // Customer Email
    private String customerPhone;                // Customer Phone Number

    // Declaration
    @Column(columnDefinition = "TEXT")
    private String declaration;                  // Declaration text

    // CoC Line Items
    @OneToMany(mappedBy = "coc", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CocLineItemEntity> lineItems;

    // Status
    private String status;                       // DRAFT, GENERATED, SENT
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


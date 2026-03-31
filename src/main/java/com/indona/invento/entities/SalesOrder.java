package com.indona.invento.entities;



import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String status;

    private String soNumber; // Auto-generated
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

    @OneToOne(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private PackingInstruction packingInstruction;



    private LocalDate targetDispatchDate;
    @Column(name = "acknowledgement_sent", nullable = true)
    private Boolean acknowledgementSent;
    private Boolean approvalLinkSent;

    // SO Approval for Overdue Customers
    @Column(name = "customer_overdue")
    private Boolean customerOverdue;

    @Column(name = "approval_remarks")
    private String approvalRemarks;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SalesOrderLineItem> items = new ArrayList<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.soNumber = "SO" + System.currentTimeMillis(); // Simple auto-gen logic
    }
}

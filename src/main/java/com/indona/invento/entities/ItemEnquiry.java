package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEnquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String quotationNo;
    private String userId;
    private String unitId;
    private String marketingExecutiveName;
    private String unitAddress;
    private String unitCode;
    private String pdfLink;

    private String branchName;
    private String address;
    private String supplierArea;
    private String state;
    private String country;
    private Integer pincode;

    private String unitName;
    private String unitGstNumber;
    private String unitEmail;

    private String customerName;
    private String customerCode;
    private String customerType;
    private String customerAddress;
    private String customerPhone;
    private String customerEmail;

    private String note1;
    private String note2;

    private String productDetailNote;
    private Double currentSellingPrice;

    private String orderType;
    private int deliveryDays;
    private String taxes;
    private boolean pvcApplicable;
    private String paymentTerms;
    private int validityHours;
    private String remarks;
    private String status;
    private String approvalStatus;  // For approve/reject API - values: PENDING, APPROVED, REJECTED
    private Boolean isLocked;
    private String blockedPdflinked;

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

    @ElementCollection
    private List<String> additionalCharges;

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemEnquiryMoq> moq = new ArrayList<>();

    @OneToMany(mappedBy = "enquiry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemEnquiryProduct> products = new ArrayList<>();


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Kolkata")
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

}

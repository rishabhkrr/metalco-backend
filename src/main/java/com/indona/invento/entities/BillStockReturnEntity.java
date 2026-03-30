package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bill_stock_return")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillStockReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String salesReturnNumber; // Format: MECRD(YY)(MM)(0001)

    // --- FRD §12.3: Header Fields ---
    private String dcNumber;              // From Gate Entry Inward (Mode="Sales Return")
    private String eWayBill;              // Auto-populated from DC Number
    private String vehicleNumber;         // Auto-populated from DC Number
    private String dcDebitNoteNumber;     // Manual entry — new field
    private String eWayBillNumberReturn;  // Manual entry — new field
    private String vehicleNumberReturn;   // Manual entry — new field

    // Reference fields from billing
    private String invoiceNumber;
    private String soNumber;
    private String lineNumber;

    // Auto-filled fields from BillingSummary (§12.4 — "Unit" removed per SR-BR-014)
    private String unit;
    private String customerCode;
    private String customerName;
    private String customerBillingAddress;
    private String customerShippingAddress;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String uomKg;
    private String uomNo;
    private BigDecimal itemPrice;

    // --- FRD §12.5: Dispatch Quantities (auto from Invoice) ---
    private BigDecimal dispatchQuantityKg;
    private BigDecimal dispatchQuantityNo;

    // User-entered fields
    private BigDecimal returnQuantityKg;
    private BigDecimal returnQuantityNo;
    private BigDecimal amount;
    private BigDecimal cgst;
    private BigDecimal sgst;
    private BigDecimal igst;
    private BigDecimal transportationCharges;
    private BigDecimal totalAmount;

    // Stock selection: "Rejection" or "General" (§12.6 — Material Acceptance)
    private String stockSelection;

    // --- FRD §12.10: Approval Workflow ---
    @Builder.Default
    private String approvalStatus = "Pending"; // Pending / Approved / Rejected

    // Audit fields
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
        if (this.approvalStatus == null) {
            this.approvalStatus = "Pending";
        }
    }
}

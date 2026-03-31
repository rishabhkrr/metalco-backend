package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "delivery_challan_creation_iumt")
public class DeliveryChallanCreationIUMTEntity {

    private static final DateTimeFormatter PL_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String packingListNumber;
    private String mrNumber;
    private String lineNumber;
    private String DCNumber;
    private String vehicleNumberPackingAndDispatch;

    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    private String requestingCode;
    private String requestingName;
    private String requestingBillingAddress;
    private String requestingShippingAddress;

    private BigDecimal totalAmount;
    private String status;
    private String dispatchThrough;
    private String otherReference;
    private String remarks;
    private String termsOfDelivery;
    private String destination;

    private String ewayBillNumber;
    private String unit;
    private String materialType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;

    private BigDecimal quantityKg;
    private String uomKg;
    private Integer quantityNo;
    private String uomNo;
    private BigDecimal itemPrice;
    private BigDecimal amount;

    // FRD: GRN-001 — GRN Status tracking
    @Column(name = "grn_status")
    private String grnStatus = "Pending"; // Pending / Completed

    // FRD: GST-001 to GST-005 — Financial fields
    private BigDecimal subTotalAmount;
    private BigDecimal igstPercent;
    private BigDecimal igstAmount;

    // FRD: DCC-007 — Sender unit GST details
    private String senderUnitGst;
    private String senderUnitState;

    // FRD: RUD-005/006 — Requesting unit GST details
    private String requestingUnitGst;
    private String requestingUnitState;

    // FRD: PDI-003/004 — Item details for PDF
    private String hsnCode;
    private String dimension;

    // FRD: PDF-005 — PAN number for PDF footer
    private String panNumber;

    private String vehicleOutStatusPackingAndDispatch;

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }

    @Transient
    public String getTimestamp() {
        if (this.timestamp == null) return null;

        return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.of("Asia/Kolkata"))
                .format(this.timestamp);
    }
}

package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "packing_list_transfer")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingListTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    private String packingListNumber;  // RFD List Number

    private String transferType;  // "Inhouse" / "Jobwork"

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
    private String transferStatus;

    // Customer & Dispatch details
    @Column(columnDefinition = "TEXT")
    private String customerBillingAddress;
    @Column(columnDefinition = "TEXT")
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

    // Batch details stored as JSON text
    @Column(name = "batch_details", columnDefinition = "TEXT")
    private String batchDetails;
}

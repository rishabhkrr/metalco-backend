package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "grn_inter_unit_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNInterUnitItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemDescription;
    private String productCategory;
    private String lineNumber;
    private String sectionNumber;   // FRD: Section Number field
    private String brand;
    private String grade;
    private String temper;
    private String uom;
    private String dimension;       // FRD: QR data field
    private String materialType;
    private Double quantityKg;
    private Integer quantityNo;     // FRD: Quantity (Numbers) from MR
    private String testCertificateNumber;
    private Double receivedNetWeight;
    private Integer receivedNo;

    // FRD: IUMR-006/008/009 — Batch-level details
    private String heatNumber;
    private String lotNumber;
    private String batchNumber;     // Auto-generated on save
    private Double itemPrice;       // Transfer price

    // FRD: IUMR-007 — QR Scan verification
    @Builder.Default
    private String scanStatus = "Pending"; // Pending / Verified

    @ManyToOne
    @JoinColumn(name = "grn_inter_unit_id")
    private GRNInterUnitEntity grnInterUnit;
}

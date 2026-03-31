package com.indona.invento.entities;


import jakarta.persistence.*;
import lombok.*;
import java.util.Date;
import java.util.List;


    @Entity
    @Table(name = "grn_inter_unit")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class GRNInterUnitEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String grnInterUnitRefNumber; // IU GRN Number (MEGRN+YYMM+seq)
        private String medcNumber;
        private String mrNumber;
        private String gateEntryRefNo;
        private String vehicleNumber;
        private String unit;
        private String senderUnit;      // FRD: Sender unit selection
        private String supplierUnit;
        private String mode;
        private String invoiceNumber;
        private String ewayBillNumber;
        private String supplierCode;
        private String supplierName;

        private String invoiceDocument;
        private String testCertificateDocument;
        private String ewayBillDocument;
        private String vehicleDocuments;
        private Double vehicleLoadWeightKg;
        private Double vehicleEmptyWeightKg;
        private Double weighmentQuantity;
        private String weighmentRefNumber;

        // FRD: GIS-002 Approval workflow
        @Builder.Default
        private String status = "Pending"; // Pending, Approved, Rejected
        private String approvedBy;
        private Date approvedAt;
        @Column(length = 1000)
        private String rejectionRemarks;

        @OneToMany(mappedBy = "grnInterUnit", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<GRNInterUnitItemEntity> items;

        private Date createdAt;
    }


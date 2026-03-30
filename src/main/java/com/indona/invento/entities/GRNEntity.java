package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "grn")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String grnRefNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timeStamp;

    private String unit;
    private String invoiceNumber;
    private String poNumber;

    private String gateEntryRefNo;
    private List<String> testCertificateNumbers;
    private String ewayBillNumber;
    private String vehicleNumber;

    private String invoiceDocument;
    private String testCertificateDocument;
    private String ewayBillDocument;
    private String vehicleDocuments;

    private String supplierName;
    private String supplierCode;

    private Double vehicleLoadWeightKg;
    private Double vehicleEmptyWeightKg;
    private Double weighmentQuantity;

    private String materialUnloadingStatus;
    private String materialUnloadingNotes;
    private String weighmentRefNumber;
    private String mode;
    private String medcNumber;
    private String mrNumber;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;

    @OneToMany(mappedBy = "grn", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GRNItemEntity> grnItems;

    @PrePersist
    protected void prePersist() {
        this.timeStamp = new Date();
        this.createdAt = new Date();
        this.updatedAt = new Date();
        if (this.materialUnloadingStatus == null) this.materialUnloadingStatus = "PENDING";
        if (this.binStatus == null) this.binStatus = "PENDING";
        if (this.status == null) this.status = "PENDING";
    }

    @PreUpdate
    protected void preUpdate() {
        this.updatedAt = new Date();
    }

    @Column(name = "bin_status")
    private String binStatus;

    @Column(name = "status")
    private String status;
}

package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Entity
@Table(name = "gate_inward")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateInwardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    

    private String gatePassRefNumber;
    private String unitCode;
    private String medcNumber;
    private String medciNumber;
    private String mode;
    private String invoiceNumber;
    private String vehicleNumber;
    private String driverName;
    private String status;
    private String purpose;
    private Date timeStamp;
    private Date gateOutTime;

    @ElementCollection
    private List<String> poNumbers;

    private String dcNumber;
    private String eWayBillNumber;

    @ElementCollection
    private List<TCDetails> testCertificates;

    private String vehicleWeighmentStatus;
    private String materialUnloadingStatus;
    private String vehicleOutStatus;

    @ElementCollection
    private List<String> invoiceScanUrls;

    @ElementCollection
    private List<String> dcDocumentScanUrls;

    @ElementCollection
    private List<String> testCertificateScanUrls;

    @ElementCollection
    private List<String> eWayBillScanUrls;

    @ElementCollection
    private List<String> vehicleDocumentsScanUrls;

    @ElementCollection
    private List<String> vehicleWeighmentScanUrls;

    @ElementCollection
    private List<String> materialUnloadingStatusUrls;

    @ElementCollection
    private List<String> medciScanUrls;

    @ElementCollection
    private List<String> medcScanUrls;

    @PrePersist
    protected void onCreate() {
        this.timeStamp = new Date();
    }
}


package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "gate_entry_packing_and_dispatch")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GateEntryPackingAndDispatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "gate_entry_ref_no", unique = true)
    private String gateEntryRefNoPackingAndDispatch;

    private LocalDateTime timeStampPackingAndDispatch;

    private String unitPackingAndDispatch;
    private String medcScan;
    private String medciScan;
    private String medcpScan;
    private String purposePackingAndDispatch;      // Pickup/Delivery
    private String modePackingAndDispatch;         // Sales/Jobwork/Interunit Transfer
    private String invoiceNumberPackingAndDispatch;
    private String medcNumberPackingAndDispatch;
    private String medciNumberPackingAndDispatch;
    private String ewayBillNumberPackingAndDispatch;
    private String vehicleNumberPackingAndDispatch;
    private String driverNamePackingAndDispatch;
    private String medcpNumberPackingAndDispatch;

    private String invoiceScanPackingAndDispatch;          // file path or base64
    private String vehicleDocumentsScanPackingAndDispatch;

    private String vehicleWeighmentStatusPackingAndDispatch;

    private String vehicleOutStatusPackingAndDispatch;

    @PrePersist
    public void prePersist() {
        this.timeStampPackingAndDispatch = LocalDateTime.now();}
}

package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "VEHICLE_WEIGHMENT")
public class VehicleWeighmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weighment_id")
    private Long id;

    @Column(name = "time_stamp")
    private Date timeStamp;

    @Column(name = "weightment_ref_number")
    private String weightmentRefNumber; // e.g. MEWB25050002

    @Column(name = "unit")
    private String unit; // derived from userId

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "medc_or_dc_numbers", columnDefinition = "TEXT")
    private String medcOrDcNumbers; // stored as JSON/comma-separated

    @Column(name = "po_numbers", columnDefinition = "TEXT")
    private String poNumbers; // stored as JSON/comma-separated

    @Column(name = "medci_numbers", columnDefinition = "TEXT")
    private String medciNumbers; // stored as JSON/comma-separated

    @Column(name = "dc_number")
    private String dcNumber;

    @Column(name = "medcp_number")
    private String medcpNumber;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "gate_entry_ref_no")
    private String gateEntryRefNo;

    @Column(name = "vehicle_photo_with_load")
    private String vehiclePhotoWithLoad;

    @Column(name = "vehicle_photo_empty")
    private String vehiclePhotoEmpty;

    @Column(name = "load_weight")
    private Double loadWeight;

    @Column(name = "empty_weight")
    private Double emptyWeight;

    @Column(name = "verified")
    private Boolean verified = false; // checkbox verification flag

    @Column(name = "user_id")
    private String userId; // logged-in user

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "mode")
    private String mode;

    @PrePersist
    public void onCreate() {
        Date now = new Date();
        this.timeStamp = now;
        this.createdAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = new Date();
    }
}

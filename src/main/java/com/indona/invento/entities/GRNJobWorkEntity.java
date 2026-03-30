package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "grn_jobwork")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNJobWorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Example: MEGR+yydd+0001 (auto-generated logic will be handled in service)
    @Column(name = "grn_ref_number", unique = true)
    private String grnRefNumber;

    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;

    private String unit;
    private String medcNumber;
    private String invoiceNumber;
    private String subContractorCode;
    private String subContractorName;

    private String gatePassRefNumber;
    private String eWayBillNumber;
    private String vehicleNumber;
    
    private String dimension;
    private String itemDescription;
    private String orderType;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;

    private Double quantityKg;
    private Integer quantityNo;

    private String grnDimension;
    private Double receivedQuantityKg;
    private String grnUomKg;
    private Integer receivedQuantityNos;
    private String grnUomNo;
    
    private Double scrapQuantityKg;
    private Integer scrapQuantityNo;           // FRD §18.5 #3: Scrap quantity in numbers

    private Double itemPriceMedc;              // FRD §18.4 #3: Item Price from MEDC
    private Double amountAsSent;               // FRD §18.4 #4: Amount (as per sent)
    private Double jobworkRate;
    private Double jobworkValue;
    private Double itemPriceJ;                 // FRD §18.5 #6: = itemPriceMedc + jobworkRate

    private String dcNumber;                   // FRD §18.3: DC Number reference

    @Column(name = "master_approval_status")
    private String masterApprovalStatus;       // FRD §18.7: Pending for Approval / Approved / Rejected

    @ElementCollection
    @CollectionTable(name = "grn_jobwork_invoice_scans", joinColumns = @JoinColumn(name = "grn_id"))
    private List<String> invoiceScanUrls;

    @ElementCollection
    @CollectionTable(name = "grn_jobwork_dc_scans", joinColumns = @JoinColumn(name = "grn_id"))
    private List<String> dcDocumentScanUrls;

    @ElementCollection
    @CollectionTable(name = "grn_jobwork_eway_scans", joinColumns = @JoinColumn(name = "grn_id"))
    private List<String> eWayBillScanUrls;

    @ElementCollection
    @CollectionTable(name = "grn_jobwork_vehicle_scans", joinColumns = @JoinColumn(name = "grn_id"))
    private List<String> vehicleDocumentsScanUrls;

    @Column(name = "weightment_ref_number")
    private String weightmentRefNumber;

    @Column(name = "load_weight")
    private Double loadWeight;

    @Column(name = "empty_weight")
    private Double emptyWeight;

    // Derived field (not persisted)
    @Transient
    private Double weighmentQuantity;

    private String materialUnloadingStatus;

    @PrePersist
    @PreUpdate
    public void calculateDerivedFields() {
        // Weighment quantity
        if (loadWeight != null && emptyWeight != null) {
            this.weighmentQuantity = loadWeight - emptyWeight;
        } else {
            this.weighmentQuantity = null;
        }

        // FRD §18.5 #6: Item Price (J) = Item Price (MEDC) + Job Work Rate
        if (itemPriceMedc != null && jobworkRate != null) {
            this.itemPriceJ = itemPriceMedc + jobworkRate;
        }

        // FRD §18.7: Default approval status on first save
        if (masterApprovalStatus == null || masterApprovalStatus.isEmpty()) {
            this.masterApprovalStatus = "Pending for Approval";
        }
    }
}

package com.indona.invento.entities;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "DEPARTMENTS")
public class StockTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="transfer_id")
    private Long id;

    @Column(name="transfer_number")
    private String transferNumber;

    @Column(name="transfer_type")
    private String transferType;

    @Column(name="transfer_quantity")
    private String transferQuantity;

    @Column(name="from_store")
    private Long fromStore;

    @Column(name="to_store")
    private Long toStore;

    @Column(name="transfer_stage")
    private String transferStage;

    @Column(name="status")
    private Integer status = 1;

    @Column(name="created_by")
    private String createdBy;

    @Column(name="soft_delete")
    private Integer deleteFlag = 0;

    @Column(name="delete_remark")
    private String deleteRemark;

    @Column(name="datetime")
    private Date dateTime;

    @Column(name="dispatch_date")
    private Date dispatchDate;

    @Column(name="recieving_date")
    private Date recievingDate;

    // FRD: STI-001 Input Type
    @Column(name="input_type")
    private String inputType; // GRN, GRN – INTERUNIT T, SALES RETURN, GRN – JOBWORK

    // FRD: STI-006 Material Acceptance
    @Column(name="material_acceptance")
    private String materialAcceptance; // General (default), Rejected

    // ========== GRN Related Fields ==========
    @Column(name="grn_ref_number")
    private String grnRefNumber;

    @Column(name="invoice_number")
    private String invoiceNumber;

    @Column(name="unit")
    private String unit;

    @Column(name="item_description")
    private String itemDescription;

    @Column(name="section_number")
    private String sectionNumber;

    @Column(name="product_category")
    private String productCategory;

    @Column(name="brand")
    private String brand;

    @Column(name="grade")
    private String grade;

    @Column(name="temper")
    private String temper;

    // GRN Quantities
    @Column(name="grn_quantity_net_weight")
    private Double grnQuantityNetWeight;

    @Column(name="grn_quantity_net_weight_uom")
    private String grnQuantityNetWeightUom;

    @Column(name="grn_quantity_no")
    private Integer grnQuantityNo;

    @Column(name="grn_quantity_no_uom")
    private String grnQuantityNoUom;

    // Added Quantities (from Add Bundles)
    @Column(name="added_quantity_net_weight")
    private Double addedQuantityNetWeight;

    @Column(name="added_quantity_net_weight_uom")
    private String addedQuantityNetWeightUom;

    @Column(name="added_quantity_no")
    private Integer addedQuantityNo;

    @Column(name="added_quantity_no_uom")
    private String addedQuantityNoUom;

    @Column(name="number_of_bundles")
    private Integer numberOfBundles;

    // Warehouse Storage Fields
    @Column(name="current_store")
    private String currentStore;

    @Column(name="recipient_store")
    private String recipientStore;

    @Column(name="storage_area")
    private String storageArea;

    @Column(name="rack_column_bin_number")
    private String rackColumnBinNumber;

    // FRD: SUB-006 — Allocation Status (Pending / Completed)
    @Column(name="allocation_status")
    private String allocationStatus = "Pending";

    // FRD: SUB-008 — Scan Location verification
    @Column(name="scan_location_verified")
    private Boolean scanLocationVerified = false;

    @PrePersist
    public void addTimestamp() {
        dateTime = new Date();

        // FRD: STI-007/008 — Auto-set recipientStore based on materialAcceptance
        if (materialAcceptance != null) {
            if ("Rejected".equalsIgnoreCase(materialAcceptance)) {
                recipientStore = "Rejection";
            } else {
                recipientStore = "Warehouse";
            }
        }
    }
}

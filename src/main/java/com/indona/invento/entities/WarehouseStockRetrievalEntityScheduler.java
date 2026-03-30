package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "warehouse_stock_retrieval_scheduler")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockRetrievalEntityScheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bundle and Stock Summary IDs for exact identification
    private Long sourceBundleId;
    private Long sourceStockSummaryId;

    // Location fields
    private String store;
    private String storageArea;
    private String rackColumnBin;

    // Retrieval quantities
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;

    // Batch info
    private String batchNumber;
    private String dateOfInward;

    // Dimension
    private String dimension;

    // QR codes
    @Column(columnDefinition = "TEXT")
    private String qrCodeRM;
    private String scanQrCode;

    // Retrieved quantities
    private BigDecimal retrievedQuantityKg;
    private Integer retrievedQuantityNo;

    // Weighed quantities
    private BigDecimal weighedQuantityKg;
    private Integer weighedQuantityNo;

    // Generate & Print QR (FG)
    @Column(columnDefinition = "TEXT")
    private String generatePrintQrFG;

    // Returnable quantities
    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;

    @ManyToOne
    @JoinColumn(name = "stock_transfer_id")
    @JsonBackReference
    private WarehouseStockTransferEntityScheduler stockTransfer;
}

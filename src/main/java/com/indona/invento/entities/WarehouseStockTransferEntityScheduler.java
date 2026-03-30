package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "warehouse_stock_transfer_scheduler")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockTransferEntityScheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String soNumber;
    private String lineNumber;
    private String nextProcess;

    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;

    private BigDecimal weighmentQuantityKg;
    private Integer weighmentQuantityNo;

    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;

    private BigDecimal scrapQuantityKg;
    private Integer scrapQuantityNo;

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String unit;

    private Boolean generateQr;
    @Lob
    @Column(name = "qr_code", columnDefinition = "TEXT")
    private String qrCode;

    // Store multiple retrieval entries as JSON (like picklist selectedBundlesJson)
    // Each entry contains: store, storageArea, rackColumnBin, retrievalQuantityKg, retrievalQuantityNo,
    // batchNumber, dateOfInward, qrCodeRM, scanQrCode, retrievedQuantityKg, retrievedQuantityNo,
    // weighedQuantityKg, weighedQuantityNo, generatePrintQrFG, returnableQuantityKg, returnableQuantityNo
    @Column(columnDefinition = "TEXT")
    private String retrievalEntriesJson;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<WarehouseStockRetrievalEntityScheduler> retrievalEntries;
}

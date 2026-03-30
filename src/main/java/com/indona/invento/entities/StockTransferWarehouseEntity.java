package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfer_warehouse")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferWarehouseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mrNumber;
    private String lineNumber;

    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
    private BigDecimal requiredQuantity;

    private BigDecimal weighmentQuantityKg;
    private Integer weighmentQuantityNo;

    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String unit;
    private String nextProcess;

    private Boolean generateQr;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "generated_qr_image", columnDefinition = "VARCHAR(MAX)")
    private String generatedQrImage;


    @OneToMany(mappedBy = "warehouse", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<StockTransferWHRetrievalQtyEntry> retrievalQtyEntries;

    public void addRetrievalEntry(StockTransferWHRetrievalQtyEntry entry) {
        if (retrievalQtyEntries == null) {
            retrievalQtyEntries = new ArrayList<>();
        }
        retrievalQtyEntries.add(entry);
        entry.setWarehouse(this);
    }

    public void removeRetrievalEntry(StockTransferWHRetrievalQtyEntry entry) {
        if (retrievalQtyEntries != null) {
            retrievalQtyEntries.remove(entry);
        }
        entry.setWarehouse(null);
    }

}
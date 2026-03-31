package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "warehouse_stock_transfer")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockTransferEntity {
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

    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String unit;

    private Boolean generateQr;

    @OneToMany(mappedBy = "stockTransfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<WarehouseStockRetrievalEntity> retrievalEntries;
}


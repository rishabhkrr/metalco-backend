package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "warehouse_stock_retrieval")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WarehouseStockRetrievalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_transfer_id")
    @JsonBackReference
    private WarehouseStockTransferEntity stockTransfer;
}


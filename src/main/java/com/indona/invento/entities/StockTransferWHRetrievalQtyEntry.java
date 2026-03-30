package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "stock_transfer_wh_retrieval_qty_entry")
public class StockTransferWHRetrievalQtyEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "warehouse_id")
    @JsonBackReference
    private StockTransferWarehouseEntity warehouse;
}

package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockSummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unit;
    private String store;
    private String storageArea;
    private String rackColumnShelfNumber;

    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal quantityKg;
    private Integer quantityNo;
    private BigDecimal itemPrice;
    private String materialType;

    // 🆕 End Piece dimensions
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal thickness;
    private String batchNumber;

    // Item Group: SFG (Semi Finished Goods) for CUT orders, FG (Finished Goods) for FULL orders
    private String itemGroup;

    private Boolean reprintQr;
    private String sectionNo;
    private String qrCode;  // QR Code URL/path

    @Column(name = "pick_list_locked")
    private Boolean pickListLocked = false;

    @Column(name = "grn_numbers", columnDefinition = "TEXT")
    private String grnNumbers; // JSON array stored as TEXT (e.g., ["GRN-001", "GRN-002"])

    // OneToMany relationship with StockSummaryBundleEntity for complete GRN data
    @OneToMany(mappedBy = "stockSummary", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StockSummaryBundleEntity> bundles = new ArrayList<>();

}

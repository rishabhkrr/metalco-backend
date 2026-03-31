package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "pick_list_scheduler")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PickListEntityScheduler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unit;
    private String soNumber;
    private String lineNumber;
    private String nextProcess;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
    private String storageArea;
    private String rackColumnShelfNumber;
    private String store;

    // New fields for bundle-based picklist
    private String storageType;
    private String selectionReason;
    private BigDecimal totalSelectedQuantityKg;
    private Integer totalBundlesSelected;

    @Column(columnDefinition = "TEXT")
    private String selectedBundlesJson;
}

package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "raw_material_qr")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RawMaterialQrEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "raw_material_qr_id", unique = true)
    private String rawMaterialQrId;

    private String slNo;
    private String grnNumber;
    private String itemDescription;
    private String productCategory;
    private String sectionNumber;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String store;
    private String storageArea;
    private String rackNo;
    private String columnNo;
    private String binNo;
    private String rackColumnBin;
    private BigDecimal quantityKg;
    private Integer quantityNo;
    private String batchNumber;

    /** Unit code for QR format compliance */
    private String unitCode;

    /** Item code from Item Master for QR format */
    private String itemCode;

    /** Current location status: IN_STOCK, IN_TRANSIT, CONSUMED, SCRAPPED */
    @Builder.Default
    private String locationStatus = "IN_STOCK";

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}

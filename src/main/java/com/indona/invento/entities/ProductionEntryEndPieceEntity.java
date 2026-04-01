package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "production_entry_end_piece")
public class ProductionEntryEndPieceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String endPieceDimension;

    private BigDecimal calculatedWeightKg;
    private BigDecimal endPieceQuantityKg;
    private BigDecimal endPieceQuantityNo;
    private String endPieceType;
    private Boolean qrGenerate;

    @Column(name = "generated_qr_image", columnDefinition = "VARCHAR(MAX)")
    private String qrCode;

    // New fields
    private String parentDimension;
    private String batchNumber;
    private BigDecimal width;
    private BigDecimal thickness;
    private BigDecimal length;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "production_entry_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ProductionEntryEntity productionEntry;
}

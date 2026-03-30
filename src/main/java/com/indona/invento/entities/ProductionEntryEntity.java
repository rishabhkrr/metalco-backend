package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "production_entry")
public class ProductionEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String soNumber;
    private String lineNumber;
    private String machineName;
    private String unit;
    private String customerCode;
    private String customerName;
    private Boolean packing;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;
    private BigDecimal calculatedWeight;
    private BigDecimal producedQtyKg;
    private Integer producedQtyNo;

    @Column(name = "generated_qr_image", columnDefinition = "VARCHAR(MAX)")
    private String generatedQr;

    private Boolean machineBreakDown;
    private Double targetBladeSpeed;
    private Double targetFeed;

    private String uomKg;
    private String uomNo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDispatchDate;

    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Kolkata")
    private Instant timestamp;

    private String startTime;

    private String endTime;

    private Double totalMetresCut;
    private String nextProductionProcess;


    @JsonManagedReference
    @OneToMany(mappedBy = "productionEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<ProductionEntryEndPieceEntity> endPieces = new ArrayList<>();

    // for end pieces total to be computed at runtime
    @Transient
    public BigDecimal getEndPiecesQtyNo() {
        return endPieces.stream()
                .map(ProductionEntryEndPieceEntity::getEndPieceQuantityNo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // helper to keep bidirectional relation consistent
    public void addEndPiece(ProductionEntryEndPieceEntity piece) {
        piece.setProductionEntry(this);
        this.endPieces.add(piece);
    }

    public void removeEndPiece(ProductionEntryEndPieceEntity piece) {
        piece.setProductionEntry(null);
        this.endPieces.remove(piece);
    }

    @JsonManagedReference
    @OneToMany(mappedBy = "productionEntry", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Fetch(org.hibernate.annotations.FetchMode.SUBSELECT)
    private List<ProductionIdleTimeEntryEntity> idleEntries = new ArrayList<>();

    // helper to keep bidirectional relation consistent
    public void addIdleEntry(ProductionIdleTimeEntryEntity idle) {
        idle.setProductionEntry(this);
        this.idleEntries.add(idle);
    }

    public void removeIdleEntry(ProductionIdleTimeEntryEntity idle) {
        idle.setProductionEntry(null);
        this.idleEntries.remove(idle);
    }

    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }
}

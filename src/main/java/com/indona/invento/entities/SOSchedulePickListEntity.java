package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name ="so_schedule_pick_list")
public class SOSchedulePickListEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String deliveryAddress;
    private String grade;
    private String itemDescription;
    private String itemStatus;
    private String lineNumber;
    private String materialType;
    private String mrNumber;
    private String nextProcess;
    private String productCategory;
    private String requestingUnit;
    private Integer requiredQuantity;
    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;
    private String status;
    private String storageArea;
    private String temper;
    private String requestingUnitUnitCode;

    private Instant timestamp;

    private String unitCode;
    private String unitName;
    private String uom;

    private BigDecimal weighmentQuantityKg;
    private Integer weighmentQuantityNo;

    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;

    private Boolean generateQr;

    @Column(columnDefinition = "TEXT")
    private String qrCode;

    @Column(name = "generated_qr_image", columnDefinition = "VARCHAR(MAX)")
    private String generatedQrImage;

    @PrePersist
    protected void onCreate() {
        this.timestamp = (Instant.now());
    }
}
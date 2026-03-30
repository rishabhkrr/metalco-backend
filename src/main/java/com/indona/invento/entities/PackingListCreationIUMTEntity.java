package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.jmx.support.MetricType.COUNTER;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name= "packing_list_creation_iumt")
public class PackingListCreationIUMTEntity {

    private static final DateTimeFormatter PL_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
    private static final AtomicInteger COUNTER = new AtomicInteger(0);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    private String packingListNo;
    private String mrNumber;
    private String lineNumber;
    private String nextProcess;
    private String unit;
    private String unitName;
    private Long slNo;
    private String customerCode;
    private String customerName;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private String materialType;
    private String requestingUnitUnitCode;
    private String requestingUnit;

    private BigDecimal requiredQuantityKg;
    private Integer requiredQuantityNo;

    private BigDecimal weighmentQuantityKg;
    private Integer weighmentQuantityNo;

    private BigDecimal retrievalQuantityKg;
    private Integer retrievalQuantityNo;

    private BigDecimal returnableQuantityKg;
    private Integer returnableQuantityNo;

    private String uomKg;
    private String uomNo;

    private Boolean packing;
    private String targetDateOfDispatch;
    private String planDate;
    private String returnStore;

    private String packingType;
    private String packingStatus;
    private String billingAddress;


    @PrePersist
    protected void onCreate() {
        this.timestamp = Instant.now();
    }
}

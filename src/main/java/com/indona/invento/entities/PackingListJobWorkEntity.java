package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Entity
@Table(name = "packing_list_jobwork")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingListJobWorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "dd-MMM-yyyy hh:mm a", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;

    @Column(name = "packing_list_number", nullable = false)
    private String packingListNumber;

    private String soNumber;
    private String lineNumber;
    private String unit;
    private String customerCode;
    private String customerName;
    private String packingStatus;
    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal quantityKg;
    private String uomKg;

    private Integer quantityNo;
    private String uomNo;
}

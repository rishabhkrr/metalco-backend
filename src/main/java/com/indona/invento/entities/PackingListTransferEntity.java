package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "packing_list_transfer",
       uniqueConstraints = @UniqueConstraint(columnNames = {"soNumber", "lineNumber"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingListTransferEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime timestamp;

    // ✅ REMOVED @Column(unique = true) - allow same MEPC for multiple entries
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
    private String transferStatus;
}

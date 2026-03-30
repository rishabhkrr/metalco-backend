package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoUpdateItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderType;
    private String productCategory;
    private String itemDescription;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;
    private Integer quantityKg;
    private String uomKg;
    private Integer quantityNos;
    private String uomNos;
    private String orderMode;
    private String productionStrategy;
    private Double currentPrice;
    private LocalDate targetDispatchDate;
    private Integer creditPeriodDays;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "so_update_id")
    @JsonBackReference
    private SoUpdate soUpdate;
}

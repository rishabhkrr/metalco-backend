package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "grn_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GRNItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemDescription;
    private String sectionNumber;
    private String grade;
    private String temper;
    private Double poQuantityKg;
    private String uom;

    private Double rate;
    private Double value;

    private Double receivedGrossWeight;
    private Double receivedNetWeight;
    private Integer receivedNo;
    private String heatNumber;
    private String lotNumber;

    private String testCertificateNumber;
    private String productCategory;
    private String brand;
    private String poNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id")
    private GRNEntity grn;
}

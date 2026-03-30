package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Entity
@Table(name = "po_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class POProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sectionNo;
    private String itemDescription;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private Double requiredQuantity;
    private String uom;

    private String selected;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "po_request_id")
    @JsonBackReference
    private PORequestEntity poRequest;
}

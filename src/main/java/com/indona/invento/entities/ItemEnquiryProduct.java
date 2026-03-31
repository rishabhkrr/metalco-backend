package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemEnquiryProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private ItemEnquiry enquiry;

    private Integer productSelectedId;
    private String category;
    private String description;

    private double thickness;
    private double width;
    private double length;

    private String brand;
    private String grade;
    private String temper;
    private String materialType;

    private String orderType;
    private double quantity;
    private String uom;
    private double price;
    private Integer quantityInNo;

    private String dimension;
    // Getters & Setters

    private String requiredCategory;
    @Column(nullable = true)

    private Double requiredThickness;
    @Column(nullable = true)

    private Double requiredWidth;
    @Column(nullable = true)
    private Double requiredLength;
    private String requiredBrand;
    private String requiredGrade;
    private String requiredTemper;
    @Column(nullable = true)

    private Double requiredQuantity;
    private String requiredUom;

    private Double currentSellingPrice;

}

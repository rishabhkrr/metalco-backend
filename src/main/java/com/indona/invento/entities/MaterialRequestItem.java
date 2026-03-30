package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "material_request_item")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequestItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemDescription;
    private String materialType;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private Integer requiredQuantity;
    private String uom;
    private String status; // ✅ default "PENDING"
    private String lineNumber;

    @ManyToOne
    @JoinColumn(name = "header_id")
    @JsonIgnore
    private MaterialRequestHeader materialRequestHeader;
}

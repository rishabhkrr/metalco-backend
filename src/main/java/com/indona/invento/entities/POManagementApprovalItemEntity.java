package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class POManagementApprovalItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String prNumber;
    private String prCreatedBy;
    private String unit;
    private String deliveryAddress;
    private String sectionNo;
    private String itemDescription;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private Double requiredQuantity;
    private String uom;
    private String prTypeAndReasonVerifiaction;
    private String soLineNumber;
    private String orderType;


    @ManyToOne
    @JoinColumn(name = "approval_id")
    @JsonBackReference
    private POManagementApprovalEntity approval;
}


package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class POManagementApprovalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String poNumber;
    private String unit;
    private LocalDate orderDate;
    private String productCategory;
    private Double quantity;
    private String supplier;
    private Integer supplierLeadTime;
    private Integer supplierMOQ;
    private String poGeneratedBy;
    private String billingAddress;
    private String shippingAddress;
    private String remarks;
    private String status;

    @OneToMany(mappedBy = "approval", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<POManagementApprovalItemEntity> items = new ArrayList<>();
}


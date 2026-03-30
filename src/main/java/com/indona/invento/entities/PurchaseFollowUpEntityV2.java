package com.indona.invento.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "purchase_follow_up_v2")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseFollowUpEntityV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String unit;
    private String poNumber;
    private String poGeneratedBy;
    private Date orderDate;
    private Double poQuantityKg;
    private String supplier;
    private String billingAddress;
    private String shippingAddress;
    private String prNumber;
    private String prCreatedBy;
    private String prType;
    private String sectionNo;
    private String itemDescription;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;

    private String salesOrderNumber;
    private String lineItemNumber;

    private String lineItemNumber2;           // Already present
    private LocalDate targetDispatchDate;    // NEW
    private Integer requiredQuantity;        // NEW
    private String uom;// NEW

    private String followUpStatus;
}

package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_challan_jw")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryChallanJWEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(pattern = "dd-MMM-yyyy hh:mm a", timezone = "Asia/Kolkata")
    private LocalDateTime timestamp;

    @Column(name = "medc_number", unique = true)
    private String medcNumber;

    private String packingListNumber;
    private String soNumber;
    private String lineNumber;
    private String unit;

    private String subContractorCode;
    private String subContractorName;
    @Column(name = "is_primary")
    private Boolean Primary;
    private String subContractorShippingAddress;
    private String subContractorBillingAddress;

    private String itemDescription;
    private String packingStatus;
    private String orderType;
    private String productCategory;
    private String brand;
    private String grade;
    private String temper;
    private String dimension;

    private BigDecimal quantityKg;
    private String uomKg;

    private Integer quantityNo;
    private String uomNo;

    private BigDecimal itemPrice;
    private Double amount;
    private Double totalAmount;

    private String vehicleNumberPackingAndDispatch;
    
    private String status;               
    private String ewayBillNumber;
    
    private String dispatchThrough;
    private String otherReference;
    private String remarks;
    private String termsOfDelivery;
    private String destination;
}
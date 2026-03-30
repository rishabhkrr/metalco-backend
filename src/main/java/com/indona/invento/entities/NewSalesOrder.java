package com.indona.invento.entities;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewSalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String soNumber;
    private String quotationNo;
    private String userId;
    private String unit;
    private String customerPoNo;
    private String customerPoFile;
    private String customerCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String billingAddress;
    private String shippingAddress;
    private Boolean sameAsBillingAddress;
    private String marketingExecutiveName;
    private String managementAuthority;
    private boolean packingRequired;

    private String creditPeriod;
    private String remark;
    private boolean acknowledgementSent;
    private boolean approvalLinkSent;
    private String status;
    private String pdfLink;
    private LocalDate targetDateOfDispatch;


    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SalesOrderItem> items;


    @OneToOne(mappedBy = "newSalesOrder", cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private NewPackingInstruction packingInstruction;


    private LocalDateTime createdAt;

//    @OneToOne(mappedBy = "newSalesOrder", cascade = CascadeType.ALL)
//    @EqualsAndHashCode.Exclude
//    @ToString.Exclude
//    private PackingInstruction packingInstruction;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.soNumber = "NSO" + System.currentTimeMillis();
    }
}

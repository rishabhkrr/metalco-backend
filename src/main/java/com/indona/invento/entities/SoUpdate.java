package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SoUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String soNumber;
    private String quotationNo;
    private String userId;
    private String unit;
    private String customerPoNo;
    private String customerCode;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String billingAddress;
    private String shippingAddress;
    private String marketingExecutiveName;
    private String managementAuthority;
    private boolean packingRequired;
    private LocalDateTime createdAt;
    private String creditPeriod;
    private String status; // default: "Pending"
    private String remark;
    private String pdfLink;


    @OneToMany(mappedBy = "soUpdate", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SoUpdateItem> items;


    @OneToOne(mappedBy = "soUpdate", cascade = CascadeType.ALL)
    private SoUpdatePackingInstruction packingInstruction;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}

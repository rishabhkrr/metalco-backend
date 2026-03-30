package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "po_request")
public class PORequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Date timeStamp;

    @Column(nullable = false, unique = true, updatable = false)
    private String prNumber;

    private String orderType;
    private String supplierCode;
    private String supplierName;
    private String unit;
    private String unitCode;
    private String soNumberLineNumber;
    private String prCreatedBy;
    private String reasonForRequest;
    private String status;


    @OneToMany(mappedBy = "poRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<POProductEntity> products;



    // Getters and setters
}

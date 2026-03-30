package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "address_details")
public class AddressDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String branchName;
    private String address;
    private String supplierArea;
    private String state;
    private String country;
    private String pincode;
    private String mapLocation;
    private boolean isPrimary;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    @JsonIgnore
    private SupplierMasterEntity supplier;
}


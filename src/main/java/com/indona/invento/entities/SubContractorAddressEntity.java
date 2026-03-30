package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sub_contractor_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorAddressEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_primary")
    private Boolean Primary;
    private String branchName;
    private String address;
    private String supplierArea;
    private String state;
    private String country;
    private String pincode;
    private String mapLocation; // can be URL or coordinates

    @ManyToOne
    @JoinColumn(name = "sub_contractor_id")
    @JsonIgnore
    private SubContractorMasterEntity subContractor;
}


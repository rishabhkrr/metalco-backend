package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unit_master_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class unitAddressEntity {

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
    @JoinColumn(name = "unit_id")
    @JsonIgnore
    private UnitMasterEntity unit;
}

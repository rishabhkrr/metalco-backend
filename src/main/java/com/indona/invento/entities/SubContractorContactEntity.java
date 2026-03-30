package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sub_contractor_contact")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "is_primary")
    private Boolean Primary;
    private String name;
    private String designation;
    private String phoneNumber;
    private String emailId;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "sub_contractor_id")
    private SubContractorMasterEntity subContractor;
}


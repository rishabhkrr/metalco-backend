package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sub_contractor_bank")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorBankEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String beneficiaryName;
    private String accountNumber;
    private String bankName;
    private String branchAddress;
    private String accountType;
    private String ifscCode;
    private String micrCode;
    private String swiftCode;
    private String bankCountry;
    private String upiId;
    @Column(name = "is_primary")
    private Boolean Primary;

    @ManyToOne
    @JoinColumn(name = "sub_contractor_id")
    @JsonIgnore
    private SubContractorMasterEntity subContractor;
}


package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unit_bank_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitBankDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "is_primary")
    private Boolean Primary;
    private String beneficiaryName;
    private String accountNumber;
    private String bankName;
    private String branchAddress;
    private String accountType;         // Dropdown & select (e.g. Current, Saving)
    private String ifscCode;

    private String micrCode;            // Optional
    private String swiftCode;           // Optional
    private String bankCountry;         // Dropdown & Select
    private String upiId;               // Optional

    @ManyToOne
    @JoinColumn(name = "unit_id")
    @JsonIgnore
    private UnitMasterEntity unit;
}

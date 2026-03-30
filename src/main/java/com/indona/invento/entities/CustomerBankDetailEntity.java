package com.indona.invento.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_bank_detail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBankDetailEntity {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private CustomerMasterEntity customer;


}


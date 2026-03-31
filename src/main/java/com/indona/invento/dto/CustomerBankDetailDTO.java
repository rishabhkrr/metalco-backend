package com.indona.invento.dto;

import jakarta.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerBankDetailDTO {

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
}

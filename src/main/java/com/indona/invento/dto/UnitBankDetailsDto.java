package com.indona.invento.dto;

import jakarta.persistence.Column;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitBankDetailsDto {

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
    private Boolean Primary;
}

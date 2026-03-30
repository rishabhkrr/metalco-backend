package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorBankDto {
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
    @JsonProperty("primary")
    private Boolean Primary;
}


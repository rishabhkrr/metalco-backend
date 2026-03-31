package com.indona.invento.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorDropdownResponseDto {
    private String subContractorCode;
    private String subContractorName;
    private Boolean primary;
}
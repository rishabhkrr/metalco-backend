package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubContractorContactDto {
    private Boolean Primary;
    private String name;
    private String designation;
    private String phoneNumber;
    private String emailId;
}

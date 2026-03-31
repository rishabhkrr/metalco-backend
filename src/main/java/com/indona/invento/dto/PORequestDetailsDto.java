package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PORequestDetailsDto {
    private String prNumber;
    private String createdBy;
    private String reasonForRequest;
    private String userDepartment;
}


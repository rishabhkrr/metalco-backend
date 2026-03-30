package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesmanMasterDto {

    private String unitCode;
    private String unitName;
    private String userId;
    private String userName;
    private String department;
    private String designation;
    private LocalDate dateOfJoining;
    private LocalDate dateOfEnding;
    private String userIdStatus;
    private String modulesWithAccess;

    private List<SalesmanIncentiveDto> incentiveRates;
}

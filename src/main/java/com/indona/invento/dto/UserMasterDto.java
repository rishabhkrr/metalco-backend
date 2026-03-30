package com.indona.invento.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Struct;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMasterDto {
    private String unitCode;
    private String userName;
    private String unitName;
    private String password;
    private String department;
    private String designation;
    private String modulesWithAccess;
    private String status;
    private LocalDate dateOfJoining;
    private LocalDate dateOfEnding;
    private List<SubModuleAccessDto> subModulesWithAccess;  //
}

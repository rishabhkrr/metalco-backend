package com.indona.invento.dto;

import com.indona.invento.entities.SubModuleAccessEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {
    private String token;
    private Long userId;
    private String userName;
    private String userEmail;
    private String unitCode;
    private String unitName;
    private String department;
    private String designation;
    private String status;
    private String userCode;
    private List<SubModuleAccessEntity> modulePermissions;  // Complete permission matrix
}

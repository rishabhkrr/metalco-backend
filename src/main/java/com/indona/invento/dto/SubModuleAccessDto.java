package com.indona.invento.dto;

import lombok.Data;

@Data
public class SubModuleAccessDto {
    private String subModuleName;
    private boolean readAccess;     // Read/View permission
    private boolean createAccess;   // Create/Add permission
    private boolean editAccess;     // Edit/Update permission
    private boolean deleteAccess;// Delete permission

    private boolean approveAccess;  // Approve permission
}

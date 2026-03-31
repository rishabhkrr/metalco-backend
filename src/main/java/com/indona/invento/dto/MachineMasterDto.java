package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineMasterDto {
    private String unitCode;
    private String unitName;
    private String machineId;
    private String machineName;
    private String machineType;
    private String modelNumber;
    private String manufacturer;
    private String machineSpecifications;

    private CuttingMachineDto cuttingConfig;
    private LaminationMachineDto laminationConfig;
}


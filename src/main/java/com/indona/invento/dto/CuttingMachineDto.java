package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuttingMachineDto {
    private Integer idealBladeSpeed;
    private Integer idealCuttingFeed;
    private Integer maxCuttingThickness;
    private Integer maxCuttingLength;
    private Integer minCuttingThickness;
    private Integer minCuttingLength;
}

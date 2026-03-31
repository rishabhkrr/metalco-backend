package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LaminationMachineDto {
    private Integer idealRollerSpeed;
    private Integer conveyorFeed;
    private Integer maxSheetThickness;
    private Integer minSheetThickness;
}


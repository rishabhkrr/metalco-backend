package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackingInstructionDTO {
    private String typeOfPacking;
    private String weightInstructions;
    private String additionalRemarks;
}

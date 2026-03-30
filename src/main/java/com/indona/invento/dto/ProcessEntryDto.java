package com.indona.invento.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessEntryDto {
    private String processType;
    private String operationType;
    private String packingType;
    private String packingStyle;
    private String mode;

    private String additionalProcesses;
}

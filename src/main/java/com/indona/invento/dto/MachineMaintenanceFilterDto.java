package com.indona.invento.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineMaintenanceFilterDto {

    private Long id;

    private String machineName;
    private String startTime;
    private String endTime;
    private Long breakdownMinutes;
    private String breakdownReason;
    private String reason;
    private String remarks;
    private String status;
}

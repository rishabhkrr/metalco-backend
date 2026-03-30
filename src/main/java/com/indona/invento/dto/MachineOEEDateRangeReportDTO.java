package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineOEEDateRangeReportDTO {
    private MachineOEEMonthlyReportDTO overall;
    private List<MachineOEEMonthlyWithDailyDTO> monthlyData;
}

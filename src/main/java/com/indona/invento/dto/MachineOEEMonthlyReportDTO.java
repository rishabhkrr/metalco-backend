package com.indona.invento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineOEEMonthlyReportDTO {

    private String month; // e.g., "November 2025"
    private Integer year;
    private Integer monthNumber;
    private String unit;
    private String machineName;

    // Production metrics (summed for the month)
    private BigDecimal machineProductionMetres;
    private BigDecimal machineProductionKg;
    private BigDecimal machineProductionNo;

    // Average speeds (averaged for the month)
    private Double averageBladeSpeed;
    private Double averageCuttingFeed;

    // Capacity and performance
    private Double machineCapacityMetres; // idealBladeSpeed * 9 hours * days in month
    private Double performancePercent; // (machineProductionMetres / machineCapacityMetres) * 100

    // Time metrics (all in hours, summed for the month)
    private Double totalAvailableTimeHrs; // 9 hours * days in month
    private Double machinePlannedTimeHrs; // sum of all daily planned times
    private Double machineRunTimeHrs; // machinePlannedTimeHrs - idleTimeHrs
    private Double idleTimeHrs; // sum of all idle times
    private Double breakdownTimeHrs; // 0 (as specified)
    private Double plannedDowntimeHrs; // 0 (as specified)

    // Availability
    private Double availabilityPercent; // (machineRunTimeHrs / machinePlannedTimeHrs) * 100

    // Scrap and wastage
    private BigDecimal scrapQuantityKg;
    private Double wastagePercent; // (scrapQuantityKg / machineProductionKg) * 100

    // OEE
    private Double oeePercent; // (performancePercent/100) * (availabilityPercent/100) * (1 - wastagePercent/100) * 100

    // Daily breakdown (optional, populated when user expands)
    private List<MachineOEEDailyReportDTO> dailyData;
}


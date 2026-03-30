package com.indona.invento.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MachineOEEDailyReportDTO {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String unit;
    private String machineName;

    // Production metrics
    private BigDecimal machineProductionMetres;
    private BigDecimal machineProductionKg;
    private BigDecimal machineProductionNo;

    // Average speeds
    private Double averageBladeSpeed;
    private Double averageCuttingFeed;

    // Capacity and performance
    private Double machineCapacityMetres; // idealBladeSpeed * 9 hours
    private Double performancePercent; // (machineProductionMetres / machineCapacityMetres) * 100

    // Time metrics (all in hours)
    private Double totalAvailableTimeHrs; // constant 9 hours per day
    private Double machinePlannedTimeHrs; // sum of (endTime - startTime) for all production runs
    private Double machineRunTimeHrs; // machinePlannedTimeHrs - idleTimeHrs
    private Double idleTimeHrs; // from idle time summary (converted from minutes to hours)
    private Double breakdownTimeHrs; // 0 (as specified)
    private Double plannedDowntimeHrs; // 0 (as specified)

    // Availability
    private Double availabilityPercent; // (machineRunTimeHrs / machinePlannedTimeHrs) * 100

    // Scrap and wastage
    private BigDecimal scrapQuantityKg;
    private Double wastagePercent; // (scrapQuantityKg / machineProductionKg) * 100

    // OEE
    private Double oeePercent; // (performancePercent/100) * (availabilityPercent/100) * (1 - wastagePercent/100) * 100
}


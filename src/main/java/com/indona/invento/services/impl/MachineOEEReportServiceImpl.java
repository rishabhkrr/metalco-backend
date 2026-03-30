package com.indona.invento.services.impl;

import com.indona.invento.dao.MachineMasterRepository;
import com.indona.invento.dao.ProductionEntryRepository;
import com.indona.invento.dao.ProductionIdleTimeEntryRepository;
import com.indona.invento.dto.MachineOEEDailyReportDTO;
import com.indona.invento.dto.MachineOEEDateRangeReportDTO;
import com.indona.invento.dto.MachineOEEMonthlyReportDTO;
import com.indona.invento.dto.MachineOEEMonthlyWithDailyDTO;
import com.indona.invento.dto.ScrapSummaryDto;
import com.indona.invento.entities.MachineMasterEntity;
import com.indona.invento.entities.ProductionEntryEntity;
import com.indona.invento.entities.ProductionIdleTimeEntryEntity;
import com.indona.invento.services.MachineOEEReportService;
import com.indona.invento.services.ProductionEntryService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MachineOEEReportServiceImpl implements MachineOEEReportService {

    private final MachineMasterRepository machineMasterRepository;
    private final ProductionEntryRepository productionEntryRepository;
    private final ProductionIdleTimeEntryRepository idleTimeEntryRepository;
    private final ProductionEntryService productionEntryService;

    private static final double AVAILABLE_HOURS_PER_DAY = 9.0;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    @Override
    public MachineOEEMonthlyReportDTO getMonthlyReport(String unitCode, String machineName, Integer month, Integer year) {
        // Default to current month/year if not provided
        LocalDate now = LocalDate.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();

        YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Don't include future dates - limit to today if in current month
        LocalDate today = LocalDate.now();
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        // Get machine details for ideal blade speed
        MachineMasterEntity machine = machineMasterRepository.findByUnitCode(unitCode).stream()
                .filter(m -> m.getMachineName().equals(machineName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Machine not found: " + machineName + " in unit: " + unitCode));

        Integer idealBladeSpeed = (machine.getCuttingConfig() != null) 
                ? machine.getCuttingConfig().getIdealBladeSpeed() 
                : 0;

        // Get daily reports
        List<MachineOEEDailyReportDTO> dailyReports = getDailyReport(unitCode, machineName, targetMonth, targetYear);

        // Aggregate monthly data
        BigDecimal totalProductionMetres = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getMachineProductionMetres)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProductionKg = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getMachineProductionKg)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProductionNo = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getMachineProductionNo)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average blade speed and cutting feed
        double avgBladeSpeed = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getAverageBladeSpeed)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double avgCuttingFeed = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getAverageCuttingFeed)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Sum time metrics
        double totalPlannedTime = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getMachinePlannedTimeHrs)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        double totalIdleTime = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getIdleTimeHrs)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        double totalRunTime = totalPlannedTime - totalIdleTime;

        // Sum scrap quantity
        BigDecimal totalScrapKg = dailyReports.stream()
                .map(MachineOEEDailyReportDTO::getScrapQuantityKg)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Calculate monthly metrics
        int daysInMonth = yearMonth.lengthOfMonth();
        double totalAvailableTime = AVAILABLE_HOURS_PER_DAY * daysInMonth;
        double machineCapacityMetres = idealBladeSpeed * AVAILABLE_HOURS_PER_DAY * daysInMonth;

        // Performance %
        double performancePercent = (machineCapacityMetres > 0) 
                ? (totalProductionMetres.doubleValue() / machineCapacityMetres) * 100 
                : 0.0;

        // Availability %
        double availabilityPercent = (totalPlannedTime > 0) 
                ? (totalRunTime / totalPlannedTime) * 100 
                : 0.0;

        // Wastage %
        double wastagePercent = (totalProductionKg.compareTo(BigDecimal.ZERO) > 0) 
                ? (totalScrapKg.doubleValue() / totalProductionKg.doubleValue()) * 100 
                : 0.0;

        // OEE %
        double oeePercent = (performancePercent / 100) * (availabilityPercent / 100) * (1 - wastagePercent / 100) * 100;

        return MachineOEEMonthlyReportDTO.builder()
                .month(yearMonth.getMonth().name())
                .year(targetYear)
                .monthNumber(targetMonth)
                .unit(unitCode)
                .machineName(machineName)
                .machineProductionMetres(roundBigDecimal(totalProductionMetres, 2))
                .machineProductionKg(roundBigDecimal(totalProductionKg, 2))
                .machineProductionNo(roundBigDecimal(totalProductionNo, 2))
                .averageBladeSpeed(roundDouble(avgBladeSpeed, 2))
                .averageCuttingFeed(roundDouble(avgCuttingFeed, 2))
                .machineCapacityMetres(roundDouble(machineCapacityMetres, 2))
                .performancePercent(roundDouble(performancePercent, 2))
                .totalAvailableTimeHrs(roundDouble(totalAvailableTime, 2))
                .machinePlannedTimeHrs(roundDouble(totalPlannedTime, 2))
                .machineRunTimeHrs(roundDouble(totalRunTime, 2))
                .idleTimeHrs(roundDouble(totalIdleTime, 2))
                .breakdownTimeHrs(0.0)
                .plannedDowntimeHrs(0.0)
                .availabilityPercent(roundDouble(availabilityPercent, 2))
                .scrapQuantityKg(roundBigDecimal(totalScrapKg, 2))
                .wastagePercent(roundDouble(wastagePercent, 2))
                .oeePercent(roundDouble(oeePercent, 2))
                .build();
    }

    @Override
    public List<MachineOEEDailyReportDTO> getDailyReport(String unitCode, String machineName, Integer month, Integer year) {
        // Default to current month/year if not provided
        LocalDate now = LocalDate.now();
        int targetMonth = (month != null) ? month : now.getMonthValue();
        int targetYear = (year != null) ? year : now.getYear();

        YearMonth yearMonth = YearMonth.of(targetYear, targetMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Don't include future dates - limit to today if in current month
        LocalDate today = LocalDate.now();
        if (endDate.isAfter(today)) {
            endDate = today;
        }

        // Get machine details for ideal blade speed
        MachineMasterEntity machine = machineMasterRepository.findByUnitCode(unitCode).stream()
                .filter(m -> m.getMachineName().equals(machineName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Machine not found: " + machineName + " in unit: " + unitCode));

        Integer idealBladeSpeed = (machine.getCuttingConfig() != null) 
                ? machine.getCuttingConfig().getIdealBladeSpeed() 
                : 0;


        Instant startInstant = startDate.atStartOfDay(ZoneId.of("Asia/Kolkata")).toInstant();
        Instant endInstant = endDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Kolkata")).minusNanos(1).toInstant();

        // Get production entries for the machine and date range
        List<ProductionEntryEntity> productionEntries = productionEntryRepository
                .findByMachineNameAndDateRange(machineName, startInstant, endInstant);

        // Get idle time entries
        List<ProductionIdleTimeEntryEntity> idleTimeEntries = idleTimeEntryRepository
                .findByMachineNameAndDateRange(machineName, startInstant, endInstant);

        // Get scrap summary for date range
        Map<LocalDate, BigDecimal> scrapByDate = getScrapDataForDateRange(machineName, startDate, endDate);

        // Group production entries by date
        Map<LocalDate, List<ProductionEntryEntity>> productionByDate = productionEntries.stream()
                .collect(Collectors.groupingBy(p -> parseDate(p.getStartTime())));

        // Group idle time entries by date
        Map<LocalDate, List<ProductionIdleTimeEntryEntity>> idleByDate = idleTimeEntries.stream()
                .collect(Collectors.groupingBy(i -> parseDate(i.getStartTime())));

        // Build daily reports
        List<MachineOEEDailyReportDTO> dailyReports = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<ProductionEntryEntity> dayProduction = productionByDate.getOrDefault(date, Collections.emptyList());
            List<ProductionIdleTimeEntryEntity> dayIdle = idleByDate.getOrDefault(date, Collections.emptyList());
            BigDecimal dayScrap = scrapByDate.getOrDefault(date, BigDecimal.ZERO);

            MachineOEEDailyReportDTO dailyReport = calculateDailyOEE(
                    date, unitCode, machineName, idealBladeSpeed, dayProduction, dayIdle, dayScrap
            );
            dailyReports.add(dailyReport);
        }

        // Sort by date descending (most recent first)
        dailyReports.sort(Comparator.comparing(MachineOEEDailyReportDTO::getDate).reversed());

        return dailyReports;
    }

    private MachineOEEDailyReportDTO calculateDailyOEE(
            LocalDate date,
            String unitCode,
            String machineName,
            Integer idealBladeSpeed,
            List<ProductionEntryEntity> productionEntries,
            List<ProductionIdleTimeEntryEntity> idleEntries,
            BigDecimal scrapQuantityKg
    ) {
        // Sum production quantities
        BigDecimal productionMetres = productionEntries.stream()
                .map(ProductionEntryEntity::getTotalMetresCut)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal productionKg = productionEntries.stream()
                .map(ProductionEntryEntity::getProducedQtyKg)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal productionNo = productionEntries.stream()
                .map(ProductionEntryEntity::getProducedQtyNo)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average blade speed and cutting feed
        double avgBladeSpeed = productionEntries.stream()
                .map(ProductionEntryEntity::getTargetBladeSpeed)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double avgCuttingFeed = productionEntries.stream()
                .map(ProductionEntryEntity::getTargetFeed)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Calculate planned time (sum of endTime - startTime for all production runs)
        double plannedTimeHrs = productionEntries.stream()
                .mapToDouble(this::calculateDurationInHours)
                .sum();

        // Calculate idle time (sum of idle minutes converted to hours)
        double idleTimeHrs = idleEntries.stream()
                .map(ProductionIdleTimeEntryEntity::getIdleMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum() / 60.0;

        // Run time = Planned time - Idle time
        double runTimeHrs = plannedTimeHrs - idleTimeHrs;
        if (runTimeHrs < 0) runTimeHrs = 0;

        // Machine capacity = ideal blade speed * 9 hours
        double machineCapacityMetres = idealBladeSpeed * AVAILABLE_HOURS_PER_DAY;

        // Performance %
        double performancePercent = (machineCapacityMetres > 0) 
                ? (productionMetres.doubleValue() / machineCapacityMetres) * 100 
                : 0.0;

        // Availability %
        double availabilityPercent = (plannedTimeHrs > 0) 
                ? (runTimeHrs / plannedTimeHrs) * 100 
                : 0.0;

        // Wastage %
        double wastagePercent = (productionKg.compareTo(BigDecimal.ZERO) > 0) 
                ? (scrapQuantityKg.doubleValue() / productionKg.doubleValue()) * 100 
                : 0.0;

        // OEE %
        double oeePercent = (performancePercent / 100) * (availabilityPercent / 100) * (1 - wastagePercent / 100) * 100;

        return MachineOEEDailyReportDTO.builder()
                .date(date)
                .unit(unitCode)
                .machineName(machineName)
                .machineProductionMetres(roundBigDecimal(productionMetres, 2))
                .machineProductionKg(roundBigDecimal(productionKg, 2))
                .machineProductionNo(roundBigDecimal(productionNo, 2))
                .averageBladeSpeed(roundDouble(avgBladeSpeed, 2))
                .averageCuttingFeed(roundDouble(avgCuttingFeed, 2))
                .machineCapacityMetres(roundDouble(machineCapacityMetres, 2))
                .performancePercent(roundDouble(performancePercent, 2))
                .totalAvailableTimeHrs(AVAILABLE_HOURS_PER_DAY)
                .machinePlannedTimeHrs(roundDouble(plannedTimeHrs, 2))
                .machineRunTimeHrs(roundDouble(runTimeHrs, 2))
                .idleTimeHrs(roundDouble(idleTimeHrs, 2))
                .breakdownTimeHrs(0.0)
                .plannedDowntimeHrs(0.0)
                .availabilityPercent(roundDouble(availabilityPercent, 2))
                .scrapQuantityKg(roundBigDecimal(scrapQuantityKg, 2))
                .wastagePercent(roundDouble(wastagePercent, 2))
                .oeePercent(roundDouble(oeePercent, 2))
                .build();
    }

    /**
     * Calculate duration in hours between startTime and endTime
     */
    private double calculateDurationInHours(ProductionEntryEntity entry) {
        try {
            if (entry.getStartTime() == null || entry.getEndTime() == null) {
                return 0.0;
            }

            LocalDateTime startDateTime = LocalDateTime.parse(entry.getStartTime(), DATE_TIME_FORMATTER);
            LocalDateTime endDateTime = LocalDateTime.parse(entry.getEndTime(), DATE_TIME_FORMATTER);

            long minutes = ChronoUnit.MINUTES.between(startDateTime, endDateTime);
            return minutes / 60.0;
        } catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * Parse date from string (handles both date-only and datetime strings)
     */
    private LocalDate parseDate(String dateTimeString) {
        try {
            if (dateTimeString == null || dateTimeString.isBlank()) {
                return LocalDate.now();
            }

            // Try parsing as datetime first
            if (dateTimeString.contains(" ")) {
                LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, DATE_TIME_FORMATTER);
                return dateTime.toLocalDate();
            }

            // Try parsing as ISO date
            return LocalDate.parse(dateTimeString);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    /**
     * Helper method to get scrap data for a date range by calling getScrapSummary for each date
     */
    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");
    private Map<LocalDate, BigDecimal> getScrapDataForDateRange(String machineName, LocalDate startDate, LocalDate endDate) {

        List<ScrapSummaryDto> allScrap = (startDate == null || endDate == null)
                ? productionEntryService.getScrapSummary(null, null)
                : productionEntryService.getScrapSummary(startDate, endDate);

        if (allScrap == null || allScrap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<LocalDate, BigDecimal> scrapByDate = allScrap.stream()
                .filter(s -> s.getTimestamp() != null && machineName.equals(s.getMachineName()))
                .collect(Collectors.groupingBy(
                        s -> s.getTimestamp().atZone(KOLKATA).toLocalDate(),
                        Collectors.mapping(
                                s -> s.getProducedQtyKg() == null ? BigDecimal.ZERO : s.getProducedQtyKg(),
                                Collectors.reducing(BigDecimal.ZERO, BigDecimal::add)
                        )
                ));

        scrapByDate.entrySet().removeIf(e -> e.getValue() == null || e.getValue().compareTo(BigDecimal.ZERO) <= 0);
        return scrapByDate;
    }

    /**
     * Round BigDecimal to specified decimal places
     */
    private BigDecimal roundBigDecimal(BigDecimal value, int scale) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    /**
     * Round double to specified decimal places
     */
    private Double roundDouble(double value, int scale) {
        BigDecimal bd = BigDecimal.valueOf(value);
        return bd.setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public MachineOEEMonthlyReportDTO getOverallReport(String unitCode, String machineName) {
        // Get machine details for ideal blade speed
        MachineMasterEntity machine = machineMasterRepository.findByUnitCode(unitCode).stream()
                .filter(m -> m.getMachineName().equals(machineName))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Machine not found: " + machineName + " in unit: " + unitCode));

        Integer idealBladeSpeed = (machine.getCuttingConfig() != null) 
                ? machine.getCuttingConfig().getIdealBladeSpeed() 
                : 0;

        // Get all production entries for the machine (no date filter)
        List<ProductionEntryEntity> allProductionEntries = productionEntryRepository
                .findByMachineName(machineName);

        // If no production data, return empty report with zeros
        if (allProductionEntries.isEmpty()) {
            return MachineOEEMonthlyReportDTO.builder()
                    .month("ALL TIME")
                    .year(null)
                    .monthNumber(null)
                    .unit(unitCode)
                    .machineName(machineName)
                    .machineProductionMetres(BigDecimal.ZERO)
                    .machineProductionKg(BigDecimal.ZERO)
                    .machineProductionNo(BigDecimal.ZERO)
                    .averageBladeSpeed(0.0)
                    .averageCuttingFeed(0.0)
                    .machineCapacityMetres(0.0)
                    .performancePercent(0.0)
                    .totalAvailableTimeHrs(0.0)
                    .machinePlannedTimeHrs(0.0)
                    .machineRunTimeHrs(0.0)
                    .idleTimeHrs(0.0)
                    .breakdownTimeHrs(0.0)
                    .plannedDowntimeHrs(0.0)
                    .availabilityPercent(0.0)
                    .scrapQuantityKg(BigDecimal.ZERO)
                    .wastagePercent(0.0)
                    .oeePercent(0.0)
                    .build();
        }

        // Get all idle time entries
        List<ProductionIdleTimeEntryEntity> allIdleTimeEntries = idleTimeEntryRepository
                .findByMachineName(machineName);

        // Get earliest and latest dates
        LocalDate earliestDate = allProductionEntries.stream()
                .map(p -> parseDate(p.getStartTime()))
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now());

        LocalDate latestDate = LocalDate.now();

        // Get scrap summary for all time
        Map<LocalDate, BigDecimal> scrapByDate = getScrapDataForDateRange(machineName, earliestDate, latestDate);
        BigDecimal totalScrapKg = scrapByDate.values().stream()
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum production quantities
        BigDecimal totalProductionMetres = allProductionEntries.stream()
                .map(ProductionEntryEntity::getTotalMetresCut)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProductionKg = allProductionEntries.stream()
                .map(ProductionEntryEntity::getProducedQtyKg)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProductionNo = allProductionEntries.stream()
                .map(ProductionEntryEntity::getProducedQtyNo)
                .filter(Objects::nonNull)
                .map(BigDecimal::valueOf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Average blade speed and cutting feed
        double avgBladeSpeed = allProductionEntries.stream()
                .map(ProductionEntryEntity::getTargetBladeSpeed)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        double avgCuttingFeed = allProductionEntries.stream()
                .map(ProductionEntryEntity::getTargetFeed)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // Calculate planned time (sum of endTime - startTime for all production runs)
        double totalPlannedTime = allProductionEntries.stream()
                .mapToDouble(this::calculateDurationInHours)
                .sum();

        // Calculate idle time (sum of idle minutes converted to hours)
        double totalIdleTime = allIdleTimeEntries.stream()
                .map(ProductionIdleTimeEntryEntity::getIdleMinutes)
                .filter(Objects::nonNull)
                .mapToInt(Integer::intValue)
                .sum() / 60.0;

        // Run time = Planned time - Idle time
        double totalRunTime = totalPlannedTime - totalIdleTime;
        if (totalRunTime < 0) totalRunTime = 0;

        // Calculate total days between earliest and latest date
        long totalDays = ChronoUnit.DAYS.between(earliestDate, latestDate) + 1;
        double totalAvailableTime = AVAILABLE_HOURS_PER_DAY * totalDays;
        double machineCapacityMetres = idealBladeSpeed * AVAILABLE_HOURS_PER_DAY * totalDays;

        // Performance %
        double performancePercent = (machineCapacityMetres > 0) 
                ? (totalProductionMetres.doubleValue() / machineCapacityMetres) * 100 
                : 0.0;

        // Availability %
        double availabilityPercent = (totalPlannedTime > 0) 
                ? (totalRunTime / totalPlannedTime) * 100 
                : 0.0;

        // Wastage %
        double wastagePercent = (totalProductionKg.compareTo(BigDecimal.ZERO) > 0) 
                ? (totalScrapKg.doubleValue() / totalProductionKg.doubleValue()) * 100 
                : 0.0;

        // OEE %
        double oeePercent = (performancePercent / 100) * (availabilityPercent / 100) * (1 - wastagePercent / 100) * 100;

        return MachineOEEMonthlyReportDTO.builder()
                .month("ALL TIME")
                .year(null)
                .monthNumber(null)
                .unit(unitCode)
                .machineName(machineName)
                .machineProductionMetres(roundBigDecimal(totalProductionMetres, 2))
                .machineProductionKg(roundBigDecimal(totalProductionKg, 2))
                .machineProductionNo(roundBigDecimal(totalProductionNo, 2))
                .averageBladeSpeed(roundDouble(avgBladeSpeed, 2))
                .averageCuttingFeed(roundDouble(avgCuttingFeed, 2))
                .machineCapacityMetres(roundDouble(machineCapacityMetres, 2))
                .performancePercent(roundDouble(performancePercent, 2))
                .totalAvailableTimeHrs(roundDouble(totalAvailableTime, 2))
                .machinePlannedTimeHrs(roundDouble(totalPlannedTime, 2))
                .machineRunTimeHrs(roundDouble(totalRunTime, 2))
                .idleTimeHrs(roundDouble(totalIdleTime, 2))
                .breakdownTimeHrs(0.0)
                .plannedDowntimeHrs(0.0)
                .availabilityPercent(roundDouble(availabilityPercent, 2))
                .scrapQuantityKg(roundBigDecimal(totalScrapKg, 2))
                .wastagePercent(roundDouble(wastagePercent, 2))
                .oeePercent(roundDouble(oeePercent, 2))
                .build();
    }

    @Override
    public ByteArrayResource exportMonthlyToExcel(String unitCode, String machineName, Integer month, Integer year) {
        MachineOEEMonthlyReportDTO report = getMonthlyReport(unitCode, machineName, month, year);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Monthly OEE Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Machine OEE Monthly Report");
            titleCell.setCellStyle(headerStyle);
            
            rowNum++; // Empty row
            
            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "Month", "Year", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
                "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
                "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
                "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data row
            Row dataRow = sheet.createRow(rowNum++);
            int colNum = 0;
            
            createCell(dataRow, colNum++, report.getMonth(), dataStyle);
            createCell(dataRow, colNum++, report.getYear() != null ? report.getYear().toString() : "N/A", dataStyle);
            createCell(dataRow, colNum++, report.getUnit(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineName(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineProductionMetres().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineProductionKg().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineProductionNo().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getAverageBladeSpeed().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getAverageCuttingFeed().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineCapacityMetres().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getPerformancePercent().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getTotalAvailableTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachinePlannedTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineRunTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getIdleTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getAvailabilityPercent().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getScrapQuantityKg().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getWastagePercent().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getOeePercent().toString(), dataStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    @Override
    public ByteArrayResource exportDailyToExcel(String unitCode, String machineName, Integer month, Integer year) {
        List<MachineOEEDailyReportDTO> dailyReports = getDailyReport(unitCode, machineName, month, year);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Daily OEE Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Machine OEE Daily Report");
            titleCell.setCellStyle(headerStyle);
            
            rowNum++; // Empty row
            
            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "Date", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
                "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
                "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
                "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows
            for (MachineOEEDailyReportDTO daily : dailyReports) {
                Row dataRow = sheet.createRow(rowNum++);
                int colNum = 0;
                
                createCell(dataRow, colNum++, daily.getDate().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getUnit(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineName(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineProductionMetres().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineProductionKg().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineProductionNo().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getAverageBladeSpeed().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getAverageCuttingFeed().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineCapacityMetres().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getPerformancePercent().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getTotalAvailableTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachinePlannedTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineRunTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getIdleTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getAvailabilityPercent().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getScrapQuantityKg().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getWastagePercent().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getOeePercent().toString(), dataStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    @Override
    public ByteArrayResource exportOverallToExcel(String unitCode, String machineName) {
        MachineOEEMonthlyReportDTO report = getOverallReport(unitCode, machineName);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Overall OEE Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Machine OEE Overall Report (All Time)");
            titleCell.setCellStyle(headerStyle);
            
            rowNum++; // Empty row
            
            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "Period", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
                "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
                "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
                "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data row
            Row dataRow = sheet.createRow(rowNum++);
            int colNum = 0;
            
            createCell(dataRow, colNum++, report.getMonth(), dataStyle);
            createCell(dataRow, colNum++, report.getUnit(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineName(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineProductionMetres().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineProductionKg().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineProductionNo().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getAverageBladeSpeed().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getAverageCuttingFeed().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineCapacityMetres().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getPerformancePercent().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getTotalAvailableTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachinePlannedTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getMachineRunTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getIdleTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getAvailabilityPercent().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getScrapQuantityKg().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getWastagePercent().toString(), dataStyle);
            createCell(dataRow, colNum++, report.getOeePercent().toString(), dataStyle);
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    @Override
    public MachineOEEDateRangeReportDTO getDateRangeReport(String unitCode, String machineName, LocalDate fromDate, LocalDate toDate) {
        // Get all months in the date range
        List<MachineOEEMonthlyWithDailyDTO> monthlyDataList = new ArrayList<>();
        
        YearMonth startYearMonth = YearMonth.from(fromDate);
        YearMonth endYearMonth = YearMonth.from(toDate);
        
        // Iterate through each month in the range
        YearMonth current = startYearMonth;
        while (!current.isAfter(endYearMonth)) {
            int month = current.getMonthValue();
            int year = current.getYear();
            
            // Get monthly report for this month
            MachineOEEMonthlyReportDTO monthlyReport = getMonthlyReport(unitCode, machineName, month, year);
            
            // Get daily reports for this month (filtered by the date range)
            List<MachineOEEDailyReportDTO> dailyReports = getDailyReport(unitCode, machineName, month, year);
            
            // Filter daily reports to only include dates within the range
            List<MachineOEEDailyReportDTO> filteredDailyReports = dailyReports.stream()
                    .filter(daily -> !daily.getDate().isBefore(fromDate) && !daily.getDate().isAfter(toDate))
                    .collect(Collectors.toList());
            
            // Create monthly with daily DTO
            MachineOEEMonthlyWithDailyDTO monthlyWithDaily = MachineOEEMonthlyWithDailyDTO.builder()
                    .monthData(monthlyReport)
                    .dailyData(filteredDailyReports)
                    .build();
            
            monthlyDataList.add(monthlyWithDaily);
            
            // Move to next month
            current = current.plusMonths(1);
        }
        
        // Sort by most recent first (descending)
        monthlyDataList.sort((a, b) -> {
            int yearCompare = Integer.compare(b.getMonthData().getYear(), a.getMonthData().getYear());
            if (yearCompare != 0) return yearCompare;
            return Integer.compare(b.getMonthData().getMonthNumber(), a.getMonthData().getMonthNumber());
        });
        
        // Calculate overall data for the selected date range only
        MachineOEEMonthlyReportDTO overall = calculateDateRangeOverall(unitCode, machineName, monthlyDataList, fromDate, toDate);
        
        return MachineOEEDateRangeReportDTO.builder()
                .overall(overall)
                .monthlyData(monthlyDataList)
                .build();
    }

    @Override
    public ByteArrayResource exportDateRangeToExcel(String unitCode, String machineName, LocalDate fromDate, LocalDate toDate) {
        MachineOEEDateRangeReportDTO report = getDateRangeReport(unitCode, machineName, fromDate, toDate);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // Sheet 1: Overall Summary
            Sheet overallSheet = workbook.createSheet("Overall Summary");
            createOverallSheet(overallSheet, report.getOverall(), headerStyle, dataStyle);
            
            // Sheet 2: Monthly Summary
            Sheet monthlySheet = workbook.createSheet("Monthly Summary");
            createMonthlySheet(monthlySheet, report.getMonthlyData(), headerStyle, dataStyle);
            
            // Sheet 3: Daily Details
            Sheet dailySheet = workbook.createSheet("Daily Details");
            createDailySheet(dailySheet, report.getMonthlyData(), headerStyle, dataStyle);
            
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private void createOverallSheet(Sheet sheet, MachineOEEMonthlyReportDTO overall, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Overall Summary (All Time)");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        // Column headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "Period", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
            "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
            "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
            "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data row
        Row dataRow = sheet.createRow(rowNum++);
        int colNum = 0;
        
        createCell(dataRow, colNum++, overall.getMonth(), dataStyle);
        createCell(dataRow, colNum++, overall.getUnit(), dataStyle);
        createCell(dataRow, colNum++, overall.getMachineName(), dataStyle);
        createCell(dataRow, colNum++, overall.getMachineProductionMetres().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getMachineProductionKg().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getMachineProductionNo().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getAverageBladeSpeed().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getAverageCuttingFeed().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getMachineCapacityMetres().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getPerformancePercent().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getTotalAvailableTimeHrs().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getMachinePlannedTimeHrs().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getMachineRunTimeHrs().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getIdleTimeHrs().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getAvailabilityPercent().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getScrapQuantityKg().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getWastagePercent().toString(), dataStyle);
        createCell(dataRow, colNum++, overall.getOeePercent().toString(), dataStyle);
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createMonthlySheet(Sheet sheet, List<MachineOEEMonthlyWithDailyDTO> monthlyDataList, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Monthly Summary");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        // Column headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "Month", "Year", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
            "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
            "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
            "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows
        for (MachineOEEMonthlyWithDailyDTO monthlyWithDaily : monthlyDataList) {
            MachineOEEMonthlyReportDTO monthly = monthlyWithDaily.getMonthData();
            Row dataRow = sheet.createRow(rowNum++);
            int colNum = 0;
            
            createCell(dataRow, colNum++, monthly.getMonth(), dataStyle);
            createCell(dataRow, colNum++, monthly.getYear() != null ? monthly.getYear().toString() : "N/A", dataStyle);
            createCell(dataRow, colNum++, monthly.getUnit(), dataStyle);
            createCell(dataRow, colNum++, monthly.getMachineName(), dataStyle);
            createCell(dataRow, colNum++, monthly.getMachineProductionMetres().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getMachineProductionKg().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getMachineProductionNo().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getAverageBladeSpeed().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getAverageCuttingFeed().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getMachineCapacityMetres().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getPerformancePercent().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getTotalAvailableTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getMachinePlannedTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getMachineRunTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getIdleTimeHrs().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getAvailabilityPercent().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getScrapQuantityKg().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getWastagePercent().toString(), dataStyle);
            createCell(dataRow, colNum++, monthly.getOeePercent().toString(), dataStyle);
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createDailySheet(Sheet sheet, List<MachineOEEMonthlyWithDailyDTO> monthlyDataList, CellStyle headerStyle, CellStyle dataStyle) {
        int rowNum = 0;
        
        // Title
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("Daily Details");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Empty row
        
        // Column headers
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {
            "Date", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
            "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
            "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
            "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
        };
        
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Data rows - iterate through all months and their daily data
        for (MachineOEEMonthlyWithDailyDTO monthlyWithDaily : monthlyDataList) {
            for (MachineOEEDailyReportDTO daily : monthlyWithDaily.getDailyData()) {
                Row dataRow = sheet.createRow(rowNum++);
                int colNum = 0;
                
                createCell(dataRow, colNum++, daily.getDate().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getUnit(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineName(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineProductionMetres().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineProductionKg().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineProductionNo().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getAverageBladeSpeed().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getAverageCuttingFeed().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineCapacityMetres().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getPerformancePercent().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getTotalAvailableTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachinePlannedTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getMachineRunTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getIdleTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getAvailabilityPercent().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getScrapQuantityKg().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getWastagePercent().toString(), dataStyle);
                createCell(dataRow, colNum++, daily.getOeePercent().toString(), dataStyle);
            }
        }
        
        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    public ByteArrayResource exportDailyDateRangeToExcel(String unitCode, String machineName, LocalDate fromDate, LocalDate toDate) {
        MachineOEEDateRangeReportDTO report = getDateRangeReport(unitCode, machineName, fromDate, toDate);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Daily OEE Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle monthHeaderStyle = createMonthHeaderStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Machine OEE Daily Report - Date Range");
            titleCell.setCellStyle(headerStyle);
            
            rowNum++; // Empty row
            
            // Column headers
            String[] headers = {
                "Date", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
                "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
                "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
                "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
            };
            
            // Data grouped by month
            for (MachineOEEMonthlyWithDailyDTO monthlyWithDaily : report.getMonthlyData()) {
                // Month header
                Row monthHeaderRow = sheet.createRow(rowNum++);
                Cell monthHeaderCell = monthHeaderRow.createCell(0);
                monthHeaderCell.setCellValue(monthlyWithDaily.getMonthData().getMonth() + " " + 
                    (monthlyWithDaily.getMonthData().getYear() != null ? monthlyWithDaily.getMonthData().getYear() : ""));
                monthHeaderCell.setCellStyle(monthHeaderStyle);
                
                // Column headers for this month
                Row headerRow = sheet.createRow(rowNum++);
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                
                // Daily data for this month
                for (MachineOEEDailyReportDTO daily : monthlyWithDaily.getDailyData()) {
                    Row dataRow = sheet.createRow(rowNum++);
                    int colNum = 0;
                    
                    createCell(dataRow, colNum++, daily.getDate().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getUnit(), dataStyle);
                    createCell(dataRow, colNum++, daily.getMachineName(), dataStyle);
                    createCell(dataRow, colNum++, daily.getMachineProductionMetres().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getMachineProductionKg().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getMachineProductionNo().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getAverageBladeSpeed().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getAverageCuttingFeed().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getMachineCapacityMetres().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getPerformancePercent().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getTotalAvailableTimeHrs().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getMachinePlannedTimeHrs().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getMachineRunTimeHrs().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getIdleTimeHrs().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getAvailabilityPercent().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getScrapQuantityKg().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getWastagePercent().toString(), dataStyle);
                    createCell(dataRow, colNum++, daily.getOeePercent().toString(), dataStyle);
                }
                
                rowNum++; // Empty row between months
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    private CellStyle createMonthHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THICK);
        style.setBorderTop(BorderStyle.THICK);
        style.setBorderLeft(BorderStyle.THICK);
        style.setBorderRight(BorderStyle.THICK);
        return style;
    }

    private MachineOEEMonthlyReportDTO calculateDateRangeOverall(String unitCode, String machineName, 
            List<MachineOEEMonthlyWithDailyDTO> monthlyDataList, LocalDate fromDate, LocalDate toDate) {
        
        if (monthlyDataList.isEmpty()) {
            return MachineOEEMonthlyReportDTO.builder()
                    .month("SELECTED PERIOD")
                    .unit(unitCode)
                    .machineName(machineName)
                    .machineProductionMetres(BigDecimal.ZERO)
                    .machineProductionKg(BigDecimal.ZERO)
                    .machineProductionNo(BigDecimal.ZERO)
                    .averageBladeSpeed(0.0)
                    .averageCuttingFeed(0.0)
                    .machineCapacityMetres(0.0)
                    .performancePercent(0.0)
                    .totalAvailableTimeHrs(0.0)
                    .machinePlannedTimeHrs(0.0)
                    .machineRunTimeHrs(0.0)
                    .idleTimeHrs(0.0)
                    .breakdownTimeHrs(0.0)
                    .plannedDowntimeHrs(0.0)
                    .availabilityPercent(0.0)
                    .scrapQuantityKg(BigDecimal.ZERO)
                    .wastagePercent(0.0)
                    .oeePercent(0.0)
                    .build();
        }
        
        // Aggregate all monthly data
        BigDecimal totalProductionMetres = BigDecimal.ZERO;
        BigDecimal totalProductionKg = BigDecimal.ZERO;
        BigDecimal totalProductionNo = BigDecimal.ZERO;
        double totalBladeSpeed = 0.0;
        double totalCuttingFeed = 0.0;
        double totalCapacityMetres = 0.0;
        double totalAvailableTimeHrs = 0.0;
        double totalPlannedTimeHrs = 0.0;
        double totalRunTimeHrs = 0.0;
        double totalIdleTimeHrs = 0.0;
        double totalBreakdownTimeHrs = 0.0;
        double totalPlannedDowntimeHrs = 0.0;
        BigDecimal totalScrapQuantityKg = BigDecimal.ZERO;
        
        int monthCount = 0;
        
        for (MachineOEEMonthlyWithDailyDTO monthlyWithDaily : monthlyDataList) {
            MachineOEEMonthlyReportDTO monthly = monthlyWithDaily.getMonthData();
            
            totalProductionMetres = totalProductionMetres.add(monthly.getMachineProductionMetres());
            totalProductionKg = totalProductionKg.add(monthly.getMachineProductionKg());
            totalProductionNo = totalProductionNo.add(monthly.getMachineProductionNo());
            totalBladeSpeed += monthly.getAverageBladeSpeed();
            totalCuttingFeed += monthly.getAverageCuttingFeed();
            totalCapacityMetres += monthly.getMachineCapacityMetres();
            totalAvailableTimeHrs += monthly.getTotalAvailableTimeHrs();
            totalPlannedTimeHrs += monthly.getMachinePlannedTimeHrs();
            totalRunTimeHrs += monthly.getMachineRunTimeHrs();
            totalIdleTimeHrs += monthly.getIdleTimeHrs();
            totalBreakdownTimeHrs += monthly.getBreakdownTimeHrs();
            totalPlannedDowntimeHrs += monthly.getPlannedDowntimeHrs();
            totalScrapQuantityKg = totalScrapQuantityKg.add(monthly.getScrapQuantityKg());
            
            monthCount++;
        }
        
        // Calculate averages and percentages
        double avgBladeSpeed = monthCount > 0 ? totalBladeSpeed / monthCount : 0.0;
        double avgCuttingFeed = monthCount > 0 ? totalCuttingFeed / monthCount : 0.0;
        double performancePercent = totalCapacityMetres > 0 ? 
            (totalProductionMetres.doubleValue() / totalCapacityMetres) * 100 : 0.0;
        double availabilityPercent = totalAvailableTimeHrs > 0 ? 
            (totalRunTimeHrs / totalAvailableTimeHrs) * 100 : 0.0;
        double wastagePercent = totalProductionKg.doubleValue() > 0 ? 
            (totalScrapQuantityKg.doubleValue() / totalProductionKg.doubleValue()) * 100 : 0.0;
        double oeePercent = (performancePercent * availabilityPercent * (100 - wastagePercent)) / 10000;
        
        return MachineOEEMonthlyReportDTO.builder()
                .month("SELECTED PERIOD")
                .unit(unitCode)
                .machineName(machineName)
                .machineProductionMetres(roundBigDecimal(totalProductionMetres, 2))
                .machineProductionKg(roundBigDecimal(totalProductionKg, 2))
                .machineProductionNo(roundBigDecimal(totalProductionNo, 2))
                .averageBladeSpeed(roundDouble(avgBladeSpeed, 1))
                .averageCuttingFeed(roundDouble(avgCuttingFeed, 1))
                .machineCapacityMetres(roundDouble(totalCapacityMetres, 1))
                .performancePercent(roundDouble(performancePercent, 1))
                .totalAvailableTimeHrs(roundDouble(totalAvailableTimeHrs, 1))
                .machinePlannedTimeHrs(roundDouble(totalPlannedTimeHrs, 1))
                .machineRunTimeHrs(roundDouble(totalRunTimeHrs, 1))
                .idleTimeHrs(roundDouble(totalIdleTimeHrs, 1))
                .breakdownTimeHrs(roundDouble(totalBreakdownTimeHrs, 1))
                .plannedDowntimeHrs(roundDouble(totalPlannedDowntimeHrs, 1))
                .availabilityPercent(roundDouble(availabilityPercent, 1))
                .scrapQuantityKg(roundBigDecimal(totalScrapQuantityKg, 2))
                .wastagePercent(roundDouble(wastagePercent, 1))
                .oeePercent(roundDouble(oeePercent, 1))
                .build();
    }

    public ByteArrayResource exportMonthlyDateRangeToExcel(String unitCode, String machineName, LocalDate fromDate, LocalDate toDate) {
        MachineOEEDateRangeReportDTO report = getDateRangeReport(unitCode, machineName, fromDate, toDate);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Monthly OEE Report");
            
            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            int rowNum = 0;
            
            // Title
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Machine OEE Monthly Report - Date Range");
            titleCell.setCellStyle(headerStyle);
            
            rowNum++; // Empty row
            
            // Column headers
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = {
                "Month", "Year", "Unit", "Machine Name", "Production (Metres)", "Production (Kg)", "Production (No)",
                "Avg Blade Speed", "Avg Cutting Feed", "Capacity (Metres)", "Performance %",
                "Available Time (Hrs)", "Planned Time (Hrs)", "Run Time (Hrs)", "Idle Time (Hrs)",
                "Availability %", "Scrap (Kg)", "Wastage %", "OEE %"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Data rows - only monthly data, no daily data
            for (MachineOEEMonthlyWithDailyDTO monthlyWithDaily : report.getMonthlyData()) {
                MachineOEEMonthlyReportDTO monthly = monthlyWithDaily.getMonthData();
                Row dataRow = sheet.createRow(rowNum++);
                int colNum = 0;
                
                createCell(dataRow, colNum++, monthly.getMonth(), dataStyle);
                createCell(dataRow, colNum++, monthly.getYear() != null ? monthly.getYear().toString() : "N/A", dataStyle);
                createCell(dataRow, colNum++, monthly.getUnit(), dataStyle);
                createCell(dataRow, colNum++, monthly.getMachineName(), dataStyle);
                createCell(dataRow, colNum++, monthly.getMachineProductionMetres().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getMachineProductionKg().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getMachineProductionNo().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getAverageBladeSpeed().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getAverageCuttingFeed().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getMachineCapacityMetres().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getPerformancePercent().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getTotalAvailableTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getMachinePlannedTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getMachineRunTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getIdleTimeHrs().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getAvailabilityPercent().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getScrapQuantityKg().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getWastagePercent().toString(), dataStyle);
                createCell(dataRow, colNum++, monthly.getOeePercent().toString(), dataStyle);
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel file", e);
        }
    }

    // Helper methods for Excel generation
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private void createDataRow(Sheet sheet, int rowNum, String label, String value, CellStyle headerStyle, CellStyle dataStyle) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(0);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(headerStyle);
        
        Cell valueCell = row.createCell(1);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(dataStyle);
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}


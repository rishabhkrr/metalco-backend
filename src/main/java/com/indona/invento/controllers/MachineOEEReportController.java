package com.indona.invento.controllers;

import com.indona.invento.dto.MachineOEEDailyReportDTO;
import com.indona.invento.dto.MachineOEEMonthlyReportDTO;
import com.indona.invento.services.MachineOEEReportService;
import com.indona.invento.services.impl.MachineOEEReportServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/machine-oee-report")
@RequiredArgsConstructor
public class MachineOEEReportController {

    private final MachineOEEReportService machineOEEReportService;

    /**
     * Get monthly OEE report for a specific machine with overall data
     * Supports both single month and date range queries
     * 
     * @param unitCode Unit code (required)
     * @param machineName Machine name (required)
     * @param month Month (1-12), optional - for single month query
     * @param year Year, optional - for single month query
     * @param fromDate Start date (dd/MM/yyyy), optional - for date range query
     * @param toDate End date (dd/MM/yyyy), optional - for date range query
     * @return Monthly OEE report with overall data and monthly/daily breakdown
     */
    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyReport(
            @RequestParam String unitCode,
            @RequestParam String machineName,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            // Check if date range is provided
            if (fromDate != null && toDate != null) {
                LocalDate from = LocalDate.parse(fromDate, formatter);
                LocalDate to = LocalDate.parse(toDate, formatter);
                
                return ResponseEntity.ok(machineOEEReportService.getDateRangeReport(
                        unitCode, machineName, from, to
                ));
            } else {
                // Single month query (backward compatible)
                MachineOEEMonthlyReportDTO currentMonth = machineOEEReportService.getMonthlyReport(
                        unitCode, machineName, month, year
                );
                MachineOEEMonthlyReportDTO overall = machineOEEReportService.getOverallReport(
                        unitCode, machineName
                );
                return ResponseEntity.ok(Map.of(
                        "overall", overall,
                        "currentMonth", currentMonth
                ));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate monthly OEE report: " + e.getMessage()));
        }
    }

    /**
     * Get daily OEE report breakdown for a specific machine
     * Returns data in descending order (most recent first)
     * 
     * @param unitCode Unit code (required)
     * @param machineName Machine name (required)
     * @param month Month (1-12), optional - defaults to current month
     * @param year Year, optional - defaults to current year
     * @return List of daily OEE reports
     */
    @GetMapping("/daily")
    public ResponseEntity<?> getDailyReport(
            @RequestParam String unitCode,
            @RequestParam String machineName,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        try {
            List<MachineOEEDailyReportDTO> reports = machineOEEReportService.getDailyReport(
                    unitCode, machineName, month, year
            );
            return ResponseEntity.ok(Map.of(
                    "totalDays", reports.size(),
                    "data", reports
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate daily OEE report"));
        }
    }

    /**
     * Export monthly OEE report to Excel
     * Supports both single month and date range exports
     * 
     * @param unitCode Unit code (required)
     * @param machineName Machine name (required)
     * @param month Month (1-12), optional - for single month export
     * @param year Year, optional - for single month export
     * @param fromDate Start date (dd/MM/yyyy), optional - for date range export
     * @param toDate End date (dd/MM/yyyy), optional - for date range export
     * @return Excel file download
     */
    @GetMapping("/monthly/export")
    public ResponseEntity<?> exportMonthlyToExcel(
            @RequestParam String unitCode,
            @RequestParam String machineName,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            ByteArrayResource resource;
            String filename;
            
            // Check if date range is provided
            if (fromDate != null && toDate != null) {
                LocalDate from = LocalDate.parse(fromDate, formatter);
                LocalDate to = LocalDate.parse(toDate, formatter);
                
                // Use the new monthly date range export method
                resource = ((MachineOEEReportServiceImpl) machineOEEReportService)
                        .exportMonthlyDateRangeToExcel(unitCode, machineName, from, to);
                
                filename = String.format("Monthly_OEE_Report_%s_%s_to_%s.xlsx", 
                        machineName,
                        fromDate.replace("/", "-"),
                        toDate.replace("/", "-"));
            } else {
                // Single month export
                resource = machineOEEReportService.exportMonthlyToExcel(
                        unitCode, machineName, month, year
                );
                
                filename = String.format("Monthly_OEE_Report_%s_%s_%s.xlsx", 
                        machineName, 
                        month != null ? month : LocalDate.now().getMonthValue(),
                        year != null ? year : LocalDate.now().getYear());
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export monthly OEE report"));
        }
    }

    /**
     * Export daily OEE report to Excel
     * Supports both single month and date range exports
     * 
     * @param unitCode Unit code (required)
     * @param machineName Machine name (required)
     * @param month Month (1-12), optional - for single month export
     * @param year Year, optional - for single month export
     * @param fromDate Start date (dd/MM/yyyy), optional - for date range export
     * @param toDate End date (dd/MM/yyyy), optional - for date range export
     * @return Excel file download
     */
    @GetMapping("/daily/export")
    public ResponseEntity<?> exportDailyToExcel(
            @RequestParam String unitCode,
            @RequestParam String machineName,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate
    ) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            ByteArrayResource resource;
            String filename;
            
            // Check if date range is provided
            if (fromDate != null && toDate != null) {
                LocalDate from = LocalDate.parse(fromDate, formatter);
                LocalDate to = LocalDate.parse(toDate, formatter);
                
                // Use the new daily date range export method
                resource = ((MachineOEEReportServiceImpl) machineOEEReportService)
                        .exportDailyDateRangeToExcel(unitCode, machineName, from, to);
                
                filename = String.format("Daily_OEE_Report_%s_%s_to_%s.xlsx", 
                        machineName,
                        fromDate.replace("/", "-"),
                        toDate.replace("/", "-"));
            } else {
                // Single month export
                resource = machineOEEReportService.exportDailyToExcel(
                        unitCode, machineName, month, year
                );
                
                filename = String.format("Daily_OEE_Report_%s_%s_%s.xlsx", 
                        machineName,
                        month != null ? month : LocalDate.now().getMonthValue(),
                        year != null ? year : LocalDate.now().getYear());
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export daily OEE report"));
        }
    }

    /**
     * Export overall (all-time) OEE report to Excel
     * 
     * @param unitCode Unit code (required)
     * @param machineName Machine name (required)
     * @return Excel file download
     */
    @GetMapping("/overall/export")
    public ResponseEntity<?> exportOverallToExcel(
            @RequestParam String unitCode,
            @RequestParam String machineName
    ) {
        try {
            ByteArrayResource resource = machineOEEReportService.exportOverallToExcel(
                    unitCode, machineName
            );
            
            String filename = String.format("Overall_OEE_Report_%s.xlsx", machineName);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to export overall OEE report"));
        }
    }
}


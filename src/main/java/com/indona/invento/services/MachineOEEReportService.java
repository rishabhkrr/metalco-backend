package com.indona.invento.services;

import com.indona.invento.dto.MachineOEEDailyReportDTO;
import com.indona.invento.dto.MachineOEEDateRangeReportDTO;
import com.indona.invento.dto.MachineOEEMonthlyReportDTO;
import org.springframework.core.io.ByteArrayResource;

import java.time.LocalDate;
import java.util.List;

public interface MachineOEEReportService {

    /**
     * Get monthly OEE report for a specific machine
     * @param unitCode Unit code
     * @param machineName Machine name
     * @param month Month (1-12), if null defaults to current month
     * @param year Year, if null defaults to current year
     * @return Monthly OEE report
     */
    MachineOEEMonthlyReportDTO getMonthlyReport(String unitCode, String machineName, Integer month, Integer year);

    /**
     * Get daily OEE report breakdown for a specific machine
     * @param unitCode Unit code
     * @param machineName Machine name
     * @param month Month (1-12), if null defaults to current month
     * @param year Year, if null defaults to current year
     * @return List of daily OEE reports in descending order (most recent first)
     */
    List<MachineOEEDailyReportDTO> getDailyReport(String unitCode, String machineName, Integer month, Integer year);

    /**
     * Get overall (all-time) OEE report for a specific machine
     * @param unitCode Unit code
     * @param machineName Machine name
     * @return Overall OEE report aggregating all available data
     */
    MachineOEEMonthlyReportDTO getOverallReport(String unitCode, String machineName);

    /**
     * Export monthly OEE report to Excel
     * @param unitCode Unit code
     * @param machineName Machine name
     * @param month Month (1-12), if null defaults to current month
     * @param year Year, if null defaults to current year
     * @return Excel file as ByteArrayResource
     */
    ByteArrayResource exportMonthlyToExcel(String unitCode, String machineName, Integer month, Integer year);

    /**
     * Export daily OEE report to Excel
     * @param unitCode Unit code
     * @param machineName Machine name
     * @param month Month (1-12), if null defaults to current month
     * @param year Year, if null defaults to current year
     * @return Excel file as ByteArrayResource
     */
    ByteArrayResource exportDailyToExcel(String unitCode, String machineName, Integer month, Integer year);

    /**
     * Export overall (all-time) OEE report to Excel
     * @param unitCode Unit code
     * @param machineName Machine name
     * @return Excel file as ByteArrayResource
     */
    ByteArrayResource exportOverallToExcel(String unitCode, String machineName);

    /**
     * Get OEE report for a date range with monthly breakdown and daily data
     * @param unitCode Unit code
     * @param machineName Machine name
     * @param fromDate Start date (inclusive)
     * @param toDate End date (inclusive)
     * @return Date range report with overall, monthly, and daily data
     */
    MachineOEEDateRangeReportDTO getDateRangeReport(String unitCode, String machineName, LocalDate fromDate, LocalDate toDate);

    /**
     * Export date range OEE report to Excel (includes all months and days in range)
     * @param unitCode Unit code
     * @param machineName Machine name
     * @param fromDate Start date
     * @param toDate End date
     * @return Excel file as ByteArrayResource
     */
    ByteArrayResource exportDateRangeToExcel(String unitCode, String machineName, LocalDate fromDate, LocalDate toDate);
}


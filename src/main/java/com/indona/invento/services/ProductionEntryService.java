package com.indona.invento.services;

import com.indona.invento.dto.ProductionEntryDto;
import com.indona.invento.dto.ScrapSummaryDto;
import com.indona.invento.entities.ProductionEntryEntity;
import com.indona.invento.entities.ProductionIdleTimeEntryEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ProductionEntryService {

    List<Map<String, Object>> getDetailsBySoAndLineNumber(String soNumber, String lineNumber);

    List<ProductionEntryEntity> getAllProductionEntries(String fromDate, String toDate);

    ProductionEntryDto createProductionEntry(ProductionEntryDto dto);

    List<Map<String, Object>> getSoNumbersAndLineNumbers();

    List<Map<String, String>> getLineNumbers(String soNumber);

    List<Map<String, String>> getRmDescriptions();

    List<ScrapSummaryDto> getScrapSummary(LocalDate fromDate, LocalDate toDate);

    List<Map<String, Object>> getDetailsBySoNumLineNumAndDescription(String soNumber, String lineNumber, String itemDescription);

    List<ProductionIdleTimeEntryEntity> getAllProductionIdleEntries(String fromDate, String toDate);

    List<String> getAvailableMachinesForDropdown();

    Map<String, String> getLastEntryEndTime();

    void deleteAllProductionEntries();
}

package com.indona.invento.controllers;

import com.indona.invento.dao.ProductionScheduleRepository;
import com.indona.invento.dto.ProductionEntryDto;
import com.indona.invento.dto.ScrapSummaryDto;
import com.indona.invento.entities.ProductionEntryEntity;
import com.indona.invento.entities.ProductionIdleTimeEntryEntity;
import com.indona.invento.services.ProductionEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/production-entry")
public class ProductionEntryController {

    @Autowired
    private ProductionEntryService productionEntryService;

    @Autowired
    private ProductionScheduleRepository productionScheduleRepository;

    @GetMapping("/details-by-so-no")
    public ResponseEntity<List<Map<String, Object>>> getDetailsBySoAndLineNumber(@RequestParam String soNumber, @RequestParam String lineNumber) {
        List<Map<String, Object>> response = productionEntryService.getDetailsBySoAndLineNumber(soNumber, lineNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/details-by-so-line-no-and-description")
    public ResponseEntity<List<Map<String, Object>>> getDetailsBySoNumLineNumAndDescription(@RequestParam String soNumber, @RequestParam String lineNumber,
                                                                                            @RequestParam String itemDescription) {
        List<Map<String, Object>> response = productionEntryService.getDetailsBySoNumLineNumAndDescription(soNumber, lineNumber, itemDescription);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    public List<ProductionEntryEntity> getAllProductionEntries(@RequestParam(required = false) String fromStartDate,
                                                               @RequestParam(required = false) String toStartDate) {
        return productionEntryService.getAllProductionEntries(fromStartDate, toStartDate);
    }

    @PostMapping
    public ProductionEntryDto createProductionEntry(@RequestBody ProductionEntryDto dto) {
        return productionEntryService.createProductionEntry(dto);
    }

    @GetMapping("/so-line-numbers")
    public ResponseEntity<Map<String, Object>> getSoNumbersAndLineNumbers() {
        List<Map<String, Object>> numbers = productionEntryService.getSoNumbersAndLineNumbers();
        return ResponseEntity.ok(Map.of("data", numbers, "status", "success"));
    }


    @GetMapping("/line-numbers")
    public ResponseEntity<Map<String, Object>> getLineNumbers(@RequestParam String soNumber) {
        List<Map<String, String>> numbers = productionEntryService.getLineNumbers(soNumber);
        return ResponseEntity.ok(Map.of("data", numbers, "status", "success"));
    }

    @GetMapping("/rm-descriptions")
    public ResponseEntity<Map<String, Object>> getRmDescriptions() {
        List<Map<String, String>> rm = productionEntryService.getRmDescriptions();
        return ResponseEntity.ok(Map.of("data", rm, "status", "success"));
    }

    @GetMapping("/scrap-summary")
    public ResponseEntity<List<ScrapSummaryDto>> getScrapSummary(
            @RequestParam(value = "fromStartDate", required = false) LocalDate fromStartDate,
            @RequestParam(value = "toStartDate", required = false) LocalDate toStartDate){

        return ResponseEntity.ok(productionEntryService.getScrapSummary(fromStartDate, toStartDate));
    }

    @GetMapping("/all-production-idle-summary")
    public List<ProductionIdleTimeEntryEntity> getAllProductionIdleEntries(@RequestParam(required = false) String fromDate,
                                                                           @RequestParam(required = false) String toDate) {
        return productionEntryService.getAllProductionIdleEntries(fromDate, toDate);
    }

    @GetMapping("/machines-dropdown")
    public ResponseEntity<List<String>> getMachinesForDropdown() {
        return ResponseEntity.ok(productionEntryService.getAvailableMachinesForDropdown());
    }

    @GetMapping("/last-entry-end-time")
    public ResponseEntity<Map<String, String>> getLastEntryEndTime(){
        return ResponseEntity.ok(productionEntryService.getLastEntryEndTime());
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<Map<String, String>> deleteAllProductionEntries() {
        productionEntryService.deleteAllProductionEntries();
        return ResponseEntity.ok(Map.of("status", "success", "message", "All production entries deleted"));
    }
}

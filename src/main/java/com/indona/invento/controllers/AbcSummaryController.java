package com.indona.invento.controllers;

import com.indona.invento.dto.StockSummaryWithItemDetailsDTO;
import com.indona.invento.dto.StockAnalysisDto;
import com.indona.invento.services.StockSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/abcstock-summary")
public class AbcSummaryController {

    @Autowired
    private StockSummaryService stockSummaryService;

    // ✅ This endpoint is now clearly defined and won't conflict with any path variable
    @GetMapping("/filter")
    public ResponseEntity<List<StockSummaryWithItemDetailsDTO>> getFilteredSummary(@RequestParam(required = false) String unit,
                                                                                   @RequestParam(required = false) List<String> brands,
                                                                                   @RequestParam(required = false) List<String> productCategories,
                                                                                   @RequestParam(required = false) List<String> materialTypes) {
        List<StockSummaryWithItemDetailsDTO> result = stockSummaryService.getFilteredSummary(unit, brands, productCategories, materialTypes);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/analysis")
    public ResponseEntity<?> getStockAnalysis(
            @RequestParam String itemDescription,
            @RequestParam String unit,
            @RequestParam(required = false) String productCategory) {
        try {
            StockAnalysisDto analysis = stockSummaryService.getStockAnalysisByItemAndUnit(itemDescription, unit, productCategory);
            return ResponseEntity.ok(analysis);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

}

package com.indona.invento.controllers;

import com.indona.invento.dto.CurrentPriceSummaryDto;
import com.indona.invento.services.PriceSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/price-summary")
@RequiredArgsConstructor
public class PriceSummaryController {

    private final PriceSummaryService priceSummaryService;

    @GetMapping("/latest")
    public ResponseEntity<CurrentPriceSummaryDto> getLatestSummary() {
        return ResponseEntity.ok(priceSummaryService.getSummary());
    }
}


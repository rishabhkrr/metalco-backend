package com.indona.invento.controllers;

import com.indona.invento.dao.HindalcoPriceRepository;
import com.indona.invento.dto.HindalcoPriceDto;
import com.indona.invento.entities.HindalcoPriceEntity;
import com.indona.invento.services.HindalcoPriceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hindalco-price")
public class HindalcoPriceController {

    @Autowired
    private HindalcoPriceService service;

    // ✅ POST: Create or update Hindalco Price
    @GetMapping("/generate-today")
    public ResponseEntity<?> generateToday() {
        try {
            List<HindalcoPriceEntity> entries = service.generateToday();
            return ResponseEntity.ok(Map.of(
                    "message", entries.size() + " entries generated",
                    "data", entries
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate Hindalco prices"));
        }
    }



    // ✅ GET: All Price Records
    @GetMapping("/all")
    public ResponseEntity<?> getAllPrices() {
        try {
            List<HindalcoPriceEntity> prices = service.getAllPricesWithoutPagination();
            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    // ✅ GET: Price By Date
    @GetMapping("/by-date")
    public ResponseEntity<?> getByDate(@RequestParam("date")
                                       @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        HindalcoPriceEntity entity = service.getByDate(date);
        if (entity == null) {
            return ResponseEntity.badRequest().body("No price found for date: " + date);
        }
        return ResponseEntity.ok(entity);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updatePrice(@PathVariable Long id, @RequestBody HindalcoPriceDto dto) {
        try {
            HindalcoPriceEntity updated = service.updatePrice(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while updating Hindalco price."));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestHindalcoPrice() {
        HindalcoPriceEntity latest = service.getLatestPrice();
        if (latest == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No Hindalco price found"));
        }

        return ResponseEntity.ok(Map.of(
                "date", latest.getPriceDate(),
                "currentHindalcoPrice", latest.getPrice()
        ));
    }
}

package com.indona.invento.controllers;

import com.indona.invento.dto.NalcoPriceUpdateDto;
import com.indona.invento.entities.NalcoPriceEntity;
import com.indona.invento.services.NalcoPriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/nalco-price")
public class NalcoPriceController {

    @Autowired
    private NalcoPriceService service;

    // Generate today's price (auto copy from last day)
    @GetMapping("/generate-today")
    public ResponseEntity<?> generateToday() {
        try {
            List<NalcoPriceEntity> generated = service.generateTodayPrice();
            return ResponseEntity.ok(generated);

        } catch (RuntimeException e) {
            // Service me agar "aaj ka record already hai" par RuntimeException throw hoti hai
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Today's price already exists."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while generating today's price."));
        }
    }


    // Update price & optionally attach PDF link
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateNalcoPriceById(
            @PathVariable Long id,
            @RequestBody NalcoPriceUpdateDto dto) {
        try {
            NalcoPriceEntity updated = service.updateNalcoPrice(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while updating Nalco price."));
        }
    }



    // View all
    @GetMapping("/all")
    public ResponseEntity<?> getAllPrices() {
        try {
            List<NalcoPriceEntity> prices = service.getAllPricesWithoutPagination();
            return ResponseEntity.ok(prices);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/latest")
    public ResponseEntity<?> getLatestNalcoPrice() {
        NalcoPriceEntity latest = service.getLatestPrice();
        if (latest == null) {
            return ResponseEntity.status(404).body(Map.of("error", "No Nalco price found"));
        }

        return ResponseEntity.ok(Map.of(
                "date", latest.getDate(),
                "currentNalcoPrice", latest.getNalcoPrice()
        ));
    }
}


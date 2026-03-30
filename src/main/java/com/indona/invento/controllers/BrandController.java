package com.indona.invento.controllers;

import com.indona.invento.dto.BrandDto;
import com.indona.invento.entities.BrandEntity;
import com.indona.invento.services.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @PostMapping("/add")
    public ResponseEntity<?> addBrand(@RequestBody BrandDto dto) {
        try {
            BrandEntity saved = brandService.addBrand(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<BrandDto>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }
}


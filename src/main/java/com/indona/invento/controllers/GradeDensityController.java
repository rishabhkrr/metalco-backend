package com.indona.invento.controllers;


import com.indona.invento.dto.GradeDensityRequest;
import com.indona.invento.entities.GradeDensityEntity;
import com.indona.invento.services.GradeDensityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/grade-density")
@RequiredArgsConstructor
public class GradeDensityController {

    private final GradeDensityService service;

    @PostMapping("/add")
    public ResponseEntity<?> addGradeDensity(@RequestBody GradeDensityRequest request) {
        try {
            GradeDensityEntity saved = service.saveGradeDensity(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Grade-density saved successfully",
                    "id", saved.getId(),
                    "grade", saved.getGrade(),
                    "density", saved.getDensity()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<GradeDensityEntity>> getAllGrades() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/density/{grade}")
    public ResponseEntity<?> getDensity(@PathVariable String grade) {
        return service.getDensityByGrade(grade)
                .map(density -> ResponseEntity.ok(Map.of(
                        "grade", grade,
                        "density", density
                )))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Grade not found")));
    }
}

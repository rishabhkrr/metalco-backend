package com.indona.invento.controllers;

import com.indona.invento.dto.RawMaterialQrDTO;
import com.indona.invento.entities.RawMaterialQrEntity;
import com.indona.invento.services.RawMaterialQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/raw-material-qr")
public class RawMaterialQrController {

    @Autowired
    private RawMaterialQrService rawMaterialQrService;

    @PostMapping
    public ResponseEntity<?> createRawMaterialQr(@RequestBody RawMaterialQrDTO dto) {
        try {
            RawMaterialQrEntity saved = rawMaterialQrService.createRawMaterialQr(dto);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "rawMaterialQrId", saved.getRawMaterialQrId(),
                    "data", saved));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/{rawMaterialQrId}")
    public ResponseEntity<?> getByRawMaterialQrId(@PathVariable String rawMaterialQrId) {
        try {
            RawMaterialQrEntity entity = rawMaterialQrService.getByRawMaterialQrId(rawMaterialQrId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", entity));
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }
}

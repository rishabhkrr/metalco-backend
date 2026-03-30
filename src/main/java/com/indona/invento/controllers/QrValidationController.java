package com.indona.invento.controllers;

import com.indona.invento.services.impl.RawMaterialQrServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * QR Code validation and scanning controller.
 * FRD Module 7: QR code must be scannable, validate metadata, and map to inventory.
 */
@RestController
@RequestMapping("/qr")
public class QrValidationController {

    @Autowired
    private RawMaterialQrServiceImpl rawMaterialQrService;

    /**
     * Scan and validate a QR code.
     * Returns item details, current location, and status.
     */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanQrCode(@RequestBody Map<String, String> request) {
        String qrContent = request.get("qrContent");
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "error", "QR content is required"
            ));
        }

        Map<String, Object> result = rawMaterialQrService.validateQrScan(qrContent.trim());
        return ResponseEntity.ok(result);
    }

    /**
     * Decode a QR code string into its components.
     * FRD format: UNIT-STORE-AREA-RACK-COLUMN-BIN-BATCHNO-ITEMCODE-SLNO
     */
    @PostMapping("/decode")
    public ResponseEntity<Map<String, String>> decodeQrCode(@RequestBody Map<String, String> request) {
        String qrContent = request.get("qrContent");
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "QR content is required"));
        }

        try {
            Map<String, String> decoded = rawMaterialQrService.decodeQrCode(qrContent.trim());
            return ResponseEntity.ok(decoded);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Transfer item by scanning QR at new location.
     * Updates rack/bin/store and regenerates QR ID.
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> transferByQr(@RequestBody Map<String, String> request) {
        try {
            String qrId = request.get("qrId");
            String newStore = request.get("newStore");
            String newRack = request.get("newRack");
            String newColumn = request.get("newColumn");
            String newBin = request.get("newBin");
            String newStatus = request.get("newStatus");

            var updated = rawMaterialQrService.updateQrLocation(
                    qrId, newStore, newRack, newColumn, newBin, newStatus);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "QR location updated successfully",
                    "newQrId", updated.getRawMaterialQrId(),
                    "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }
}

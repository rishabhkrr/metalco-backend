package com.indona.invento.controllers;

import com.indona.invento.dto.VehicleWeighmentRequestDTO;
import com.indona.invento.dto.VehicleWeighmentResponseDTO;
import com.indona.invento.services.VehicleWeighmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/vehicle-weighment")
@RequiredArgsConstructor
public class VehicleWeighmentController {

    private final VehicleWeighmentService vehicleWeighmentService;

    @PostMapping
    public ResponseEntity<VehicleWeighmentResponseDTO> createWeighment(@RequestBody VehicleWeighmentRequestDTO requestDTO) {
        VehicleWeighmentResponseDTO response = vehicleWeighmentService.createWeighment(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VehicleWeighmentResponseDTO>> getAllWeighments() {
        List<VehicleWeighmentResponseDTO> response = vehicleWeighmentService.getAllWeighments();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleWeighmentResponseDTO> getWeighmentById(@PathVariable Long id) {
        VehicleWeighmentResponseDTO response = vehicleWeighmentService.getWeighmentById(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/ref/{weighmentRefNumber}")
    public ResponseEntity<VehicleWeighmentResponseDTO> getWeighmentByRefNumber(@PathVariable String weighmentRefNumber) {
        VehicleWeighmentResponseDTO response = vehicleWeighmentService.getWeighmentByRefNumber(weighmentRefNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/unit/{unitCode}")
    public ResponseEntity<List<VehicleWeighmentResponseDTO>> getWeighmentsByUnit(@PathVariable String unitCode) {
        List<VehicleWeighmentResponseDTO> response = vehicleWeighmentService.getWeighmentsByUnit(unitCode);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/vehicle/{vehicleNumber}")
    public ResponseEntity<VehicleWeighmentResponseDTO> getWeighmentByVehicleNumber(@PathVariable String vehicleNumber) {
        VehicleWeighmentResponseDTO response = vehicleWeighmentService.getWeighmentByVehicleNumber(vehicleNumber);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleWeighmentResponseDTO> updateWeighment(
            @PathVariable Long id,
            @RequestBody VehicleWeighmentRequestDTO requestDTO) {
        VehicleWeighmentResponseDTO response = vehicleWeighmentService.updateWeighment(id, requestDTO);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<VehicleWeighmentResponseDTO> verifyWeighment(@PathVariable Long id) {
        VehicleWeighmentResponseDTO response = vehicleWeighmentService.verifyWeighment(id);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/vehicles/in")
    public ResponseEntity<List<String>> getVehiclesWithInStatus() {
        List<String> vehicleNumbers = vehicleWeighmentService.getAllVehicleNumbersWithInStatus();
        return new ResponseEntity<>(vehicleNumbers, HttpStatus.OK);
    }

    @PostMapping("/save-invoice")
    public ResponseEntity<?> saveInvoiceNumber(
            @RequestParam String vehicleNumber,
            @RequestParam String gateEntryRefNo,
            @RequestParam String invoiceNumber) {
        try {
            VehicleWeighmentResponseDTO response = vehicleWeighmentService.saveInvoiceNumber(vehicleNumber, gateEntryRefNo, invoiceNumber);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Invoice number saved successfully",
                    "data", response,
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to save invoice number",
                    "details", e.getMessage(),
                    "status", "error"
            ));
        }
    }

}

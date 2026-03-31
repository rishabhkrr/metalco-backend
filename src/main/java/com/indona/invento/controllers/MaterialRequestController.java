package com.indona.invento.controllers;

import com.indona.invento.dto.*;
import com.indona.invento.entities.MaterialRequestHeader;
import com.indona.invento.entities.SOSchedulePickListEntity;
import com.indona.invento.entities.StockTransferWHReturnEntity;
import com.indona.invento.entities.StockTransferWarehouseEntity;
import com.indona.invento.services.MaterialRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/material-request")
@RequiredArgsConstructor
public class MaterialRequestController {

    private final MaterialRequestService service;

    @PostMapping("/create")
    public ResponseEntity<MaterialRequestHeader> createRequest(@RequestBody MaterialRequestDTO dto) {
        MaterialRequestHeader savedHeader = service.createMaterialRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedHeader);
    }


    @GetMapping("/summary/all")
    public ResponseEntity<List<MaterialRequestSummaryResponseDTO>> getAllSummaries() {
        List<MaterialRequestSummaryResponseDTO> summaries = service.getAllSummaries();
        return ResponseEntity.ok(summaries);
    }


    @GetMapping("/iu-material-transfer-schedule")
    public ResponseEntity<List<MaterialTransferScheduleDto>> getAllSummariesFlat(@RequestParam (required = false) String mrNumber,
                                                                                 @RequestParam (required = false) String lineNumber) {
        List<MaterialTransferScheduleDto> summary = service.getAllSummaryFromMaterialRequest(mrNumber, lineNumber);
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/pick-list-rack-details")
    public ResponseEntity<List<Map<String, Object>>> getPickListRackDetails(
            @RequestParam String unit,
            @RequestParam String itemDescription,
            @RequestParam(required = false) String store) {

        List<Map<String, Object>> result = service.findPickListRackDetails(unit, itemDescription, store);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/save-iu-material")
    public ResponseEntity<SOSchedulePickListEntity> saveIUMaterial(@RequestBody SOSchedulePickListEntity entity) {
        SOSchedulePickListEntity saved = service.saveIUMaterial(entity);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/iu-material/{id}")
    public ResponseEntity<SOSchedulePickListEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PutMapping("/iu-material/{id}")
    public ResponseEntity<SOSchedulePickListEntity> updateIUMaterial(
            @PathVariable Long id,
            @RequestBody SOSchedulePickListEntity updated) {
        SOSchedulePickListEntity saved = service.updateIUMaterial(id, updated);
        if (saved == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(saved);
    }

    @PostMapping("/save-stock-transfer-warehouse")
    public ResponseEntity<StockTransferWarehouseDto> saveStockTransferWarehouse(
            @RequestBody StockTransferWarehouseDto dto) {

        StockTransferWarehouseDto savedDto = service.saveStockTransferWarehouse(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
    }

    @PostMapping("/save-stock-transfer-return")
    public ResponseEntity<?> saveStockTransfer(@Valid @RequestBody StockTransferWHReturnDto dto, @RequestParam Long SOSchedulePickListId) {
        StockTransferWHReturnEntity saved = service.saveStockTransfer(dto, SOSchedulePickListId);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(saved.getId())
                .toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockTransferWarehouseDto> getStockTransferWarehouseById(@PathVariable Long id) {
        StockTransferWarehouseDto dto = service.getStockTransferWarehouseById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/get-qr-details")
    public ResponseEntity<List<Map<String, Object>>> getQRDetails(
            @RequestParam String mrNumber,
            @RequestParam String lineNumber,
            @RequestParam(required = false) String itemDescription) {

        List<Map<String, Object>> result = service.getQrDetails(mrNumber, lineNumber, itemDescription);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllMaterialRequests() {
        try {
            service.deleteAllMaterialRequests();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All material requests deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all material requests",
                    "details", e.getMessage()
            ));
        }
    }
}


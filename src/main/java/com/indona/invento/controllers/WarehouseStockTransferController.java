package com.indona.invento.controllers;

import com.indona.invento.dto.WarehouseStockReturnRequestDTO;
import com.indona.invento.dto.WarehouseStockTransferRequestDTO;
import com.indona.invento.entities.StockSummaryEntity;
import com.indona.invento.entities.WarehouseStockTransferEntity;
import com.indona.invento.services.WarehouseStockTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouse-stock-transfer")
@RequiredArgsConstructor
public class WarehouseStockTransferController {

    private final WarehouseStockTransferService service;

    @PostMapping("/submit")
    public ResponseEntity<WarehouseStockTransferEntity> submitTransfer(@RequestBody WarehouseStockTransferRequestDTO request) {
        WarehouseStockTransferEntity savedEntity = service.processWarehouseTransfer(request);
        return ResponseEntity.ok(savedEntity);
    }


    @PostMapping("/return")
    public ResponseEntity<List<StockSummaryEntity>> submitReturn(@RequestBody WarehouseStockReturnRequestDTO request) {
        List<StockSummaryEntity> savedEntities = service.processReturn(request);
        return ResponseEntity.ok(savedEntities);
    }

}


package com.indona.invento.controllers;

import java.util.List;

import com.indona.invento.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.indona.invento.entities.StockTransferEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.services.StockTransferService;

import java.util.Map;

@RestController
@RequestMapping("/stock-transfer")
public class StockTransfersController {

    @Autowired
    private StockTransferService stockTransferService;

    @GetMapping("/all")
    public List<StockTransferWithLineItemsDto> getAllStockTransfers() {
        return stockTransferService.getAllStockTransfersAll();
    }


    // ✅ Stock Transfer Summary Dashboard API
    @GetMapping("/summary/dashboard")
    public ResponseEntity<List<StockTransferSummaryDto>> getStockTransferSummaryDashboard() {
        List<StockTransferSummaryDto> summaries = stockTransferService.getStockTransferSummaryDashboard();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping
    public List<StockTransferEntity> getAllStockTransfers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search) {
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockTransferService.getAllStockTransfers(search, pageable);
    }


    @GetMapping("/store/{id}")
    public List<StockTransferEntity> getStockinByStore(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search) {
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockTransferService.getStockTransferByStore(id, search, pageable);
    }

    @GetMapping("/warehouse/{id}")
    public List<StockTransferEntity> getStockinByWarehouse(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search) {
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockTransferService.getStockTransferByWarehouse(id, search, pageable);
    }

    @GetMapping("/by-from-to/{id}")
    public List<StockTransferEntity> getTransferByFromAndTo(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search) {
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockTransferService.getStockTransferByFromAndTo(id, search, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransferSkuDto> getStockTransferById(@PathVariable Long id) {
        TransferSkuDto department = stockTransferService.getStockTransferById(id);
        return department != null ?
                new ResponseEntity<>(department, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<TransferSkuDto> createStockTransfer(@RequestBody TransferSkuDto department) {
        TransferSkuDto createdStockTransfer = stockTransferService.createStockTransfer(department);
        return new ResponseEntity<>(createdStockTransfer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransferSkuDto> updateStockTransfer(@PathVariable Long id, @RequestBody TransferSkuDto department) {
        TransferSkuDto updatedStockTransfer = stockTransferService.updateStockTransfer(id, department);
        return updatedStockTransfer != null ?
                new ResponseEntity<>(updatedStockTransfer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}/{remark}")
    public ResponseEntity<Void> deleteStockTransfer(@PathVariable Long id, @PathVariable String remark) {
        stockTransferService.deleteStockTransfer(id, remark);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Save Stock Transfer and mark GRN as COMPLETED
    @PostMapping("/save")
    public ResponseEntity<?> saveStockTransfer(@RequestBody SaveStockTransferRequestDto request) {
        try {
            SaveStockTransferResponseDto response = stockTransferService.saveStockTransfer(request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to save stock transfer: " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "message", "An error occurred: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllStockTransfers() {
        try {
            stockTransferService.deleteAllStockTransfers();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All stock transfers deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all stock transfers",
                    "details", e.getMessage()
            ));
        }
    }
}

package com.indona.invento.controllers;

import java.util.List;

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

import com.indona.invento.dto.TransferSkuDto;
import com.indona.invento.entities.StockReturnEntity;
import com.indona.invento.services.StockReturnService;

@RestController
@RequestMapping("/stock-returns")
public class StockReturnController {

	@Autowired
    private StockReturnService stockReturnService;

    @GetMapping("/return")
    public List<StockReturnEntity> getAllStockReturnsReturns(
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search) {
    	Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockReturnService.getAllStockReturnReturns(search, pageable);
    }
    
    @GetMapping("/return/{id}")
    public List<StockReturnEntity> getAllStockReturnsInvoices(@PathVariable Long id) {
        return stockReturnService.getAllStockReturnInvoices(id);
    }
    
    @GetMapping("/return/store/{id}")
    public List<StockReturnEntity> getAllStockReturnsReturnsStore(
    		@PathVariable Long id,
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search) {
    	Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockReturnService.getAllStockReturnReturnsStore(id, search, pageable);
    }
    
    @GetMapping
    public List<StockReturnEntity> getAllStockReturns(
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
    	Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockReturnService.getAllStockReturn(search, type, pageable);
    }
    
    @GetMapping("/return/report")
    public List<StockReturnEntity> getAllStockReturnsReport(
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
    	Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockReturnService.getAllStockReturn(search, type, pageable);
    }

    @GetMapping("/store/{id}")
    public List<StockReturnEntity> getStockReturnByStore(
    		@PathVariable Long id,
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
    	Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockReturnService.getAllStockinByStore(id, search, type, pageable);
    }
    
    @GetMapping("warehouse/{warehouseId}/store/{id}")
    public List<StockReturnEntity> getStockReturnByWarehouseStore(
    		@PathVariable Long id, @PathVariable Long warehouseId,
    		@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "id,desc") String[] sort,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String type) {
    	Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));
        return stockReturnService.getAllStockinByStoreAndWarehouse(warehouseId, id, search, type, pageable);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<TransferSkuDto> getStockReturnById(@PathVariable Long id) {
    	TransferSkuDto line = stockReturnService.getStockReturnById(id);
        return line != null ?
                new ResponseEntity<>(line, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<TransferSkuDto> createStockReturn(@RequestBody TransferSkuDto line) {
    	TransferSkuDto createdStockReturn = stockReturnService.createStockReturn(line);
        return new ResponseEntity<>(createdStockReturn, HttpStatus.CREATED);
    }
    
//    @PostMapping("/summary")
//    public ResponseEntity<InvoiceSummaryDto> getInvoiceSummary(@RequestBody FilterDto req) {
//    	InvoiceSummaryDto res = stockReturnService.getInvoiceSummary(req);
//        return new ResponseEntity<>(res, HttpStatus.CREATED);
//    }
//    
//    @PostMapping("/summary/day")
//    public ResponseEntity<List<InvoiceSummaryDto>> getInvoiceSummaryDay(@RequestBody FilterDto req) {
//    	List<InvoiceSummaryDto> res = stockReturnService.getInvoiceSummaryDayWise(req);
//        return new ResponseEntity<>(res, HttpStatus.CREATED);
//    }
//    
//    @PostMapping("/summary/all")
//    public ResponseEntity<InvoiceSummaryDto> getInvoiceSummaryAll(@RequestBody FilterDto req) {
//    	InvoiceSummaryDto res = stockReturnService.getOverallInvoiceSummary(req);
//        return new ResponseEntity<>(res, HttpStatus.CREATED);
//    }

    @PutMapping("/{id}")
    public ResponseEntity<StockReturnEntity> updateStockReturn(@PathVariable Long id, @RequestBody StockReturnEntity line) {
    	StockReturnEntity updatedStockReturn = stockReturnService.updateStockReturn(id, line);
        return updatedStockReturn != null ?
                new ResponseEntity<>(updatedStockReturn, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}/{remark}")
    public ResponseEntity<Void> deleteStockReturn(@PathVariable Long id, @PathVariable String remark) {
        stockReturnService.deleteStockReturn(id, remark);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
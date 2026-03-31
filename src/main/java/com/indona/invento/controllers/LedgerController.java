package com.indona.invento.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.indona.invento.dto.FilterLedgerDto;
import com.indona.invento.dto.LedgerDto;
import com.indona.invento.entities.LedgerEntity;
import com.indona.invento.services.LedgerService;

@RestController
@RequestMapping("/ledger")
public class LedgerController {

    @Autowired
    private LedgerService ledgersService;

    @GetMapping
    public List<LedgerDto> getAllLedgers() {
        return ledgersService.getAllLedgers();
    }
    
    @GetMapping("/store/{id}")
    public List<LedgerDto> getLedgerByStore(@PathVariable Long id) {
        return ledgersService.getByStore(id);
    }
    
    @GetMapping("/type/{type}")
    public List<LedgerDto> getLedgerByType(@PathVariable String type) {
        return ledgersService.getByType(type);
    }
    
    @GetMapping("/store/{id}/type/{type}")
    public List<LedgerDto> getLedgerByStoreAndType(@PathVariable Long id, @PathVariable String type) {
        return ledgersService.getByStoreAndType(id, type);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LedgerEntity> getLedgerById(@PathVariable Long id) {
        LedgerEntity customer = ledgersService.getLedgerById(id);
        return customer != null ?
                new ResponseEntity<>(customer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<LedgerEntity> createLedger(@RequestBody LedgerEntity ledger) {
        LedgerEntity createdCustomer = ledgersService.createLedger(ledger);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LedgerEntity> updateLedger(@PathVariable Long id, @RequestBody LedgerEntity ledger) {
        LedgerEntity updatedCustomer = ledgersService.updateLedger(id, ledger);
        return updatedCustomer != null ?
                new ResponseEntity<>(updatedCustomer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        ledgersService.deleteLedger(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    
    @PostMapping("/report/expense")
    public ResponseEntity<List<LedgerDto>> getExpenseRpeort(@RequestBody FilterLedgerDto req) {
    	List<LedgerDto> createdCustomer = ledgersService.getReportExpense(req);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }
    
    @PostMapping("/report/pgm")
    public ResponseEntity<List<LedgerDto>> getPgmReport(@RequestBody FilterLedgerDto req) {
    	List<LedgerDto> createdCustomer = ledgersService.getReportPgm(req);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }
    
    @PostMapping("/report/dealer")
    public ResponseEntity<List<LedgerDto>> getDealerReport(@RequestBody FilterLedgerDto req) {
    	List<LedgerDto> createdCustomer = ledgersService.getReportDealer(req);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }
}
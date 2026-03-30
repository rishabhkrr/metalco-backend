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

import com.indona.invento.entities.SupplierEntity;
import com.indona.invento.services.SupplierService;

@RestController
@RequestMapping("/suppliers")
public class SuppliersController {

    @Autowired
    private SupplierService suppliersService;

    @GetMapping
    public List<SupplierEntity> getAllSuppliers() {
        return suppliersService.getAllSuppliers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierEntity> getSupplierById(@PathVariable Long id) {
        SupplierEntity customer = suppliersService.getSupplierById(id);
        return customer != null ?
                new ResponseEntity<>(customer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<SupplierEntity> createSupplier(@RequestBody SupplierEntity supplier) {
        SupplierEntity createdCustomer = suppliersService.createSupplier(supplier);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierEntity> updateSupplier(@PathVariable Long id, @RequestBody SupplierEntity supplier) {
        SupplierEntity updatedCustomer = suppliersService.updateSupplier(id, supplier);
        return updatedCustomer != null ?
                new ResponseEntity<>(updatedCustomer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        suppliersService.deleteSupplier(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
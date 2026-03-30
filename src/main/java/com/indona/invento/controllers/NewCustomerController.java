package com.indona.invento.controllers;


import com.indona.invento.dto.NewCustomerDto;
import com.indona.invento.entities.NewCustomerDetails;

import com.indona.invento.entities.NewSalesOrder;
import com.indona.invento.services.NewCustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/new-customer")
public class NewCustomerController {

    @Autowired
    private NewCustomerService service;

    // ✅ Create new customer
    @PostMapping("/create")
    public ResponseEntity<NewCustomerDetails> createCustomer(@RequestBody NewCustomerDto dto) {
        NewCustomerDetails saved = service.createNewCustomer(dto);
        return ResponseEntity.ok(saved);
    }

    // ✅ Get all customers
    @GetMapping("/all")
    public ResponseEntity<List<NewCustomerDetails>> getAllCustomers() {
        return ResponseEntity.ok(service.getAllCustomers());
    }


    // ✅ Get customer by ID
    @GetMapping("/{id}")
    public ResponseEntity<NewCustomerDetails> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCustomerById(id));
    }

    // ✅ Delete customer by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<NewCustomerDetails> deleteCustomer(@PathVariable Long id) {
        NewCustomerDetails deletedCustomer = service.deleteCustomer(id);
        return ResponseEntity.ok(deletedCustomer);
    }

    // ✅ Delete all customers
    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllCustomers() {
        try {
            service.deleteAllCustomers();
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ All customers deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "error", "❌ Failed to delete all customers",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<NewCustomerDetails> updateCustomer(@PathVariable Long id, @RequestBody NewCustomerDto dto) {
        NewCustomerDetails updated = service.updateCustomer(id, dto);
        return ResponseEntity.ok(updated);
    }

}

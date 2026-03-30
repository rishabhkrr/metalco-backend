package com.indona.invento.controllers;

import com.indona.invento.dto.CustomerRegistrationVerificationDto;
import com.indona.invento.entities.CustomerRegistrationVerificationEntity;
import com.indona.invento.services.CustomerRegistrationVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer-registration-verification")
public class CustomerRegistrationVerificationController {

    @Autowired
    private CustomerRegistrationVerificationService service;

    @PostMapping("/register")
    public ResponseEntity<CustomerRegistrationVerificationEntity> registerCustomer(
            @RequestBody CustomerRegistrationVerificationDto dto) {
        return ResponseEntity.ok(service.registerCustomer(dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<CustomerRegistrationVerificationEntity>> getAllCustomers() {
        return ResponseEntity.ok(service.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerRegistrationVerificationEntity> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCustomerById(id));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<CustomerRegistrationVerificationEntity> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerRegistrationVerificationDto dto) {
        return ResponseEntity.ok(service.updateCustomer(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCustomer(@PathVariable Long id) {
        service.deleteCustomer(id);
        return ResponseEntity.ok("Customer deleted successfully");
    }
}

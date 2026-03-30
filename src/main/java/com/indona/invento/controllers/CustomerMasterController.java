package com.indona.invento.controllers;

import com.indona.invento.dto.CustomerMasterDto;
import com.indona.invento.entities.CustomerMasterEntity;
import com.indona.invento.services.CustomerMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer-master")
public class CustomerMasterController {

    @Autowired
    private CustomerMasterService service;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CustomerMasterDto dto) {
        return ResponseEntity.ok(service.createCustomer(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody CustomerMasterDto dto) {
        return ResponseEntity.ok(service.updateCustomer(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteCustomer(@PathVariable Long id) {
        boolean deleted = service.deleteCustomer(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Customer deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Customer not found with ID: " + id));
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllCustomers() {
        try {
            List<CustomerMasterEntity> customers = service.getAllCustomersWithoutPagination();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getCustomerById(id));
    }

    @GetMapping("/checkName")
    public ResponseEntity<?> checkCustomerName(@RequestParam String name) {
        boolean exists = service.isCustomerNameExists(name);
        return ResponseEntity.ok(Map.of(
                "name", name,
                "exists", exists
        ));
    }

    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<CustomerMasterEntity>> getAllCustomersWithoutPagination() {
        try {
            List<CustomerMasterEntity> customers = service.getAllCustomersWithoutPagination();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveCustomer(@PathVariable Long id) {
        try {
            CustomerMasterEntity approvedCustomer = service.approveCustomer(id);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Customer approved successfully",
                    "id", approvedCustomer.getId(),
                    "customerCode", approvedCustomer.getCustomerCode(),
                    "customerName", approvedCustomer.getCustomerName(),
                    "status", approvedCustomer.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to approve customer",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectCustomer(@PathVariable Long id) {
        try {
            CustomerMasterEntity rejectedCustomer = service.rejectCustomer(id);
            return ResponseEntity.ok(Map.of(
                    "message", "❌ Customer rejected successfully",
                    "id", rejectedCustomer.getId(),
                    "customerCode", rejectedCustomer.getCustomerCode(),
                    "customerName", rejectedCustomer.getCustomerName(),
                    "status", rejectedCustomer.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to reject customer",
                    "details", e.getMessage()
            ));
        }
    }

}

package com.indona.invento.controllers;

import com.indona.invento.dto.UserMasterDto;
import com.indona.invento.entities.UserMasterEntity;
import com.indona.invento.services.UserMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user-master")
public class UserMasterController {

    @Autowired
    private UserMasterService service;

    @PostMapping("/create")
    public ResponseEntity<UserMasterEntity> create(@RequestBody UserMasterDto dto) {
        UserMasterEntity savedUser = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }


    @PutMapping("/update/{id}")
    public ResponseEntity<UserMasterEntity> update(@PathVariable Long id, @RequestBody UserMasterDto dto) {
        UserMasterEntity updatedUser = service.update(id, dto);
        return ResponseEntity.ok(updatedUser);
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserMasterEntity> users = service.getAllWithoutPagination();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserMasterEntity> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        UserMasterEntity deletedUser = service.delete(id);
        if (deletedUser != null) {
            return ResponseEntity.ok(deletedUser);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }
    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<UserMasterEntity>> getAllUsersWithoutPagination() {
        try {
            List<UserMasterEntity> users = service.getAllWithoutPagination();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        try {
            UserMasterEntity approvedUser = service.approveUser(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ User approved successfully",
                    "id", approvedUser.getId(),
                    "userName", approvedUser.getUserName(),
                    "status", approvedUser.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to approve user",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectUser(@PathVariable Long id) {
        try {
            UserMasterEntity rejectedUser = service.rejectUser(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "❌ User rejected successfully",
                    "id", rejectedUser.getId(),
                    "userName", rejectedUser.getUserName(),
                    "status", rejectedUser.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to reject user",
                    "details", e.getMessage()
            ));
        }
    }

}

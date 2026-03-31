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

import com.indona.invento.entities.UserRolesEntity;
import com.indona.invento.services.UserRolesService;

@RestController
@RequestMapping("/user-roles")
public class UserRolesController {

	@Autowired
    private UserRolesService userRolesService;

    @GetMapping
    public List<UserRolesEntity> getAllUserRoles() {
        return userRolesService.getAllUserRoles();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserRolesEntity> getUserRoleById(@PathVariable Long id) {
        UserRolesEntity userRole = userRolesService.getUserRoleById(id);
        return userRole != null ?
                new ResponseEntity<>(userRole, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<UserRolesEntity> createUserRole(@RequestBody UserRolesEntity userRole) {
        UserRolesEntity createdUserRole = userRolesService.createUserRole(userRole);
        return new ResponseEntity<>(createdUserRole, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserRolesEntity> updateUserRole(@PathVariable Long id, @RequestBody UserRolesEntity userRole) {
        UserRolesEntity updatedUserRole = userRolesService.updateUserRole(id, userRole);
        return updatedUserRole != null ?
                new ResponseEntity<>(updatedUserRole, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserRole(@PathVariable Long id) {
        userRolesService.deleteUserRole(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
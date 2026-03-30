/**
 * 
 */
package com.indona.invento.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.indona.invento.dto.ResponseDto;
import com.indona.invento.entities.UsersEntity;
import com.indona.invento.services.UsersService;
import com.indona.invento.util.ResponseUtil;
/**
 * 
 */

@RestController
@RequestMapping(path = "/users")
public class UsersController {
	
	@Autowired
    private UsersService userService;

    @GetMapping
    public List<UsersEntity> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsersEntity> getUserById(@PathVariable Long id) {
        UsersEntity user = userService.getUserById(id);
        return user != null ?
                new ResponseEntity<>(user, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<UsersEntity> createUser(@RequestBody UsersEntity user) {
    	UsersEntity createdUser = userService.createUser(user);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsersEntity> updateUser(@PathVariable Long id, @RequestBody UsersEntity user) {
    	UsersEntity updatedUser = userService.updateUser(id, user);
        return updatedUser != null ?
                new ResponseEntity<>(updatedUser, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    // Exception handling
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Void>> handleException(Exception ex) {
        return ResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}

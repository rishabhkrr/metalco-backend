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

import com.indona.invento.entities.SubCategoryEntity;
import com.indona.invento.services.SubCategoryService;

@RestController
@RequestMapping("/sub-category")
public class SubCategoryController {

    @Autowired
    private SubCategoryService binService;

    @GetMapping
    public List<SubCategoryEntity> getAllSubCategories() {
        return binService.getAllSubCategories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubCategoryEntity> getSubCategoryById(@PathVariable Long id) {
        SubCategoryEntity customer = binService.getSubCategoryById(id);
        return customer != null ?
                new ResponseEntity<>(customer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<SubCategoryEntity> createSubCategory(@RequestBody SubCategoryEntity customer) {
        SubCategoryEntity createdSubCategory = binService.createSubCategory(customer);
        return new ResponseEntity<>(createdSubCategory, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubCategoryEntity> updateSubCategory(@PathVariable Long id, @RequestBody SubCategoryEntity customer) {
        SubCategoryEntity updatedSubCategory = binService.updateSubCategory(id, customer);
        return updatedSubCategory != null ?
                new ResponseEntity<>(updatedSubCategory, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubCategory(@PathVariable Long id) {
        binService.deleteSubCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
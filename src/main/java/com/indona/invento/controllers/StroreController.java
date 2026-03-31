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

import com.indona.invento.entities.StoresEntity;
import com.indona.invento.services.StoresService;

@RestController
@RequestMapping("/store")
public class StroreController {

    @Autowired
    private StoresService storeService;

    @GetMapping
    public List<StoresEntity> getAllStores() {
        return storeService.getAllStores();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoresEntity> getStoreById(@PathVariable Long id) {
        StoresEntity defect = storeService.getStoreById(id);
        return defect != null ?
                new ResponseEntity<>(defect, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<StoresEntity> createStore(@RequestBody StoresEntity defect) {
        StoresEntity createdStore = storeService.createStore(defect);
        return new ResponseEntity<>(createdStore, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StoresEntity> updateStore(@PathVariable Long id, @RequestBody StoresEntity defect) {
        StoresEntity updatedStore = storeService.updateStore(id, defect);
        return updatedStore != null ?
                new ResponseEntity<>(updatedStore, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStore(@PathVariable Long id) {
        storeService.deleteStore(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
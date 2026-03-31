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

import com.indona.invento.entities.SkuEntity;
import com.indona.invento.services.SkuService;

@RestController
@RequestMapping("/sku")
public class SkusController {

    @Autowired
    private SkuService skuService;

    @GetMapping
    public List<SkuEntity> getAllSkus() {
        return skuService.getAllSkus();
    }

    @GetMapping("/{id}")
    public ResponseEntity<SkuEntity> getSkuById(@PathVariable Long id) {
        SkuEntity defect = skuService.getSkuById(id);
        return defect != null ?
                new ResponseEntity<>(defect, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<SkuEntity> createSku(@RequestBody SkuEntity defect) {
        SkuEntity createdSku = skuService.createSku(defect);
        return new ResponseEntity<>(createdSku, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SkuEntity> updateSku(@PathVariable Long id, @RequestBody SkuEntity defect) {
        SkuEntity updatedSku = skuService.updateSku(id, defect);
        return updatedSku != null ?
                new ResponseEntity<>(updatedSku, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSku(@PathVariable Long id) {
        skuService.deleteSku(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
package com.indona.invento.controllers;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.indona.invento.entities.AdjustmentEntity;
import com.indona.invento.services.AdjustmentService;

@RestController
@RequestMapping("/adjust")
public class AdjustmentController {

	@Autowired
    private AdjustmentService adjService;

    @GetMapping
    public List<AdjustmentEntity> getAllStockins() {
        return adjService.getAllAdjustment();
    }

    @GetMapping("/adjust/{id}")
    public List<AdjustmentEntity> getStockinByStore(@PathVariable Long id) {
        return adjService.getAllAdjustmentByStore(id);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AdjustmentEntity> getStockinById(@PathVariable Long id) {
        AdjustmentEntity line = adjService.getAdjustmentById(id);
        return line != null ?
                new ResponseEntity<>(line, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    
    @GetMapping("/transfer/{id}")
    public ResponseEntity<List<AdjustmentEntity>> getAdujstmentByTransferId(@PathVariable Long id) {
    	List<AdjustmentEntity> line = adjService.getAdjustmentByTransferId(id);
        return line != null ?
                new ResponseEntity<>(line, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<AdjustmentEntity> createStockin(@RequestBody AdjustmentEntity adj) {
        AdjustmentEntity createdStockin = adjService.createAdjustment(adj);
        return new ResponseEntity<>(createdStockin, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdjustmentEntity> updateStockin(@PathVariable Long id, @RequestBody AdjustmentEntity adj) {
        AdjustmentEntity updatedStockin = adjService.updateAdjustment(id, adj);
        return updatedStockin != null ?
                new ResponseEntity<>(updatedStockin, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}/{remark}")
    public ResponseEntity<Void> deleteStockin(@PathVariable Long id, @PathVariable String remark) {
    	adjService.deleteStockin(id, remark);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
}
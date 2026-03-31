package com.indona.invento.controllers;

import com.indona.invento.dto.PackingListJobWorkDTO;
import com.indona.invento.dto.PackingListJobWorkResponseDTO;
import com.indona.invento.entities.PackingListJobWorkEntity;
import com.indona.invento.services.PackingListJobWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/packing-list-jobwork")
public class PackingListJobWorkController {

    @Autowired
    private PackingListJobWorkService service;

    @PostMapping("/save")
    public ResponseEntity<List<PackingListJobWorkEntity>> save(@RequestBody List<PackingListJobWorkDTO> dtos) {
        List<PackingListJobWorkEntity> saved = service.savePackingList(dtos);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PackingListJobWorkEntity>> getAll() {
        return ResponseEntity.ok(service.getAllPackingLists());
    }
    
    // 1. Get all packing list numbers only
    @GetMapping("/numbers")
    public ResponseEntity<List<String>> getAllPackingListNumbers() {
        return ResponseEntity.ok(service.getAllPackingListNumbers());
    }

    // 2. Get full details by packing list number
    @GetMapping("/by-number")
    public ResponseEntity<List<PackingListJobWorkResponseDTO>> getByPackingListNumber(
            @RequestParam String packingListNumber) {
        return ResponseEntity.ok(service.getByPackingListNumber(packingListNumber));
    }

}

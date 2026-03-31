package com.indona.invento.controllers;

import com.indona.invento.dto.ProcessEntryDto;
import com.indona.invento.entities.ProcessEntryEntity;
import com.indona.invento.services.ProcessEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process-entry")
public class ProcessEntryController {

    @Autowired
    private ProcessEntryService service;

    @PostMapping("/add")
    public ResponseEntity<ProcessEntryEntity> addProcessEntry(@RequestBody ProcessEntryDto dto) {
        ProcessEntryEntity savedEntity = service.addEntry(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ProcessEntryEntity> updateProcessEntry(@PathVariable Long id, @RequestBody ProcessEntryDto dto) {
        try {
            ProcessEntryEntity updatedEntity = service.updateEntry(id, dto);
            return ResponseEntity.ok(updatedEntity);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }



    @GetMapping("/all")
    public ResponseEntity<?> getAllProcessEntries() {
        try {
            List<ProcessEntryEntity> entries = service.getAllEntriesWithoutPagination();
            return ResponseEntity.ok(entries);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ProcessEntryEntity> getProcessEntryById(@PathVariable Long id) {
        ProcessEntryEntity entry = service.getEntryById(id);
        if (entry != null) {
            return ResponseEntity.ok(entry);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteProcessEntry(@PathVariable Long id) {
        ProcessEntryEntity deletedEntity = service.deleteEntry(id);
        if (deletedEntity != null) {
            return ResponseEntity.ok(deletedEntity);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Process entry not found");
        }
    }


}

package com.indona.invento.controllers;

import com.indona.invento.entities.StorageAreaEntity;
import com.indona.invento.services.StorageAreaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/storage-areas")
public class StorageAreaController {

    private final StorageAreaService storageAreaService;

    public StorageAreaController(StorageAreaService storageAreaService) {
        this.storageAreaService = storageAreaService;
    }

    // POST API → Add new storage area
    @PostMapping
    public StorageAreaEntity addStorageArea(@RequestBody StorageAreaEntity storageArea) {
        return storageAreaService.addStorageArea(storageArea);
    }

    // GET ALL API → Fetch all storage areas
    @GetMapping
    public List<StorageAreaEntity> getAllStorageAreas() {
        return storageAreaService.getAllStorageAreas();
    }
}

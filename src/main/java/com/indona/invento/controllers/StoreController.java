package com.indona.invento.controllers;



import com.indona.invento.entities.StoreEntity;
import com.indona.invento.services.StoreService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    // POST API → Add new store
    @PostMapping
    public StoreEntity addStore(@RequestBody StoreEntity store) {
        return storeService.addStore(store);
    }

    // GET ALL API → Fetch all stores
    @GetMapping
    public List<StoreEntity> getAllStores() {
        return storeService.getAllStores();
    }
}


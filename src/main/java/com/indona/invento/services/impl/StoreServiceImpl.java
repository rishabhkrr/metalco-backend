package com.indona.invento.services.impl;

import com.indona.invento.dao.StoreRepository;
import com.indona.invento.entities.StoreEntity;
import com.indona.invento.services.StoreService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StoreServiceImpl implements StoreService {

    private static final Logger log = LoggerFactory.getLogger(StoreServiceImpl.class);

    private final StoreRepository storeRepository;

    // FRD-defined default stores
    private static final String[] DEFAULT_STORES = {
            "Warehouse",
            "Production",
            "Dispatch",
            "Loose Piece Storage",
            "End Piece Storage",
            "Rejection",
            "Cantilever Rack",
            "Scrap"
    };

    public StoreServiceImpl(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    @PostConstruct
    public void seedDefaultStores() {
        for (String storeName : DEFAULT_STORES) {
            if (!storeRepository.existsByStoreName(storeName)) {
                StoreEntity store = StoreEntity.builder()
                        .storeName(storeName)
                        .build();
                storeRepository.save(store);
                log.info("✅ Seeded default store: {}", storeName);
            }
        }
    }

    @Override
    public StoreEntity addStore(StoreEntity store) {
        return storeRepository.save(store);
    }

    @Override
    public List<StoreEntity> getAllStores() {
        return storeRepository.findAll();
    }
}

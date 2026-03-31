package com.indona.invento.services.impl;

import com.indona.invento.dao.StorageAreaRepository;
import com.indona.invento.entities.StorageAreaEntity;
import com.indona.invento.services.StorageAreaService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StorageAreaServiceImpl implements StorageAreaService {

    private static final Logger log = LoggerFactory.getLogger(StorageAreaServiceImpl.class);

    private final StorageAreaRepository storageAreaRepository;

    // FRD-defined default storage areas (exact order from spreadsheet)
    private static final String[][] DEFAULT_STORAGE_AREAS = {
            {"Common", "1"},
            {"Rejection", "2"},
            {"Cantilever Rack", "3"},
            {"Selective Rack", "4"},
            {"Piegion Rack", "5"},
            {"End Piece Storage", "6"},
            {"ALL", "7"}
    };

    public StorageAreaServiceImpl(StorageAreaRepository storageAreaRepository) {
        this.storageAreaRepository = storageAreaRepository;
    }

    @PostConstruct
    public void seedDefaultStorageAreas() {
        for (String[] entry : DEFAULT_STORAGE_AREAS) {
            String name = entry[0];
            int order = Integer.parseInt(entry[1]);

            if (!storageAreaRepository.existsByStorageAreaName(name)) {
                StorageAreaEntity area = StorageAreaEntity.builder()
                        .storageAreaName(name)
                        .storageAreaOrder(order)
                        .build();
                storageAreaRepository.save(area);
                log.info("✅ Seeded default storage area: {} (order: {})", name, order);
            }
        }
    }

    @Override
    public StorageAreaEntity addStorageArea(StorageAreaEntity storageArea) {
        return storageAreaRepository.save(storageArea);
    }

    @Override
    public List<StorageAreaEntity> getAllStorageAreas() {
        List<StorageAreaEntity> storageAreas = storageAreaRepository.findAll();
        return storageAreas;
    }
}

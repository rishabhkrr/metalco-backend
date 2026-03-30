package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.StoresEntity;

public interface StoresService {

	List<StoresEntity> getAllStores();
    StoresEntity getStoreById(Long id);
    StoresEntity createStore(StoresEntity aqlMapping);
    StoresEntity updateStore(Long id, StoresEntity aqlMapping);
    void deleteStore(Long id);
}

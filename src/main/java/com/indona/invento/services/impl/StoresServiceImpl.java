package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.StoresRepository;
import com.indona.invento.entities.StoresEntity;
import com.indona.invento.services.StoresService;

@Service
public class StoresServiceImpl implements StoresService {

	@Autowired
    private StoresRepository storesRepository;

    @Override
    public List<StoresEntity> getAllStores() {
        return storesRepository.findAll();
    }

    @Override
    public StoresEntity getStoreById(Long id) {
        return storesRepository.findById(id).orElse(null);
    }

    @Override
    public StoresEntity createStore(StoresEntity aqlMapping) {
        return storesRepository.save(aqlMapping);
    }

    @Override
    public StoresEntity updateStore(Long id, StoresEntity aqlMapping) {
        if (storesRepository.existsById(id)) {
            aqlMapping.setId(id);
            return storesRepository.save(aqlMapping);
        }
        return null; // Or throw an exception indicating AQL mapping not found
    }

    @Override
    public void deleteStore(Long id) {
        storesRepository.deleteById(id);
    }
    
}

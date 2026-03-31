package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.SkusRepository;
import com.indona.invento.entities.SkuEntity;
import com.indona.invento.services.SkuService;

@Service
public class SkuServiceImpl implements SkuService {

 	@Autowired
    private SkusRepository skuRepository;

    @Override
    public List<SkuEntity> getAllSkus() {
        return skuRepository.findAll();
    }

    @Override
    public SkuEntity getSkuById(Long id) {
        return skuRepository.findById(id).get();
    }

    @Override
    public SkuEntity createSku(SkuEntity defect) {
        return skuRepository.save(defect);
    }

    @Override
    public SkuEntity updateSku(Long id, SkuEntity defect) {
        if (skuRepository.existsById(id)) {
            defect.setId(id);
            return skuRepository.save(defect);
        }
        return null; // Or throw an exception indicating defect not found
    }

    @Override
    public void deleteSku(Long id) {
        skuRepository.deleteById(id);
    }
}

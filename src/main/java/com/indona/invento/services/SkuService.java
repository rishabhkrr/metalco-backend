package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.SkuEntity;

public interface SkuService {

	List<SkuEntity> getAllSkus();
    SkuEntity getSkuById(Long id);
    SkuEntity createSku(SkuEntity defect);
    SkuEntity updateSku(Long id, SkuEntity defect);
    void deleteSku(Long id);
}

package com.indona.invento.services.impl;

import com.indona.invento.dao.HsnCodeMasterRepository;
import com.indona.invento.dto.HsnCodeMasterDto;
import com.indona.invento.entities.HsnCodeMasterEntity;
import com.indona.invento.services.HsnCodeMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HsnCodeMasterServiceImpl implements HsnCodeMasterService {

    @Autowired
    private HsnCodeMasterRepository repository;

    @Override
    public HsnCodeMasterEntity createHsnCode(HsnCodeMasterDto dto) {
            // ✅ Check if HSN Code already exists
        if (repository.findByHsnCode(dto.getHsnCode()).isPresent()) {
            throw new RuntimeException("❌ HSN Code already exists: " + dto.getHsnCode());
        }

        HsnCodeMasterEntity entity = HsnCodeMasterEntity.builder()
                .materialType(dto.getMaterialType())
                .productCategory(dto.getProductCategory())
                .hsnCode(dto.getHsnCode())
                .description(dto.getDescription())
                .effectiveDate(dto.getEffectiveDate())
                .previousHsnCode(null)
                .gstRate(dto.getGstRate())
                .lastgstRate(dto.getLastgstRate())
                .gstEffectiveDate(dto.getGstEffectiveDate())
                .status("Pending")
                .build();
        return repository.save(entity);
    }

    @Override
    public HsnCodeMasterEntity updateHsnCode(Long id, HsnCodeMasterDto dto) {
        HsnCodeMasterEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HSN Code not found"));

        // ✅ Check if new HSN Code already exists (and it's not the current record)
        if (!entity.getHsnCode().equals(dto.getHsnCode())) {
            if (repository.findByHsnCode(dto.getHsnCode()).isPresent()) {
                throw new RuntimeException("❌ HSN Code already exists: " + dto.getHsnCode());
            }
        }

        entity.setPreviousHsnCode(dto.getPreviousHsnCode()); // Save old code
        entity.setHsnCode(dto.getHsnCode());
        entity.setDescription(dto.getDescription());
        entity.setEffectiveDate(dto.getEffectiveDate());
        entity.setMaterialType(dto.getMaterialType());
        entity.setProductCategory(dto.getProductCategory());
        entity.setGstRate(dto.getGstRate());
        entity.setStatus("PENDING");
        entity.setLastgstRate(dto.getLastgstRate());
        entity.setGstEffectiveDate(dto.getGstEffectiveDate());

        return repository.save(entity);
    }

    @Override
    public Page<HsnCodeMasterEntity> getAllHsnCodes(Pageable pageable) {
        return repository.findAll(pageable);
    }


    @Override
    public HsnCodeMasterEntity getHsnCodeById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HSN Code not found with ID: " + id));
    }

    @Override
    public HsnCodeMasterEntity deleteHsnCodeAndReturn(Long id) {
        HsnCodeMasterEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HSN Code not found with ID: " + id));

        repository.delete(entity);
        return entity;
    }

    @Override
    public List<HsnCodeMasterEntity> getAllHsnCodesWithoutPagination() {
        return repository.findAll();
    }

    @Override
    public HsnCodeMasterEntity approveHsnCode(Long id) throws Exception {
        HsnCodeMasterEntity hsnCode = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HSN Code not found with ID: " + id));
        hsnCode.setStatus("APPROVED");
        return repository.save(hsnCode);
    }

    @Override
    public HsnCodeMasterEntity rejectHsnCode(Long id) throws Exception {
        HsnCodeMasterEntity hsnCode = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("HSN Code not found with ID: " + id));
        hsnCode.setStatus("REJECTED");
        return repository.save(hsnCode);
    }

}

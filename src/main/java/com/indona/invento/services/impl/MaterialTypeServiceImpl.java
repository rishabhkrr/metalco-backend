package com.indona.invento.services.impl;

import com.indona.invento.dao.MaterialTypeRepository;
import com.indona.invento.dto.MaterialTypeDto;
import com.indona.invento.entities.MaterialTypeEntity;

import com.indona.invento.services.MaterialTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaterialTypeServiceImpl implements MaterialTypeService {

    @Autowired
    private MaterialTypeRepository repository;

    @Override
    public MaterialTypeEntity addMaterialType(MaterialTypeDto dto) {
        MaterialTypeEntity entity = MaterialTypeEntity.builder()
                .name(dto.getName())
                .build();
        return repository.save(entity);
    }

    @Override
    public List<MaterialTypeDto> getAllMaterialTypes() {
        return repository.findAll().stream()
                .map(entity -> MaterialTypeDto.builder()
                        .name(entity.getName())
                        .build())
                .toList();
    }
}

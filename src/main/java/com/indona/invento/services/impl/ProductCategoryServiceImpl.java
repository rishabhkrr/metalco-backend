package com.indona.invento.services.impl;

import com.indona.invento.dao.ProductCategoryRepository;
import com.indona.invento.dto.ProductCategoryDto;
import com.indona.invento.entities.ProductCategoryEntity;

import com.indona.invento.services.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

    @Autowired
    private ProductCategoryRepository repository;

    @Override
    public ProductCategoryEntity addCategory(ProductCategoryDto dto) {
        ProductCategoryEntity entity = ProductCategoryEntity.builder()
                .name(dto.getName())
                .build();
        return repository.save(entity);
    }

    @Override
    public List<ProductCategoryDto> getAllCategoryNames() {
        return repository.findAll().stream()
                .map(entity -> ProductCategoryDto.builder()
                        .name(entity.getName())
                        .build())
                .toList();
    }

}

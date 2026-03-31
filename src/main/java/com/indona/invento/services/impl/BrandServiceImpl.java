package com.indona.invento.services.impl;

import com.indona.invento.dao.BrandRepository;
import com.indona.invento.dto.BrandDto;
import com.indona.invento.entities.BrandEntity;
import com.indona.invento.services.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository repository;

    @Override
    public BrandEntity addBrand(BrandDto dto) {
        if (repository.existsByBrandName(dto.getBrandName())) {
            throw new RuntimeException("Brand already exists: " + dto.getBrandName());
        }

        BrandEntity entity = BrandEntity.builder()
                .brandName(dto.getBrandName())
                .build();

        return repository.save(entity);
    }

    @Override
    public List<BrandDto> getAllBrands() {
        return repository.findAll()
                .stream()
                .map(brand -> BrandDto.builder()
                        .brandName(brand.getBrandName())
                        .build())
                .collect(Collectors.toList());
    }

}


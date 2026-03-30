package com.indona.invento.services;

import com.indona.invento.dto.BrandDto;
import com.indona.invento.entities.BrandEntity;

import java.util.List;

public interface BrandService {
    BrandEntity addBrand(BrandDto dto);
    List<BrandDto> getAllBrands();
}

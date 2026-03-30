package com.indona.invento.services;

import com.indona.invento.dto.ProductCategoryDto;
import com.indona.invento.entities.ProductCategoryEntity;

import java.util.List;

public interface ProductCategoryService {
    ProductCategoryEntity addCategory(ProductCategoryDto dto);
    List<ProductCategoryDto> getAllCategoryNames();

}

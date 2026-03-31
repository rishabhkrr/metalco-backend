package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.CategoryEntity;

public interface CategoryService {

	List<CategoryEntity> getAllCategories();
    CategoryEntity getCategoryById(Long id);
    CategoryEntity createCategory(CategoryEntity customer);
    CategoryEntity updateCategory(Long id, CategoryEntity customer);
    void deleteCategory(Long id);
}

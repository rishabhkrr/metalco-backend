package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.CategoryRepository;
import com.indona.invento.entities.CategoryEntity;
import com.indona.invento.services.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
    private CategoryRepository categoriesRepository;

    @Override
    public List<CategoryEntity> getAllCategories() {
        return categoriesRepository.findAll();
    }

    @Override
    public CategoryEntity getCategoryById(Long id) {
        return categoriesRepository.findById(id).get();
    }

    @Override
    public CategoryEntity createCategory(CategoryEntity customer) {
        return categoriesRepository.save(customer);
    }

    @Override
    public CategoryEntity updateCategory(Long id, CategoryEntity customer) {
        if (categoriesRepository.existsById(id)) {
            customer.setId(id);
            return categoriesRepository.save(customer);
        }
        return null; // Or throw an exception indicating customer not found
    }

    @Override
    public void deleteCategory(Long id) {
        categoriesRepository.deleteById(id);
    }
}

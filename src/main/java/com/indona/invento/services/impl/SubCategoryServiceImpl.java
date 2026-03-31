package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.SubCategoryRepository;
import com.indona.invento.entities.SubCategoryEntity;
import com.indona.invento.services.SubCategoryService;

@Service
public class SubCategoryServiceImpl implements SubCategoryService {

	@Autowired
    private SubCategoryRepository categoriesRepository;

    @Override
    public List<SubCategoryEntity> getAllSubCategories() {
        return categoriesRepository.findAll();
    }

    @Override
    public SubCategoryEntity getSubCategoryById(Long id) {
        return categoriesRepository.findById(id).get();
    }

    @Override
    public SubCategoryEntity createSubCategory(SubCategoryEntity customer) {
        return categoriesRepository.save(customer);
    }

    @Override
    public SubCategoryEntity updateSubCategory(Long id, SubCategoryEntity customer) {
        if (categoriesRepository.existsById(id)) {
            customer.setId(id);
            return categoriesRepository.save(customer);
        }
        return null; // Or throw an exception indicating customer not found
    }

    @Override
    public void deleteSubCategory(Long id) {
        categoriesRepository.deleteById(id);
    }
}

package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.SubCategoryEntity;

public interface SubCategoryService {

	List<SubCategoryEntity> getAllSubCategories();
    SubCategoryEntity getSubCategoryById(Long id);
    SubCategoryEntity createSubCategory(SubCategoryEntity customer);
    SubCategoryEntity updateSubCategory(Long id, SubCategoryEntity customer);
    void deleteSubCategory(Long id);
}

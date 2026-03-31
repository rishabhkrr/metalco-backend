package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.ExpenseCategoryEntity;
import com.indona.invento.entities.ExpenseCategoryEntity;

public interface ExpenseCategoryService {

	List<ExpenseCategoryEntity> getAllExpenseCategorys();
	ExpenseCategoryEntity getExpenseCategoryById(Long id);
	ExpenseCategoryEntity createExpenseCategory(ExpenseCategoryEntity supplier);
	ExpenseCategoryEntity updateExpenseCategory(Long id, ExpenseCategoryEntity supplier);
    void deleteExpenseCategory(Long id);
	void processExcelData(List<ExcelRow> rows);
}

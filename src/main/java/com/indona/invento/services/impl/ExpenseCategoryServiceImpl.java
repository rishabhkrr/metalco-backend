package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.indona.invento.dao.ExpenseCategoryRepository;
import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.ExpenseCategoryEntity;
import com.indona.invento.services.ExpenseCategoryService;

import org.springframework.stereotype.Service;

@Service
public class ExpenseCategoryServiceImpl implements ExpenseCategoryService {

	@Autowired
    private ExpenseCategoryRepository expenseCatRepo;
	
	@Override
	public List<ExpenseCategoryEntity> getAllExpenseCategorys() {
		return expenseCatRepo.findAll();
	}

	@Override
	public ExpenseCategoryEntity getExpenseCategoryById(Long id) {
		return expenseCatRepo.findById(id).get();
	}

	@Override
	public ExpenseCategoryEntity createExpenseCategory(ExpenseCategoryEntity supplier) {
		return expenseCatRepo.save(supplier);
	}

	@Override
	public ExpenseCategoryEntity updateExpenseCategory(Long id, ExpenseCategoryEntity supplier) {
		if (expenseCatRepo.existsById(id)) {
			supplier.setId(id);
            return expenseCatRepo.save(supplier);
        }
        return null; // Or throw an exception indicating customer not found
	}

	@Override
	public void deleteExpenseCategory(Long id) {
		expenseCatRepo.deleteById(id);

	}
	
	 @Override
//   @Transactional
   public void processExcelData(List<ExcelRow> rows) {
   	Long count = 0L;
   	Long rowsSize = Long.valueOf(rows.size());
       for (ExcelRow row : rows) {
    	   ExpenseCategoryEntity cat = new ExpenseCategoryEntity();
    	   cat.setCatName(row.getSkuCode());
    	   expenseCatRepo.save(cat);
		   count++;
		   System.out.println("Saved:"+count+"/"+rowsSize);
       }
   }

}

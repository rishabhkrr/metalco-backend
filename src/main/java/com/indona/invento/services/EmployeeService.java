package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.EmployeeEntity;

public interface EmployeeService {

	List<EmployeeEntity> getAllEmployees();
	EmployeeEntity getEmployeeById(Long id);
	EmployeeEntity createEmployee(EmployeeEntity supplier);
	EmployeeEntity updateEmployee(Long id, EmployeeEntity supplier);
    void deleteEmployee(Long id);
	void processExcelData(List<ExcelRow> rows);
}

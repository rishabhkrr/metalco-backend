package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.indona.invento.dao.EmployeeRepository;
import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.EmployeeEntity;
import com.indona.invento.services.EmployeeService;

import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
    private EmployeeRepository employeeRepo;
	
	@Override
	public List<EmployeeEntity> getAllEmployees() {
		return employeeRepo.findAll();
	}

	@Override
	public EmployeeEntity getEmployeeById(Long id) {
		return employeeRepo.findById(id).get();
	}

	@Override
	public EmployeeEntity createEmployee(EmployeeEntity supplier) {
		return employeeRepo.save(supplier);
	}

	@Override
	public EmployeeEntity updateEmployee(Long id, EmployeeEntity supplier) {
		if (employeeRepo.existsById(id)) {
			supplier.setId(id);
            return employeeRepo.save(supplier);
        }
        return null; // Or throw an exception indicating customer not found
	}

	@Override
	public void deleteEmployee(Long id) {
		employeeRepo.deleteById(id);

	}
	
	 @Override
//   @Transactional
   public void processExcelData(List<ExcelRow> rows) {
   	Long count = 0L;
   	Long rowsSize = Long.valueOf(rows.size());
       for (ExcelRow row : rows) {
    	   EmployeeEntity employee = employeeRepo.findByEmployeeName(row.getSkuCode());
           if (employee == null) {
        	   employee = new EmployeeEntity();
        	   employee.setEmployeeName(row.getSkuCode());
        	   employee.setPhone(row.getSkuName());
			   employee.setStatus(1);
			   employee = employeeRepo.save(employee);
			} else {
			   employee.setPhone(row.getSkuName());
			   employee = employeeRepo.save(employee);
           }
		   count++;
		   System.out.println("Saved:"+count+"/"+rowsSize);
       }
   }

}

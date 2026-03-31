package com.indona.invento.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.ExpenseCategoryEntity;
import com.indona.invento.services.ExpenseCategoryService;

@RestController
@RequestMapping("/expense-category")
public class ExpenseCategoryController {

    @Autowired
    private ExpenseCategoryService expenseCategoryService;

    @GetMapping
    public List<ExpenseCategoryEntity> getAllExpenseCategorys() {
        return expenseCategoryService.getAllExpenseCategorys();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseCategoryEntity> getExpenseCategoryById(@PathVariable Long id) {
        ExpenseCategoryEntity customer = expenseCategoryService.getExpenseCategoryById(id);
        return customer != null ?
                new ResponseEntity<>(customer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<ExpenseCategoryEntity> createExpenseCategory(@RequestBody ExpenseCategoryEntity dealer) {
        ExpenseCategoryEntity createdCustomer = expenseCategoryService.createExpenseCategory(dealer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseCategoryEntity> updateExpenseCategory(@PathVariable Long id, @RequestBody ExpenseCategoryEntity dealer) {
        ExpenseCategoryEntity updatedCustomer = expenseCategoryService.updateExpenseCategory(id, dealer);
        return updatedCustomer != null ?
                new ResponseEntity<>(updatedCustomer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        expenseCategoryService.deleteExpenseCategory(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
        	XSSFWorkbook workbook = new XSSFWorkbook(is);
        	XSSFSheet sheet = workbook.getSheetAt(0);
        	
            List<ExcelRow> rows = new ArrayList<>();

            for(int i=0; i < sheet.getPhysicalNumberOfRows(); i++) {
            	XSSFRow row = sheet.getRow(i);
            	if (row.getRowNum() == 0) {
                    continue; // Skip header row
                }

            	if(row.getCell(0) != null) {
            		ExcelRow excelRow = new ExcelRow();
                    excelRow.setSkuCode(row.getCell(0).getStringCellValue());
                    rows.add(excelRow);
            	}
            }
            
            workbook.close();

            expenseCategoryService.processExcelData(rows);

            return new ResponseEntity<>("File uploaded and data processed successfully", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Error processing file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
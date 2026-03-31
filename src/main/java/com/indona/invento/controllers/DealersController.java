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
import com.indona.invento.entities.DealerEntity;
import com.indona.invento.services.DealerService;

@RestController
@RequestMapping("/dealers")
public class DealersController {

    @Autowired
    private DealerService dealersService;

    @GetMapping
    public List<DealerEntity> getAllDealers() {
        return dealersService.getAllDealers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealerEntity> getDealerById(@PathVariable Long id) {
        DealerEntity customer = dealersService.getDealerById(id);
        return customer != null ?
                new ResponseEntity<>(customer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping
    public ResponseEntity<DealerEntity> createDealer(@RequestBody DealerEntity dealer) {
        DealerEntity createdCustomer = dealersService.createDealer(dealer);
        return new ResponseEntity<>(createdCustomer, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DealerEntity> updateDealer(@PathVariable Long id, @RequestBody DealerEntity dealer) {
        DealerEntity updatedCustomer = dealersService.updateDealer(id, dealer);
        return updatedCustomer != null ?
                new ResponseEntity<>(updatedCustomer, HttpStatus.OK) :
                new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        dealersService.deleteDealer(id);
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

            	if(row.getCell(0) != null && row.getCell(1) != null) {
            		ExcelRow excelRow = new ExcelRow();
                    excelRow.setSkuCode(row.getCell(0).getStringCellValue());
                    excelRow.setSkuName(row.getCell(1).getStringCellValue());
                    excelRow.setServicePrice(row.getCell(2).getStringCellValue());
                    excelRow.setRetailPrice(row.getCell(3).getStringCellValue());
                    rows.add(excelRow);
            	}
            }
            
            workbook.close();

            dealersService.processExcelData(rows);

            return new ResponseEntity<>("File uploaded and data processed successfully", HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>("Error processing file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
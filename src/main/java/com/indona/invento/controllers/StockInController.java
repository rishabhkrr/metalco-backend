package com.indona.invento.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import io.swagger.annotations.ApiParam;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
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
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.dto.StockInReportDto;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.services.StockinService;

@RestController
@RequestMapping("/stock-in")
public class StockInController {

	@Autowired
	private StockinService stockinService;

	@GetMapping
	public List<StockinEntity> getAllStockins(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "id,desc") String[] sort,
			@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int deleteFlag) {
		// Determine the sorting direction and set up pagination
		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

		// Fetch and return the filtered stock-in data excluding damaged products
		return stockinService.getStockinExcludingDamaged(deleteFlag, search, pageable);
	}

	@GetMapping("/report")
	public List<StockInReportDto> getAllStockinsReport() {
		return stockinService.getAllStockinReport();
	}

	@GetMapping("/store/{id}")
	public List<StockinEntity> getStockinByStore(@PathVariable Long id, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size, @RequestParam(defaultValue = "id,desc") String[] sort,
			@RequestParam(required = false) String search) {

		Sort.Direction direction = Sort.Direction.fromString(sort[1]);
		Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort[0]));

		if (search != null && !search.isEmpty()) {
			return stockinService.searchStockinByStore(id, search, pageable);
		} else {
			return stockinService.getAllStockinByStore(id, pageable);
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<StockinEntity> getStockinById(@PathVariable Long id) {
		StockinEntity line = stockinService.getStockinById(id);
		return line != null ? new ResponseEntity<>(line, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping
	public ResponseEntity<StockinEntity> createStockin(@RequestBody StockinEntity line) {
		StockinEntity createdStockin = stockinService.createStockin(line);
		return new ResponseEntity<>(createdStockin, HttpStatus.OK);
	}

	@PutMapping("/{id}")
	public ResponseEntity<StockinEntity> updateStockin(@PathVariable Long id, @RequestBody StockinEntity line) {
		StockinEntity updatedStockin = stockinService.updateStockin(id, line);
		return updatedStockin != null ? new ResponseEntity<>(updatedStockin, HttpStatus.OK)
				: new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@DeleteMapping("/{id}/{remark}")
	public ResponseEntity<Void> deleteStockin(@PathVariable Long id, @PathVariable String remark) {
		stockinService.deleteStockin(id, remark);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
		try (InputStream is = file.getInputStream()) {
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0);

			List<ExcelRow> rows = new ArrayList<>();

			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				XSSFRow row = sheet.getRow(i);
				if (row.getRowNum() == 0) {
					continue; // Skip header row
				}

				if (row.getCell(0) != null && row.getCell(1) != null) {
					ExcelRow excelRow = new ExcelRow();
					excelRow.setCategoryId(row.getCell(0).getStringCellValue());
					excelRow.setSkuCode(row.getCell(1).getStringCellValue());
					excelRow.setSkuName(row.getCell(2).getStringCellValue());
					excelRow.setSkuQuantity(row.getCell(3).getStringCellValue());
					excelRow.setBinName(row.getCell(4).getStringCellValue());
//					excelRow.setServicePrice(row.getCell(5).getStringCellValue());
//					excelRow.setRetailPrice(row.getCell(6).getStringCellValue());
//					excelRow.setPgmPrice(row.getCell(7).getStringCellValue());
//					excelRow.setDealerPrice(row.getCell(8).getStringCellValue());
					rows.add(excelRow);
				}
			}

			workbook.close();

			stockinService.processExcelData(rows);

			return new ResponseEntity<>("File uploaded and data processed successfully", HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>("Error processing file", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/upload/warehouse")
	public ResponseEntity<String> uploadWarehouseFile(@RequestParam("file") MultipartFile file) {
		try (InputStream is = file.getInputStream()) {
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0);

			List<ExcelRow> rows = new ArrayList<>();

			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				XSSFRow row = sheet.getRow(i);
				if (row.getRowNum() == 0) {
					continue; // Skip header row
				}

				if (row.getCell(0) != null && row.getCell(1) != null) {
					ExcelRow excelRow = new ExcelRow();
					excelRow.setSkuCode(row.getCell(0).getStringCellValue());
					excelRow.setSkuName(row.getCell(1).getStringCellValue());
					excelRow.setSkuQuantity(row.getCell(2).getStringCellValue());
					excelRow.setBinName(row.getCell(3).getStringCellValue());
					rows.add(excelRow);
				}
			}

			workbook.close();

			stockinService.processExcelData(rows);

			return new ResponseEntity<>("File uploaded and data processed successfully", HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>("Error processing file", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/upload/price")
	public ResponseEntity<String> uploadPriceMaster(@RequestParam("file") MultipartFile file) {
		try (InputStream is = file.getInputStream()) {
			XSSFWorkbook workbook = new XSSFWorkbook(is);
			XSSFSheet sheet = workbook.getSheetAt(0);

			List<ExcelRow> rows = new ArrayList<>();

			for (int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
				XSSFRow row = sheet.getRow(i);
				if (row.getRowNum() == 0) {
					continue; // Skip header row
				}

				if (row.getCell(0) != null && row.getCell(1) != null) {
					ExcelRow excelRow = new ExcelRow();
					excelRow.setSkuCode(row.getCell(0).getStringCellValue());
					excelRow.setSkuName(row.getCell(1).getStringCellValue());
					excelRow.setDealerPrice(row.getCell(2).getStringCellValue());
					excelRow.setPgmPrice(row.getCell(3).getStringCellValue());
					excelRow.setRetailPrice(row.getCell(4).getStringCellValue());
					excelRow.setServicePrice(row.getCell(5).getStringCellValue());
					rows.add(excelRow);
				}
			}

			workbook.close();

			stockinService.processPriceExcelData(rows);

			return new ResponseEntity<>("File uploaded and data processed successfully", HttpStatus.OK);
		} catch (IOException e) {
			return new ResponseEntity<>("Error processing file", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
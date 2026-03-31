package com.indona.invento.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.dto.StockInReportDto;
import com.indona.invento.entities.StockinEntity;

public interface StockinService {
	List<StockinEntity> getAllStockin(String name, Pageable pageable);

	List<StockinEntity> getAllStockinByStore(Long storeId, Pageable pageable);

	StockinEntity getStockinById(Long id);

	StockinEntity createStockin(StockinEntity line);

	StockinEntity updateStockin(Long id, StockinEntity line);

	void deleteStockin(Long id, String remark);

	void processExcelData(List<ExcelRow> rows);

	List<StockinEntity> searchStockinByStore(Long storeId, String name, Pageable pageable);

	List<StockInReportDto> getAllStockinReport();

	// New method to fetch stock-in data excluding damaged products
	List<StockinEntity> getStockinExcludingDamaged(int deleteFlag, String search, Pageable pageable);

	void processPriceExcelData(List<ExcelRow> rows);
}

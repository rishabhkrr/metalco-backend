package com.indona.invento.services;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.indona.invento.dto.FilterDto;
import com.indona.invento.dto.InvoiceSummaryDto;
import com.indona.invento.dto.TransferSkuDto;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.entities.StockoutEntity;

public interface StockoutService {
	List<StockoutEntity> getAllStockout(String search, String type, Pageable pageable);
	TransferSkuDto getStockoutById(Long id);
	TransferSkuDto createStockout(TransferSkuDto line);
	TransferSkuDto updateStockout(Long id, TransferSkuDto line);
    void deleteStockout(Long id, String remark);
	List<StockoutEntity> getAllStockinByStore(Long id, String search, String type, Pageable pageable);
	InvoiceSummaryDto getInvoiceSummary(FilterDto req);
	InvoiceSummaryDto getOverallInvoiceSummary(FilterDto req);
	List<StockoutEntity> getAllStockinByStoreAndWarehouse(Long warehouseId, Long id, String search, String type,
			Pageable pageable);
	List<StockoutEntity> getAllStockoutReturns(String search, Pageable pageable);
	List<StockoutEntity> getAllStockoutReturnsStore(Long id, String search, Pageable pageable);
	List<StockoutEntity> getAllStockoutInvoices(Long id);
	List<InvoiceSummaryDto> getInvoiceSummaryDayWise(FilterDto req);
}

package com.indona.invento.services;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.indona.invento.dto.TransferSkuDto;
import com.indona.invento.entities.StockReturnEntity;

public interface StockReturnService {

	List<StockReturnEntity> getAllStockReturnReturns(String search, Pageable pageable);

	List<StockReturnEntity> getAllStockReturnInvoices(Long id);

	List<StockReturnEntity> getAllStockReturnReturnsStore(Long id, String search, Pageable pageable);

	List<StockReturnEntity> getAllStockReturn(String search, String type, Pageable pageable);

	List<StockReturnEntity> getAllStockinByStore(Long id, String search, String type, Pageable pageable);

	List<StockReturnEntity> getAllStockinByStoreAndWarehouse(Long warehouseId, Long id, String search, String type,
			Pageable pageable);

	TransferSkuDto getStockReturnById(Long id);

	TransferSkuDto createStockReturn(TransferSkuDto line);

	void deleteStockReturn(Long id, String remark);

	StockReturnEntity updateStockReturn(Long id, StockReturnEntity line);
	
}

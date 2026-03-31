package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.*;
import org.springframework.data.domain.Pageable;

import com.indona.invento.entities.StockTransferEntity;

public interface StockTransferService {

    List<StockTransferEntity> getAllStockTransfers(String search, Pageable pageable);
    TransferSkuDto getStockTransferById(Long id);
    TransferSkuDto createStockTransfer(TransferSkuDto department);
    TransferSkuDto updateStockTransfer(Long id, TransferSkuDto department);
    void deleteStockTransfer(Long id, String remark);
    List<StockTransferEntity> getStockTransferByStore(Long id, String search, Pageable pageable);
    List<StockTransferEntity> getStockTransferByWarehouse(Long id, String search, Pageable pageable);
    List<StockTransferEntity> getStockTransferByFromAndTo(Long id, String search, Pageable pageable);
     List<StockTransferWithLineItemsDto> getAllStockTransfersAll();
    List<StockTransferSummaryDto> getStockTransferSummaryDashboard();
    SaveStockTransferResponseDto saveStockTransfer(SaveStockTransferRequestDto request);
    void deleteAllStockTransfers();
}
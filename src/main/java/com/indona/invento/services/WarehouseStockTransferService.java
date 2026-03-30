package com.indona.invento.services;

import com.indona.invento.dto.WarehouseStockReturnRequestDTO;
import com.indona.invento.dto.WarehouseStockTransferRequestDTO;
import com.indona.invento.entities.StockSummaryEntity;
import com.indona.invento.entities.WarehouseStockTransferEntity;

import java.util.List;

public interface WarehouseStockTransferService {
    WarehouseStockTransferEntity processWarehouseTransfer(WarehouseStockTransferRequestDTO request);
    List<StockSummaryEntity> processReturn(WarehouseStockReturnRequestDTO request);
}


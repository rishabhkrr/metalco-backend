package com.indona.invento.services;


import com.indona.invento.dto.*;
import com.indona.invento.entities.MaterialRequestHeader;
import com.indona.invento.entities.SOSchedulePickListEntity;
import com.indona.invento.entities.StockTransferWHReturnEntity;
import com.indona.invento.entities.StockTransferWarehouseEntity;

import java.util.List;
import java.util.Map;

public interface MaterialRequestService {
    MaterialRequestHeader createMaterialRequest(MaterialRequestDTO dto);

    List<MaterialRequestSummaryResponseDTO> getAllSummaries();

    List<MaterialTransferScheduleDto> getAllSummaryFromMaterialRequest(String mrNumber, String lineNumber);

    List<Map<String, Object>> findPickListRackDetails(String unit, String itemDescription, String store);

    SOSchedulePickListEntity saveIUMaterial(SOSchedulePickListEntity entity);

    StockTransferWarehouseDto saveStockTransferWarehouse(StockTransferWarehouseDto dto);

    StockTransferWHReturnEntity saveStockTransfer(StockTransferWHReturnDto dto, Long SOSchedulePickListId);

    StockTransferWarehouseDto getStockTransferWarehouseById(Long id);

    SOSchedulePickListEntity getById(Long id);

    SOSchedulePickListEntity updateIUMaterial(Long id, SOSchedulePickListEntity updated);

    List<Map<String, Object>> getQrDetails(String mrNumber, String lineNumber, String itemDescription);

    void deleteAllMaterialRequests();

    Map<String, Object> generatePickListForIUMT(MaterialTransferScheduleDto dto);
}

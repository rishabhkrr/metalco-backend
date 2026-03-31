package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.AdjustmentEntity;
import com.indona.invento.entities.StockinEntity;

public interface AdjustmentService {
	List<AdjustmentEntity> getAllAdjustment();
	List<AdjustmentEntity> getAllAdjustmentByStore(Long storeId);
	AdjustmentEntity getAdjustmentById(Long id);
	AdjustmentEntity createAdjustment(AdjustmentEntity line);
	AdjustmentEntity updateAdjustment(Long id, AdjustmentEntity line);
    void deleteStockin(Long id, String remark);
	void processExcelData(List<ExcelRow> rows);
	List<AdjustmentEntity> getAdjustmentByTransferId(Long id);
}

package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.DealerEntity;
import com.indona.invento.entities.DealerEntity;

public interface DealerService {

	List<DealerEntity> getAllDealers();
	DealerEntity getDealerById(Long id);
	DealerEntity createDealer(DealerEntity supplier);
	DealerEntity updateDealer(Long id, DealerEntity supplier);
    void deleteDealer(Long id);
	void processExcelData(List<ExcelRow> rows);
}

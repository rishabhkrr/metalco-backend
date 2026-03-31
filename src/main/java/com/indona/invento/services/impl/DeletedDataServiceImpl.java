package com.indona.invento.services.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.StockTransferRepository;
import com.indona.invento.dao.StockinRepository;
import com.indona.invento.dao.StockoutRepository;
import com.indona.invento.dto.DeletedDataDto;
import com.indona.invento.entities.StockTransferEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.entities.StockoutEntity;
import com.indona.invento.services.DeletedDataService;

@Service
public class DeletedDataServiceImpl implements DeletedDataService {

	@Autowired
	private StockinRepository stockInRepo;
	
	@Autowired
	private StockoutRepository stockOutRepo;
	
	@Autowired
	private StockTransferRepository stockTrnRepo;
	
	@Override
	public List<DeletedDataDto> getAllData() {
		List<DeletedDataDto> data = new ArrayList<>();
		
		// Fetch the lists of entities marked for deletion
        List<StockinEntity> stockinList = stockInRepo.findByDeleteFlag(1);
        List<StockoutEntity> stockoutList = stockOutRepo.findByDeleteFlag(1);
        List<StockTransferEntity> stocktrnList = stockTrnRepo.findByDeleteFlag(1);

        // Map StockinEntity to DeletedDataDto and add to data list
        for (StockinEntity stockin : stockinList) {
            DeletedDataDto dto = new DeletedDataDto();
            dto.setRefNo(String.valueOf(stockin.getId()));
            dto.setType("stock-in");
            dto.setDate(stockin.getDateTime());
            dto.setStore(stockin.getStoreId());
            dto.setRemark(stockin.getDeleteRemark());
            data.add(dto);
        }

        // Map StockoutEntity to DeletedDataDto and add to data list
        for (StockoutEntity stockout : stockoutList) {
            DeletedDataDto dto = new DeletedDataDto();
            dto.setRefNo(String.valueOf(stockout.getId()));
            dto.setType("stock-out");
            dto.setDate(stockout.getDateTime());
            dto.setStore(stockout.getStoreId());
            dto.setRemark(stockout.getDeleteRemark());
            data.add(dto);
        }

        // Map StockTransferEntity to DeletedDataDto and add to data list
        for (StockTransferEntity stocktrn : stocktrnList) {
            DeletedDataDto dto = new DeletedDataDto();
            dto.setRefNo(stocktrn.getTransferNumber());
            dto.setType("stock-transfer");
            dto.setDate(stocktrn.getDateTime());
            dto.setStore(stocktrn.getFromStore());
            dto.setRemark(stocktrn.getDeleteRemark());
            data.add(dto);
        }
		
		return data;
	}

}

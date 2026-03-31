package com.indona.invento.services.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.AdjustmentRepository;
import com.indona.invento.dao.StockinRepository;
import com.indona.invento.dto.ExcelRow;
import com.indona.invento.entities.AdjustmentEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.services.AdjustmentService;

@Service
public class AdjustmentServiceImpl implements AdjustmentService {
	
	@Autowired
	private AdjustmentRepository adjustRepo;
	
	@Autowired
	private StockinRepository stockRepo;

	@Override
	public List<AdjustmentEntity> getAllAdjustment() {
		// TODO Auto-generated method stub
		return adjustRepo.findAllByOrderByDateTimeDesc();
	}

	@Override
	public List<AdjustmentEntity> getAllAdjustmentByStore(Long storeId) {
		// TODO Auto-generated method stub
		return adjustRepo.findByStoreIdOrderByDateTimeDesc(storeId);
	}

	@Override
	public AdjustmentEntity getAdjustmentById(Long id) {
		// TODO Auto-generated method stub
		return adjustRepo.getById(id);
	}

	@Override
	public AdjustmentEntity createAdjustment(AdjustmentEntity en) {
		// TODO Auto-generated method stub
		return adjustRepo.save(en);
	}

	@Override
	public AdjustmentEntity updateAdjustment(Long id, AdjustmentEntity en) {
		// TODO Auto-generated method stub
		if(adjustRepo.existsById(id)) {
			if(en.getStockId() != null) {
				if(en.getStatus().equals(1L) && stockRepo.existsById(en.getStockId())){
					Optional<StockinEntity> stock = stockRepo.findById(en.getStockId());
					stock.get().setSkuQuantity(stock.get().getSkuQuantity() + en.getQuantityDiff());
					stockRepo.save(stock.get());
				}
			}
			adjustRepo.save(en);
		}
		return en;
	}

	@Override
	public void deleteStockin(Long id, String remark) {
		// TODO Auto-generated method stub

	}

	@Override
	public void processExcelData(List<ExcelRow> rows) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<AdjustmentEntity> getAdjustmentByTransferId(Long id) {
		// TODO Auto-generated method stub
		return adjustRepo.findByRefNo(String.valueOf(id));
	}

}

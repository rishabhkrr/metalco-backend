package com.indona.invento.services.helpers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.indona.invento.dao.StockinRepository;
import com.indona.invento.dto.PicklistDto;
import com.indona.invento.entities.BinsEntity;
import com.indona.invento.entities.SkuEntity;
import com.indona.invento.entities.StockinEntity;

@Component
public class StockinServiceImplHelper {

	@Autowired
    private StockinRepository stockinRepository;
	
	public StockinEntity createStockin(StockinEntity stock) {
    	
        return stockinRepository.save(stock);
    }
	
	public StockinEntity createBinTransfer(StockinEntity stock) {
		List<StockinEntity> stockinEntries = stockinRepository.findBySkuIdAndStoreIdAndBinIdOrderByDateTimeAsc(stock.getSkuId(), stock.getStoreId(), stock.getPrevBinId());
		Long quantityRequested = Long.valueOf(stock.getSkuQuantity());
		
		for (StockinEntity stockin : stockinEntries) {
            if (Long.valueOf(quantityRequested) <= 0) {
                break;
            }
            

            Long availableQuantity = stockin.getSkuQuantity() - stockin.getSkuHold();

            if (quantityRequested > availableQuantity) {
                continue;
            }
            
            if (availableQuantity >= quantityRequested) {
            	stockin.setSkuQuantity(stockin.getSkuQuantity()-quantityRequested);
            	stockinRepository.save(stockin);
            	stockinRepository.save(stock);
            	break;
            }
        }
    	
    	// If we couldn't fulfill the requested quantity, throw an exception or handle it appropriately
        if (quantityRequested > 0) {
            throw new Error("Enough Quantity not available in the requested Bin");
            
        }
        return stockinRepository.save(stock);
    }
	
	public StockinEntity createStockTransfer(StockinEntity stock) {
		
	    return stockinRepository.save(stock);
	}
	
	public StockinEntity createStockReturn(StockinEntity stock) {
		
	    return stockinRepository.save(stock);
	}
	
	
}

package com.indona.invento.services.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.LedgerRepository;
import com.indona.invento.dao.PicklistRepository;
import com.indona.invento.dao.SkusRepository;
import com.indona.invento.dao.StockReturnRepository;
import com.indona.invento.dao.StockinRepository;
import com.indona.invento.dao.StockoutRepository;
import com.indona.invento.dao.TransferSkuDetailsRepository;
import com.indona.invento.dto.FilterDto;
import com.indona.invento.dto.InvoiceSummaryDto;
import com.indona.invento.dto.TransferSkuDetailsDto;
import com.indona.invento.dto.TransferSkuDto;
import com.indona.invento.entities.PicklistEntity;
import com.indona.invento.entities.SkuEntity;
import com.indona.invento.entities.StockReturnEntity;
import com.indona.invento.entities.StockTransferEntity;
import com.indona.invento.entities.StockTransferSkuEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.entities.StockoutEntity;
import com.indona.invento.services.StockReturnService;
import com.indona.invento.services.StockoutService;

@Service
public class StockReturnServiceImpl implements StockReturnService {

    @Autowired
    private StockReturnRepository stockReturnRepository;
    
    @Autowired
    private StockoutRepository stockOutRepository;
    
    @Autowired
    private PicklistRepository pickRepo;
	
	@Autowired
    private StockinRepository stockinRepo;
	
	@Autowired
    private SkusRepository skuRepo;
	
	@Autowired
	private LedgerRepository ledgerRepo;
	
	@Autowired
    private TransferSkuDetailsRepository skuTrnRepo;

	@Override
	public List<StockReturnEntity> getAllStockReturnReturns(String search, Pageable pageable) {
		return stockReturnRepository.findByDeleteFlagAndTransferStageOrderByDateTimeDesc(0,"returned", search, pageable);
	}

	@Override
	public List<StockReturnEntity> getAllStockReturnInvoices(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StockReturnEntity> getAllStockReturnReturnsStore(Long id, String search, Pageable pageable) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StockReturnEntity> getAllStockReturn(String search, String type, Pageable pageable) {
		if(type != null && type.equalsIgnoreCase("phone")) {
    		return stockReturnRepository.findByPhoneDeleteFlagOrderByDateTimeDesc(0, search, pageable);
    	} else {
    		return stockReturnRepository.findByDeleteFlagOrderByDateTimeDesc(0, search, pageable);
    	}
	}

	@Override
	public List<StockReturnEntity> getAllStockinByStore(Long id, String search, String type, Pageable pageable) {
		if (type != null && type.equalsIgnoreCase("phone")) {
			return stockReturnRepository.findByPhoneStoreIdAndDeleteFlagOrderByDateTimeDesc(id, 0, search, pageable);
		} else {
			return stockReturnRepository.findByStoreIdAndDeleteFlagOrderByDateTimeDesc(id, 0, search, pageable);
		}
	}

	@Override
	public List<StockReturnEntity> getAllStockinByStoreAndWarehouse(Long warehouseId, Long id, String search,
			String type, Pageable pageable) {
		if (type != null && type.equalsIgnoreCase("phone")) {
			return stockReturnRepository.findByPhoneStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(warehouseId, id, 0, search, pageable);
		} else {
			return stockReturnRepository.findByStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(warehouseId, id, 0, search, pageable);
		}
	}

	@Override
	public TransferSkuDto getStockReturnById(Long id) {
		StockReturnEntity st = stockReturnRepository.findById(id).get();
    	TransferSkuDto re = new TransferSkuDto();
    	re.setFromStore(st.getStoreId());
    	re.setCustomerName(st.getCustomerName());
    	re.setCustomerPhone(st.getCustomerPhone());
    	re.setTransferNumber(String.valueOf(st.getSeqNo()));
    	re.setAdvancePayment(st.getAdvancePayment());
    	re.setTransferStage(st.getTransferStage());
    	re.setTransferType("stock-out");
    	re.setPaymentStatus(st.getPaymentStatus());
    	re.setPaymentType(st.getPaymentType());
    	re.setCreditTerms(st.getCreditTerms());
    	re.setTransactionType(st.getTransactionType());
    	re.setTotalBill(st.getTotalBill());
    	re.setLabourCharges(st.getLabourCharges());
    	re.setLabourChargesDescp(st.getLabourChargesDescp());
    	re.setDiscountAmount(st.getDiscountAmount());
    	re.setTaxPercentage(st.getTaxPercentage());
    	re.setReturnType(st.getReturnType());
    	
    	List<StockTransferSkuEntity> skuEntities = skuTrnRepo.findAllByTransferNumber(re.getTransferNumber());
    	List<TransferSkuDetailsDto> skuList = new ArrayList<>();
    	
    	for(StockTransferSkuEntity en:skuEntities) {
    		TransferSkuDetailsDto rec = new TransferSkuDetailsDto();
    		rec.setTransferQuantity(en.getTransferQuantity());
    		rec.setTransferSkuCode(en.getTransferSkuCode());
    		rec.setTransferSkuName(en.getTransferSkuName());
    		rec.setSkuPrice(en.getSkuPrice());
    		rec.setSkuTotal(en.getSkuTotal());
    		rec.setDispatchQuantity(en.getDispatchQuantity());
    		rec.setReturnedQuantity(en.getReturnedQuantity());
    		rec.setRecievedQuantity(en.getRecievedQuantity());
    		rec.setPicked(en.getPicked());
    		skuList.add(rec);
    	}
    	
    	re.setTransferSkuList(skuList);
		return re;
	}

	@Override
	public TransferSkuDto createStockReturn(TransferSkuDto req) {
		StockReturnEntity st = new StockReturnEntity();
        
		// Get the list of TransferSKU objects
        List<TransferSkuDetailsDto> transferSkuList = req.getTransferSkuList();
        
        // Sum up the quantities of all SKUs in the list, converting String to Long
        long totalQuantity = transferSkuList.stream()
                                            .map(TransferSkuDetailsDto::getTransferQuantity)
                                            .mapToLong(Long::parseLong)
                                            .sum();
        
    	st.setStoreId(req.getFromStore());
    	st.setCustomerName(req.getCustomerName());
    	st.setCustomerPhone(req.getCustomerPhone());
    	st.setSkuQuantity(totalQuantity);
    	st.setTransferType("stock-out");
    	st.setTransactionType(req.getTransactionType());
    	st.setPaymentType(req.getPaymentType());
    	st.setCreditTerms(req.getCreditTerms());
    	st.setTotalBill(req.getTotalBill());
    	st.setLabourCharges(req.getLabourCharges());
    	st.setLabourChargesDescp(req.getLabourChargesDescp());
    	st.setAdvancePayment(req.getAdvancePayment());
    	st.setTransactionType(req.getTransactionType());
    	st.setPaymentStatus(req.getPaymentStatus());
    	st.setTransferStage(req.getTransferStage());
    	st.setDiscountAmount(req.getDiscountAmount());
    	st.setTaxPercentage(req.getTaxPercentage());
    	st.setRefNo(req.getTransferNumber());
    	st.setSeqNo(req.getSeqNo());
    	st.setReturnType(req.getReturnType());
    	st.setReturnDue(req.getReturnDue());
    	
    	StockReturnEntity ste = stockReturnRepository.save(st);
    	if(ste.getId() != null) {
    		for(TransferSkuDetailsDto item: transferSkuList) {
    			StockTransferSkuEntity sku = new StockTransferSkuEntity();
    			sku.setTransferNumber(String.valueOf(ste.getSeqNo()));
    			sku.setTransferQuantity(item.getTransferQuantity());
    			sku.setRecievedQuantity(item.getRecievedQuantity());
    			sku.setTransferSkuCode(item.getTransferSkuCode());
    			sku.setTransferSkuName(item.getTransferSkuName());
    			sku.setReturnedQuantity(item.getReturnedQuantity());
    			sku.setSkuPrice(item.getSkuPrice());
    			sku.setSkuTotal(item.getSkuTotal());
    			sku.setPicked(item.getPicked());
    			skuTrnRepo.save(sku);
    		}
    	}
        return req;
	}

	@Override
	public void deleteStockReturn(Long id, String remark) {
		StockReturnEntity stockOut = stockReturnRepository.findById(id).get();
	        if(stockReturnRepository.existsById(id)) {
	        	stockOut.setDeleteFlag(1);
	            stockOut.setDeleteRemark(remark);
	        	stockReturnRepository.save(stockOut);
	        }
	}

	@Override
	public StockReturnEntity updateStockReturn(Long id, StockReturnEntity req) {
		if (stockReturnRepository.existsById(id)) {
            StockReturnEntity st = new StockReturnEntity();
            st.setId(id);
            st.setStoreId(req.getStoreId());
        	st.setRefNo(req.getRefNo());
        	st.setReturnDue(req.getReturnDue());
        	st.setReturnType(req.getReturnType());
        	st.setCreatedBy(req.getCreatedBy());
        	st.setTransactionType(req.getTransactionType());
        	st.setSeqNo(req.getSeqNo());
        	st.setTransactionType(req.getTransactionType());
        	st.setTransferStage(req.getTransferStage());
        	st.setCreatedBy(req.getCreatedBy());
        	st.setDateTime(req.getDateTime());
        	
        	stockReturnRepository.save(st);
		}
		
		return req;
	}

	
}

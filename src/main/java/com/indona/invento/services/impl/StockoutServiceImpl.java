package com.indona.invento.services.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

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
import com.indona.invento.entities.StockTransferEntity;
import com.indona.invento.entities.StockTransferSkuEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.entities.StockoutEntity;
import com.indona.invento.services.StockoutService;

@Service
public class StockoutServiceImpl implements StockoutService {

    @Autowired
    private StockoutRepository stockoutRepository;
    
    @Autowired
    private StockReturnRepository stockReturnRepository;
    
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
	public List<StockoutEntity> getAllStockoutInvoices(Long id) {
		return stockoutRepository.getAllInvoices(id);
	}
	
    @Override
    public List<StockoutEntity> getAllStockoutReturns(String search, Pageable pageable) {
    	return stockoutRepository.findByDeleteFlagAndTransferStageOrderByDateTimeDesc(0,"returned", search, pageable);
    }
    
    @Override
    public List<StockoutEntity> getAllStockoutReturnsStore(Long storeId, String search, Pageable pageable) {
    	return stockoutRepository.findByDeleteFlagAndTransferStageAndStoreOrderByDateTimeDesc(0,"returned", storeId, search, pageable);
    }
    
    @Override
    public List<StockoutEntity> getAllStockout(String search, String type, Pageable pageable) {
    	if(type != null && type.equalsIgnoreCase("phone")) {
    		return stockoutRepository.findByPhoneDeleteFlagOrderByDateTimeDesc(0, search, pageable);
    	} else {
    		return stockoutRepository.findByDeleteFlagOrderByDateTimeDesc(0, search, pageable);
    	}
    }
    
    @Override
	public List<StockoutEntity> getAllStockinByStore(Long id, String search, String type, Pageable pageable) {
		if (type != null && type.equalsIgnoreCase("phone")) {
			return stockoutRepository.findByPhoneStoreIdAndDeleteFlagOrderByDateTimeDesc(id, 0, search, pageable);
		} else {
			return stockoutRepository.findByStoreIdAndDeleteFlagOrderByDateTimeDesc(id, 0, search, pageable);
		}
	}
    
    @Override
	public List<StockoutEntity> getAllStockinByStoreAndWarehouse(Long warehouseId, Long id, String search, String type,
			Pageable pageable) {
    	if (type != null && type.equalsIgnoreCase("phone")) {
			return stockoutRepository.findByPhoneStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(warehouseId, id, 0, search, pageable);
		} else {
			return stockoutRepository.findByStoreIdAndWarehouseIdDeleteFlagOrderByDateTimeDesc(warehouseId, id, 0, search, pageable);
		}
	}
    
    @Override
    public TransferSkuDto getStockoutById(Long id) {
    	StockoutEntity st = stockoutRepository.findById(id).get();
    	TransferSkuDto re = new TransferSkuDto();
    	re.setFromStore(st.getStoreId());
    	re.setCustomerName(st.getCustomerName());
    	re.setCustomerPhone(st.getCustomerPhone());
    	re.setTransferNumber(String.valueOf(st.getId()));
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
    	re.setInvoiceRemark(st.getInvoiceRemark());
    	
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
    		rec.setRecievedQuantity(en.getRecievedQuantity());
    		rec.setPicked(en.getPicked());
    		skuList.add(rec);
    	}
    	
    	re.setTransferSkuList(skuList);
        return re;
    }

    @Override
    public TransferSkuDto createStockout(TransferSkuDto req) {
    	StockoutEntity st = new StockoutEntity();
    	
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Cairo"));
        Date date = Date.from(now.toInstant());
        st.setDateTime(date);//common time zone
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
    	st.setInvoiceRemark(req.getInvoiceRemark());
    	
    	StockoutEntity ste = stockoutRepository.save(st);
    	if(ste.getId() != null) {
    		for(TransferSkuDetailsDto item: transferSkuList) {
    			StockTransferSkuEntity sku = new StockTransferSkuEntity();
    			sku.setTransferNumber(String.valueOf(ste.getId()));
    			sku.setTransferQuantity(item.getTransferQuantity());
    			sku.setTransferSkuCode(item.getTransferSkuCode());
    			sku.setTransferSkuName(item.getTransferSkuName());
    			sku.setSkuPrice(item.getSkuPrice());
    			sku.setSkuTotal(item.getSkuTotal());
    			sku.setPicked(item.getPicked());
    			skuTrnRepo.save(sku);
    		}
    	}
        return req;
    }

    @Override
    public TransferSkuDto updateStockout(Long id, TransferSkuDto req) {
    	if (stockoutRepository.existsById(id)) {
            StockoutEntity st = new StockoutEntity();
            
            StockoutEntity sto = stockoutRepository.findById(id).get();
            
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Cairo"));
            Date date = Date.from(now.toInstant());
            st.setDateTime(date);
        	// Get the list of TransferSKU objects
            List<TransferSkuDetailsDto> transferSkuList = req.getTransferSkuList();
            
            // Sum up the quantities of all SKUs in the list, converting String to Long
            long totalQuantity = transferSkuList.stream()
                                                .map(TransferSkuDetailsDto::getTransferQuantity)
                                                .mapToLong(Long::parseLong)
                                                .sum();
            st.setId(id);
            st.setStoreId(req.getFromStore());
        	st.setCustomerName(req.getCustomerName());
        	st.setCustomerPhone(req.getCustomerPhone());
        	st.setSkuQuantity(totalQuantity);
        	st.setTransferType(req.getTransactionType());
        	st.setPaymentType(req.getPaymentType());
        	st.setPaymentStatus(req.getPaymentStatus());
        	st.setCreditTerms(req.getCreditTerms());
        	st.setTransferStage(req.getTransferStage());
        	st.setAdvancePayment(req.getAdvancePayment());
        	st.setTransactionType(req.getTransactionType());
        	st.setTotalBill(req.getTotalBill());
        	st.setLabourCharges(req.getLabourCharges());
        	st.setLabourChargesDescp(req.getLabourChargesDescp());
        	st.setDiscountAmount(req.getDiscountAmount());
        	st.setTaxPercentage(req.getTaxPercentage());
        	st.setInvoiceRemark(req.getInvoiceRemark());
        	st.setProgressStatus(sto.getProgressStatus());
        	
        	StockoutEntity ste = stockoutRepository.save(st);
        	
        	Iterator<StockTransferSkuEntity> iterator = skuTrnRepo.findAllByTransferNumber(st.getId().toString()).iterator();

            // Iterate through the transferSkuList and remove elements not present in skuList
            List<TransferSkuDetailsDto> skuList = req.getTransferSkuList();
            while (iterator.hasNext()) {
            	StockTransferSkuEntity item = iterator.next();
                boolean found = false;
                
                for (TransferSkuDetailsDto sku : skuList) {
                    // Assuming a method to compare TransferSkuDetailsDto with StockTransferSkuEntity
                    if (item.getTransferSkuCode().equals(sku.getTransferSkuCode())) {
                        found = true;
                        break;
                    }
                }
                
                if (!found) {
                	skuTrnRepo.deleteByTransferNumberAndTransferSkuCode(st.getId().toString(), item.getTransferSkuCode());
                    iterator.remove();
                }
            }
            
        	if(ste.getId() != null && skuTrnRepo.existsByTransferNumber(String.valueOf(ste.getId()))) {
        		for(TransferSkuDetailsDto item: transferSkuList) {
                    StockTransferSkuEntity sku = skuTrnRepo.findByTransferNumberAndTransferSkuCode(st.getId().toString(), item.getTransferSkuCode());
                    if (sku == null) {
                        sku = new StockTransferSkuEntity();
                        sku.setTransferNumber(String.valueOf(ste.getId()));
            			sku.setTransferSkuCode(item.getTransferSkuCode());
            			sku.setTransferSkuName(item.getTransferSkuName());
            			sku.setRecievedQuantity(item.getRecievedQuantity());
                        sku.setDispatchQuantity(item.getDispatchQuantity()); 
            			sku.setSkuPrice(item.getSkuPrice());
            			sku.setSkuTotal(item.getSkuTotal());
            			sku.setPicked(item.getPicked());
                    }

                    // Update or save StockTransferSkuEntity
                    sku.setTransferQuantity(item.getTransferQuantity());
                    sku.setRecievedQuantity(item.getRecievedQuantity());
                    sku.setDispatchQuantity(item.getDispatchQuantity());  
                    sku.setSkuPrice(item.getSkuPrice());
        			sku.setSkuTotal(item.getSkuTotal());
                    skuTrnRepo.save(sku);
        		}
        	}
            
        	if (req.getTransferStage().equalsIgnoreCase("dispatched")) {
        	    String progressStatus = ste.getProgressStatus();
        	    if (progressStatus != null && (progressStatus.equalsIgnoreCase("dispatched") || progressStatus.equalsIgnoreCase("returned"))) {
        	        // Do nothing
        	    } else {
        	        updateStockTransfer(req);
        	        ste.setProgressStatus("dispatched");
        	        stockoutRepository.save(ste);
        	    }
        	}
            if(req.getTransferStage().equalsIgnoreCase("returned")){
            	String progressStatus = ste.getProgressStatus();
        	    if (progressStatus != null && (progressStatus.equalsIgnoreCase("returned"))) {
        	        // Do nothing
        	    } else {
        	        returnStockTransfer(req);
        	        ste.setProgressStatus("returned");
        	        stockoutRepository.save(ste);
        	    }
            	
            }
            return req;
        }
        return null; // Or throw an exception indicating department not found
    }

    @Override
    public void deleteStockout(Long id, String remark) {
        StockoutEntity stockOut = stockoutRepository.findById(id).get();
        if(stockoutRepository.existsById(id)) {
        	List<PicklistEntity> pickList = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(Long.valueOf(stockOut.getId()), Long.valueOf(stockOut.getStoreId()), "out");
    		if(pickList.size() > 0) {
            	for(PicklistEntity entity : pickList) {
            		if(entity.getStockInId() != null) {
            			SkuEntity sku = skuRepo.findById(entity.getSkuId()).get();
            			StockTransferSkuEntity skuTrn = skuTrnRepo.findByTransferNumberAndTransferSkuCode(String.valueOf(stockOut.getId()), sku.getSkuCode());
            			StockinEntity stockin = stockinRepo.getOne(entity.getStockInId());
            			if(stockin.getBinId() != null) {
            				if(stockOut.getTransferStage().equalsIgnoreCase("requested") || stockOut.getTransferStage().equalsIgnoreCase("picked") || stockOut.getTransferStage().equalsIgnoreCase("packed")) {
                				stockin.setSkuHold(stockin.getSkuHold()-Long.valueOf(skuTrn.getTransferQuantity()));
                			} else {
                				stockin.setSkuQuantity(stockin.getSkuQuantity()+Long.valueOf(skuTrn.getRecievedQuantity()));
                			}
                    		
                    		stockinRepo.save(stockin);
            			}
                		
                		stockinRepo.save(stockin);
            		}
            	}
            }
        	stockOut.setDeleteFlag(1);
            stockOut.setDeleteRemark(remark);
        	stockoutRepository.save(stockOut);
        }
    }
    
    private void updateStockTransfer(TransferSkuDto req) {
		List<PicklistEntity> pickList = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(Long.valueOf(req.getTransferNumber()), Long.valueOf(req.getFromStore()), "out");
		if(pickList.size() > 0) {
        	for(PicklistEntity entity : pickList) {
        		if(entity.getStockInId() != null) {
        			StockinEntity stockin = stockinRepo.getOne(entity.getStockInId());
            		stockin.setSkuHold(stockin.getSkuHold()-entity.getQuantity());
            		stockin.setSkuQuantity(stockin.getSkuQuantity()-entity.getQuantity());
            		stockinRepo.save(stockin);
        		}
        	}
        }
	}
    
    private void returnStockTransfer(TransferSkuDto req) {
		List<PicklistEntity> pickList = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(Long.valueOf(req.getTransferNumber()), Long.valueOf(req.getFromStore()), "out");
		if(pickList.size() > 0) {
        	for(PicklistEntity entity : pickList) {
        		if(entity.getStockInId() != null) {
        			SkuEntity sku = skuRepo.findById(entity.getSkuId()).get();
        			StockTransferSkuEntity skuTrn = skuTrnRepo.findByTransferNumberAndTransferSkuCode(req.getTransferNumber(), sku.getSkuCode());
        			StockinEntity stockin = stockinRepo.getOne(entity.getStockInId());
            		stockin.setSkuQuantity(stockin.getSkuQuantity()+(Long.valueOf(skuTrn.getDispatchQuantity()) - Long.valueOf(skuTrn.getRecievedQuantity())));
            		stockinRepo.save(stockin);
        		}
        	}
        }
	}
    
    @Override
    public List<InvoiceSummaryDto> getInvoiceSummaryDayWise(FilterDto req) {
    	List<InvoiceSummaryDto> invoiceSummaryList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("Africa/Bamako"));
        calendar.setTime(req.getFromDate());
        Date endDate = req.getToDate();

        while (!calendar.getTime().after(endDate)) {
        	// Set start time (00:00:00) for the current date
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            Date currentDateStart = calendar.getTime();

            // Set end time (23:59:59) for the current date
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            Date currentDateEnd = calendar.getTime();
            
            Long totalInvoice = getSafeValue(stockoutRepository.getTotalInvoice(currentDateStart, currentDateEnd, req.getStoreId()));
            Long receivedPayment = getSafeValue(stockoutRepository.getReceivedPayment(currentDateStart, currentDateEnd, req.getStoreId()));
            
            // Get Transfer numbers falling within the current date and storeId
            List<String> transferNumbers = stockReturnRepository.getTransferNumbers(currentDateStart, currentDateEnd, req.getStoreId());
            Long returnedPayment = getSafeValue(skuTrnRepo.getReturnedPayment(currentDateStart, currentDateEnd, transferNumbers));
            Long totalExpenses = getSafeValue(ledgerRepo.getTotalExpenses(currentDateStart, currentDateEnd, req.getStoreId(), "expense"));
            Long totalCredit = getSafeValue(ledgerRepo.getTotalCredit(currentDateStart, currentDateEnd, req.getStoreId()));
            Long totalDebit = getSafeValue(ledgerRepo.getTotalDebit(currentDateStart, currentDateEnd, req.getStoreId()));

            Long cashInHand = receivedPayment + totalDebit - returnedPayment - totalExpenses;

            InvoiceSummaryDto invoiceSummaryDto = new InvoiceSummaryDto(
            	currentDateStart,
                totalInvoice, 
                receivedPayment, 
                returnedPayment, 
                totalExpenses, 
                totalCredit, 
                totalDebit, 
                cashInHand
            );

            invoiceSummaryList.add(invoiceSummaryDto);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return invoiceSummaryList;
    }

	@Override
	public InvoiceSummaryDto getInvoiceSummary(FilterDto req) {
	    Long totalInvoice = getSafeValue(stockoutRepository.getTotalInvoice(req.getFromDate(), req.getToDate(), req.getStoreId()));
	    Long receivedPayment = getSafeValue(stockoutRepository.getReceivedPayment(req.getFromDate(), req.getToDate(), req.getStoreId()));
	    
	    // Get Trnafer numbers falling within the date range and storeId
	    List<String> transferNumbers = stockReturnRepository.getTransferNumbers(req.getFromDate(), req.getToDate(), req.getStoreId());
	    Long returnedPayment = getSafeValue(skuTrnRepo.getReturnedPayment(req.getFromDate(), req.getToDate(), transferNumbers));
	    Long totalExpenses = getSafeValue(ledgerRepo.getTotalExpenses(req.getFromDate(), req.getToDate(), req.getStoreId(), "expense"));
	    Long totalCredit = getSafeValue(ledgerRepo.getTotalCredit(req.getFromDate(), req.getToDate(), req.getStoreId()));
	    Long totalDebit = getSafeValue(ledgerRepo.getTotalDebit(req.getFromDate(), req.getToDate(), req.getStoreId()));

	    Long cashInHand = receivedPayment + totalDebit - returnedPayment - totalExpenses;

	    return new InvoiceSummaryDto(req.getFromDate(), totalInvoice, receivedPayment, returnedPayment, totalExpenses, totalCredit, totalDebit, cashInHand);
	}
	
	@Override
	public InvoiceSummaryDto getOverallInvoiceSummary(FilterDto req) {
	    Long totalInvoice = getSafeValue(stockoutRepository.getTotalInvoiceOverall(req.getFromDate(), req.getToDate()));
	    Long receivedPaymentTmp = getSafeValue(stockoutRepository.getReceivedPaymentOverall(req.getFromDate(), req.getToDate()));
	    
	    // Get Trnafer numbers falling within the date range and storeId
	    List<String> transferNumbers = stockReturnRepository.getTransferNumbersOverall(req.getFromDate(), req.getToDate());
	    Long returnedPayment = getSafeValue(skuTrnRepo.getReturnedPaymentOverall(req.getFromDate(), req.getToDate(), transferNumbers));
	    Long totalExpenses = getSafeValue(ledgerRepo.getTotalExpensesOverall(req.getFromDate(), req.getToDate(), "expense"));
	    Long totalCredit = getSafeValue(ledgerRepo.getTotalCreditOverall(req.getFromDate(), req.getToDate()));
	    Long totalDebit = getSafeValue(ledgerRepo.getTotalDebitOverall(req.getFromDate(), req.getToDate()));
	    Long receivedPayment = receivedPaymentTmp + returnedPayment - totalCredit + totalExpenses;
	    Long cashInHand = receivedPayment + totalDebit - returnedPayment - totalExpenses;

	    return new InvoiceSummaryDto(req.getFromDate(), totalInvoice, receivedPayment, returnedPayment, totalExpenses, totalCredit, totalDebit, cashInHand);
	}

	private Long getSafeValue(Long value) {
	    return Optional.ofNullable(value).orElse(0L);
	}

}

package com.indona.invento.services.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.BinsRepository;
import com.indona.invento.dao.PicklistRepository;
import com.indona.invento.dao.SkusRepository;
import com.indona.invento.dao.StockinRepository;
import com.indona.invento.dao.StockoutRepository;
import com.indona.invento.dao.TransferSkuDetailsRepository;
import com.indona.invento.dto.PicklistDto;
import com.indona.invento.dto.PicklistRequestDto;
import com.indona.invento.entities.BinsEntity;
import com.indona.invento.entities.PicklistEntity;
import com.indona.invento.entities.SkuEntity;
import com.indona.invento.entities.StockTransferEntity;
import com.indona.invento.entities.StockTransferSkuEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.entities.StockoutEntity;
import com.indona.invento.services.PicklistService;

import jakarta.transaction.Transactional;

@Service
public class PicklistServiceImpl implements PicklistService {

	@Autowired 
	private SkusRepository skuRepo;
	
	@Autowired 
	private StockinRepository stockinRepo;
	
	@Autowired 
	private StockoutRepository stockoutRepo;
	
	@Autowired
	private BinsRepository binRepo;
	
	@Autowired 
	private PicklistRepository pickRepo;
	
	@Autowired 
	private TransferSkuDetailsRepository trnSkuRepo;
	
	@Override
	@Transactional
	public List<PicklistDto> generateSOPicklist(PicklistRequestDto req, List<StockTransferSkuEntity> skus) {
		List<PicklistDto> picklist = new ArrayList<>();
//		String skuCode = req.getSkuId();
        Long storeId = Long.valueOf(req.getStoreId());
        Long refNo = Long.valueOf(req.getRefNo());
        List<StockTransferSkuEntity> skusNew = new ArrayList<>();
        skusNew.addAll(skus);
        
        List<PicklistEntity> pick = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(refNo, storeId, "out");
        for (StockTransferSkuEntity sku : skus) {
			if (pick.size() > 0) {
				for (PicklistEntity entity : pick) {
					Long skuId = skuRepo.findBySkuCode(sku.getTransferSkuCode()).getId();
					if (entity.getSkuId().equals(skuId)) {
						skusNew.remove(skusNew.indexOf(sku));
						break;
					}
				}
			}
		}
        
        if(skusNew.size() > 0) {
        	picklistGenrate(skusNew, storeId, refNo, req);
        }
        
        List<PicklistEntity> pickNew = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(refNo, storeId, "out");
        if(pickNew.size() > 0) {
        	for(PicklistEntity entity : pickNew) {
        		PicklistDto picklistDto = new PicklistDto();
        		SkuEntity sku = skuRepo.getOne(entity.getSkuId());
        		StockTransferSkuEntity trnEntity = trnSkuRepo.findByTransferNumberAndTransferSkuCode(req.getRefNo(), sku.getSkuCode());
				if (entity.getBinId() == null && entity.getStockInId() == null) {
		    		Long skuId = Long.valueOf(sku.getId());
		    		Long quantityRequested = entity.getQuantity();
		    		Long remainingQuantity = entity.getQuantity();
		        	// Retrieve stock-in entries for the SKU, ordered by the date they were stocked in
		            List<StockinEntity> stockinEntries = stockinRepo.findBySkuIdAndStoreIdAndDeleteFlagOrderByDateTimeAsc(skuId, storeId, 0);
		        
		        	for (StockinEntity stockin : stockinEntries) {
		                if (Long.valueOf(remainingQuantity) <= 0) {
		                    break;
		                }

		                Long availableQuantity = stockin.getSkuQuantity() - stockin.getSkuHold();

		                if (availableQuantity > 0) {
		                	Long quantityToPick = Math.min(availableQuantity, remainingQuantity);
		                    picklistDto.setSkuCode(sku.getSkuCode());
		                    picklistDto.setSkuName(skuRepo.getOne(skuId).getSkuName());
		                    
		                    if(stockin.getBinId() == null) {
		                    	continue;
		                    }
		                    BinsEntity bin = binRepo.getOne(stockin.getBinId());
		                    picklistDto.setBinName(bin.getBinName());
		                    picklistDto.setLocation(bin.getBinLocation());
		                    picklistDto.setQuantity(quantityToPick);
		                    picklistDto.setOrderedQuantity(quantityRequested);
		                    picklistDto.setPicked("To be picked");
		                    picklist.add(picklistDto);
		                    updatePickEntry(entity.getId(), picklistDto, stockin.getId(), bin.getId(), storeId, skuId, refNo, "out");
		                    // Update the stock-in quantity
		                    stockin.setSkuHold(stockin.getSkuHold()+quantityToPick);
		                    stockinRepo.save(stockin);

		                    remainingQuantity -= quantityToPick;
		                }
		            }
		        	
		        	// If we couldn't fulfill the requested quantity, throw an exception or handle it appropriately
		            if (remainingQuantity > 0) {
		                picklistDto.setSkuCode(sku.getSkuCode());
		                picklistDto.setSkuName(sku.getSkuName());
		                picklistDto.setOrderedQuantity(quantityRequested);
		                picklistDto.setBinName("N/A");
		                picklistDto.setLocation("N/A");
		                picklistDto.setPicked("Picked");
		                picklistDto.setQuantity(0L);
		                
		                if(remainingQuantity != quantityRequested) {
		                	createPickEntry(picklistDto, null, null, storeId, skuId, refNo, "out");
		                }
		                
		                picklist.add(picklistDto);
		                
		            }
		            
		            trnEntity.setDispatchQuantity(String.valueOf(Long.valueOf(trnEntity.getTransferQuantity())-remainingQuantity));
		            trnEntity.setHoldQuantity(trnEntity.getDispatchQuantity());
		            trnEntity.setRecievedQuantity(trnEntity.getDispatchQuantity());
		            trnEntity.setPicked("true");
		            trnEntity.setId(trnEntity.getId());
		            trnSkuRepo.save(trnEntity);
				} else {
					picklistDto.setSkuCode(sku.getSkuCode());
	                picklistDto.setSkuName(sku.getSkuName());
	                BinsEntity bin = new BinsEntity();
	                if(entity.getBinId() != null) {
	                	bin = binRepo.getOne(entity.getBinId());
	                	picklistDto.setQuantity(entity.getQuantity());
	                } else {
	                	bin.setBinLocation("N/A");
	                	bin.setBinName("N/A");
	                	picklistDto.setQuantity(0L);
	                }
	                picklistDto.setBinName(bin.getBinName());
	                picklistDto.setLocation(bin.getBinLocation());
	                picklistDto.setOrderedQuantity(entity.getQuantity());
	                if(entity.getPicked() != null) {
	                	picklistDto.setPicked((entity.getPicked().equalsIgnoreCase("true")) ? "Picked":"To be picked");
	                	if (entity.getPicked().equalsIgnoreCase("false")) {
//	    					StockTransferSkuEntity trnEntity = trnSkuRepo.findByTransferNumberAndTransferSkuCode(req.getRefNo(), sku.getSkuCode());
	    	                trnEntity.setPicked("true");
	    	                trnSkuRepo.save(trnEntity);
	    	                
	    	                entity.setPicked("true");
	    	                pickRepo.save(entity);
	    				}
	                } else {
	                	picklistDto.setPicked("false");
	                }
	                
	                picklist.add(picklistDto);
				}
                
        	}
        }

        return picklist;
	}
	
	@Override
	@Transactional
	public List<PicklistDto> generateSOReturn(PicklistRequestDto req, List<StockTransferSkuEntity> skus) {
		List<PicklistDto> picklist = new ArrayList<>();
//		String skuCode = req.getSkuId();
        Long storeId = Long.valueOf(req.getStoreId());
        Long refNo = Long.valueOf(req.getRefNo());

        
        List<PicklistEntity> pick = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(refNo, storeId, "out");
        if(pick.size() > 0) {
        	for(PicklistEntity entity : pick) {
        		
        		PicklistDto picklistDto = new PicklistDto();
        		SkuEntity sku = skuRepo.getOne(entity.getSkuId());
                picklistDto.setSkuCode(sku.getSkuCode());
                picklistDto.setSkuName(sku.getSkuName());
                BinsEntity bin = new BinsEntity();
                if(entity.getBinId() != null) {
                	bin = binRepo.getOne(entity.getBinId());
                } else {
                	bin.setBinLocation("N/A");
                	bin.setBinName("N/A");
                }
                picklistDto.setBinName(bin.getBinName());
                picklistDto.setLocation(bin.getBinLocation());
                picklistDto.setQuantity(entity.getQuantity());
                picklist.add(picklistDto);
        	}
        } else {
        	for(StockTransferSkuEntity sku: skus) {
        		Long quantityRequested = Long.valueOf(sku.getTransferQuantity());
                Long remainingQuantity = quantityRequested;
                SkuEntity skuEntity = skuRepo.findBySkuCode(sku.getTransferSkuCode());
        		Long skuId = Long.valueOf(skuEntity.getId());
	        	// Retrieve stock-in entries for the SKU, ordered by the date they were stocked in
	            List<StockinEntity> stockinEntries = stockinRepo.findBySkuIdAndStoreIdAndDeleteFlagOrderByDateTimeAsc(skuId, storeId, 0);
            
            
            	
            	for (StockinEntity stockin : stockinEntries) {
                    if (Long.valueOf(remainingQuantity) <= 0) {
                        break;
                    }

                    Long availableQuantity = stockin.getSkuQuantity() - stockin.getSkuHold();

                    if (availableQuantity > 0) {
                    	Long quantityToPick = Math.min(availableQuantity, remainingQuantity);
                        PicklistDto picklistDto = new PicklistDto();
                        picklistDto.setSkuCode(sku.getTransferSkuCode());
                        picklistDto.setSkuName(skuRepo.getOne(skuId).getSkuName());
                        
                        if(stockin.getBinId() == null) {
                        	continue;
                        }
                        BinsEntity bin = binRepo.getOne(stockin.getBinId());
                        picklistDto.setBinName(bin.getBinName());
                        picklistDto.setLocation(bin.getBinLocation());
                        picklistDto.setQuantity(quantityToPick);
                        picklistDto.setOrderedQuantity(quantityRequested);
                        picklist.add(picklistDto);
                        createPickEntry(picklistDto, stockin.getId(), bin.getId(), storeId, skuId, refNo, "out");
                        // Update the stock-in quantity
                        stockin.setSkuHold(stockin.getSkuHold()+quantityToPick);
                        stockinRepo.save(stockin);

                        // Add a record to the stock-out repository
//                        StockoutEntity stockout = new StockoutEntity();
//                        stockout.setCustomerName("Stock Transfer");
//                        stockout.setSkuId(skuId);
//                        stockout.setStoreId(storeId);
//                        stockout.setBinId(stockin.getBinId());
//                        stockout.setSkuQuantity(quantityToPick);
//                        stockout.setTransferStage("internal");
//                        stockout.setDateTime(new Date());
//                        stockoutRepo.save(stockout);

                        remainingQuantity -= quantityToPick;
                    }
                }
            	
            	// If we couldn't fulfill the requested quantity, throw an exception or handle it appropriately
                if (remainingQuantity > 0) {
//                    throw new Error("Error *"+skuEntity.getSkuName()+":"+remainingQuantity+"*");
                	PicklistDto picklistDto = new PicklistDto();
                    picklistDto.setSkuCode(sku.getTransferSkuCode());
                    picklistDto.setSkuName(skuEntity.getSkuName());
                    picklistDto.setOrderedQuantity(quantityRequested);
                    picklistDto.setBinName("N/A");
                    picklistDto.setLocation("N/A");
                    picklistDto.setQuantity(0L);
                    
                    picklist.add(picklistDto);
                    
                    createPickEntry(picklistDto, null, null, storeId, skuId, refNo, "out");
                    
                }
                
                StockTransferSkuEntity trnEntity = trnSkuRepo.findByTransferNumberAndTransferSkuCode(req.getRefNo(), skuEntity.getSkuCode());
                trnEntity.setDispatchQuantity(String.valueOf(Long.valueOf(trnEntity.getTransferQuantity())-remainingQuantity));
                trnEntity.setRecievedQuantity(trnEntity.getDispatchQuantity());
                trnEntity.setId(trnEntity.getId());
                trnSkuRepo.save(trnEntity);

            }
        }

        return picklist;
	}

	@Override
	public List<PicklistDto> generateSILocation(PicklistRequestDto req) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<PicklistDto> picklistGenrate(List<StockTransferSkuEntity> skus, Long storeId, Long refNo, PicklistRequestDto req) {
		List<PicklistDto> picklist = new ArrayList<>();
		for(StockTransferSkuEntity sku: skus) {
    		Long quantityRequested = Long.valueOf(sku.getTransferQuantity());
            Long remainingQuantity = quantityRequested;
            SkuEntity skuEntity = skuRepo.findBySkuCode(sku.getTransferSkuCode());
    		Long skuId = Long.valueOf(skuEntity.getId());
        	// Retrieve stock-in entries for the SKU, ordered by the date they were stocked in
            List<StockinEntity> stockinEntries = stockinRepo.findBySkuIdAndStoreIdAndDeleteFlagOrderByDateTimeAsc(skuId, storeId, 0);
        
        
        	
        	for (StockinEntity stockin : stockinEntries) {
                if (Long.valueOf(remainingQuantity) <= 0) {
                    break;
                }
//                System.out.println(stockin.getId() + "=> Qty: "+stockin.getSkuQuantity() + " Hold:"+stockin.getSkuHold());
                Long availableQuantity = stockin.getSkuQuantity() - stockin.getSkuHold();

                if (availableQuantity > 0) {
                	Long quantityToPick = Math.min(availableQuantity, remainingQuantity);
                    PicklistDto picklistDto = new PicklistDto();
                    picklistDto.setSkuCode(sku.getTransferSkuCode());
                    picklistDto.setSkuName(skuRepo.getOne(skuId).getSkuName());
                    
                    if(stockin.getBinId() == null) {
                    	continue;
                    }
                    BinsEntity bin = binRepo.getOne(stockin.getBinId());
                    picklistDto.setBinName(bin.getBinName());
                    picklistDto.setLocation(bin.getBinLocation());
                    picklistDto.setQuantity(quantityToPick);
                    picklistDto.setOrderedQuantity(quantityRequested);
                    picklistDto.setPicked(sku.getPicked() == "true" ? "true":"false");
                    picklist.add(picklistDto);
                    createPickEntry(picklistDto, stockin.getId(), bin.getId(), storeId, skuId, refNo, "out");
                    // Update the stock-in quantity
                    stockin.setSkuHold(stockin.getSkuHold()+quantityToPick);
                    stockinRepo.save(stockin);

                    remainingQuantity -= quantityToPick;
                }
            }
        	
        	// If we couldn't fulfill the requested quantity, throw an exception or handle it appropriately
            if (remainingQuantity > 0) {
            	PicklistDto picklistDto = new PicklistDto();
                picklistDto.setSkuCode(sku.getTransferSkuCode());
                picklistDto.setSkuName(skuEntity.getSkuName());
                picklistDto.setOrderedQuantity(quantityRequested);
                picklistDto.setBinName("N/A");
                picklistDto.setLocation("N/A");
                picklistDto.setPicked("false");
                picklistDto.setQuantity(remainingQuantity);
                
                createPickEntry(picklistDto, null, null, storeId, skuId, refNo, "out");
                
                // updated to show 0 in picklist for the remaining quantity
                picklistDto.setOrderedQuantity(0L);
                picklist.add(picklistDto);
                
            }
            
            StockTransferSkuEntity trnEntity = trnSkuRepo.findByTransferNumberAndTransferSkuCode(req.getRefNo(), skuEntity.getSkuCode());
            trnEntity.setDispatchQuantity(String.valueOf(Long.valueOf(trnEntity.getTransferQuantity())-remainingQuantity));
            trnEntity.setHoldQuantity(trnEntity.getDispatchQuantity());
            trnEntity.setRecievedQuantity(trnEntity.getDispatchQuantity());
            trnEntity.setPicked("true");
            trnEntity.setId(trnEntity.getId());
            trnSkuRepo.save(trnEntity);

        }
		
		return picklist;
	}
	
	private void createPickEntry(PicklistDto pick, Long stockInId, Long binId, Long storeId, Long skuId, Long refNo, String type) {
		PicklistEntity pE = new PicklistEntity();
		pE.setStockInId(stockInId);
		pE.setBinId(binId);
		pE.setCreatedBy("system");
		pE.setQuantity(pick.getQuantity());
		pE.setRefNo(refNo);
		pE.setSkuId(skuId);
		pE.setStoreId(storeId);
		pE.setType(type);
		pE.setPicked(pick.getPicked());
		pickRepo.save(pE);
	}
	
	private void updatePickEntry(Long id, PicklistDto pick, Long stockInId, Long binId, Long storeId, Long skuId, Long refNo, String type) {
		PicklistEntity pE = pickRepo.getOne(id);
		pE.setStockInId(stockInId);
		pE.setBinId(binId);
		pE.setCreatedBy("system");
		pE.setQuantity(pick.getQuantity());
		pE.setRefNo(refNo);
		pE.setSkuId(skuId);
		pE.setStoreId(storeId);
		pE.setType(type);
		pE.setPicked("false");
		pickRepo.save(pE);
	}
	
	private void removePickEntry(PicklistEntity entity) {
		pickRepo.delete(entity);
	}

}
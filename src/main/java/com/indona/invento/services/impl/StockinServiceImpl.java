package com.indona.invento.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.BinsRepository;
import com.indona.invento.dao.SkusRepository;
import com.indona.invento.dao.StockinRepository;
import com.indona.invento.dao.StoresRepository;
import com.indona.invento.dto.ExcelRow;
import com.indona.invento.dto.StockInReportDto;
import com.indona.invento.entities.BinsEntity;
import com.indona.invento.entities.SkuEntity;
import com.indona.invento.entities.StockinEntity;
import com.indona.invento.services.StockinService;
import com.indona.invento.services.helpers.StockinServiceImplHelper;

import jakarta.transaction.Transactional;

@Service
public class StockinServiceImpl implements StockinService {

	@Autowired
	private StockinRepository stockinRepository;

	@Autowired
	private SkusRepository skuRepository;

	@Autowired
	private BinsRepository binRepository;

	@Autowired
	private StoresRepository storeRepository;

	@Autowired
	private StockinServiceImplHelper helper;

	@Override
	public List<StockinEntity> getStockinExcludingDamaged(int deleteFlag, String search, Pageable pageable) {
		return stockinRepository.findStockInExcludingDamaged(deleteFlag, search, pageable);
	}

	@Override
	public List<StockinEntity> getAllStockin(String name, Pageable pageable) {
		return stockinRepository.findByDeleteFlagOrderByDateTimeDesc(0, name, pageable);
	}

	@Override
	public List<StockInReportDto> getAllStockinReport() {
		List<Object> report = stockinRepository.getAllStockInReport();
		List<StockInReportDto> reportList = new ArrayList<>();
		Long count = 0L;
		for (Object obj : report) {
			Object[] objArr = (Object[]) obj;
			StockInReportDto dto = new StockInReportDto();
			dto.setSkuCode(objArr[0]);
			dto.setSkuName(objArr[1]);
			dto.setStoreName(objArr[2]);
			dto.setQuantity(objArr[3]);
			reportList.add(dto);
			count++;
			System.out.println("Saved:" + count + "/" + report.size());
		}
		return reportList;
	}

	@Override
	public StockinEntity getStockinById(Long id) {
		return stockinRepository.findById(id).get();
	}

	@Override
	public StockinEntity createStockin(StockinEntity line) {
		switch (line.getType()) {
		case "stock-in":
			return helper.createStockin(line);
		case "bin-transfer":
			return helper.createBinTransfer(line);
		case "stock-transfer":
			return helper.createStockTransfer(line);
		case "return-transfer":
			return helper.createStockReturn(line);
		default:
			return null;
		}
	}

	@Override
	public StockinEntity updateStockin(Long id, StockinEntity line) {
		if (stockinRepository.existsById(id)) {
			StockinEntity entity = stockinRepository.findById(id).get();
			line.setId(id);
			line.setSkuName(entity.getSkuName());
			line.setCreatedBy(entity.getCreatedBy());
			return stockinRepository.save(line);
		}
		return null; // Or throw an exception indicating line not found
	}

	@Override
	public void deleteStockin(Long id, String remark) {
		Optional<StockinEntity> stockIn = stockinRepository.findById(id);
		stockIn.get().setDeleteFlag(1);
		stockIn.get().setDeleteRemark(remark);
		stockinRepository.save(stockIn.get());
	}

	@Override
//    @Transactional
	public void processExcelData(List<ExcelRow> rows) {
		Long count = 0L;
		Long rowsSize = Long.valueOf(rows.size());
		for (ExcelRow row : rows) {

			if (row.getBinName().equalsIgnoreCase("") || row.getSkuCode().equalsIgnoreCase("")
					|| row.getSkuName().equalsIgnoreCase("")) {
				continue;
			} else if (Long.valueOf(row.getSkuQuantity()) < 0 || row.getSkuQuantity() == null) {
				SkuEntity sku = skuRepository.findBySkuCode(row.getSkuCode());
				if (sku == null) {
					sku = new SkuEntity();
					sku.setSkuCode(row.getSkuCode());
					sku.setSkuName(row.getSkuName());

					if (row.getServicePrice() == null) {
						sku.setServicePrice("0");
					} else {
						sku.setServicePrice(row.getServicePrice());
					}

					if (row.getRetailPrice() == null) {
						sku.setRetailPrice("0");
					} else {
						sku.setRetailPrice(row.getRetailPrice());
					}

					if (row.getPgmPrice() == null) {
						sku.setPgmPrice("0");
					} else {
						sku.setPgmPrice(row.getPgmPrice());
					}

					if (row.getDealerPrice() == null) {
						sku.setDealerPrice("0");
					} else {
						sku.setDealerPrice(row.getDealerPrice());
					}

					sku.setCategoryId(Long.valueOf(row.getCategoryId()));
					sku.setCreatedBy("admin");
					sku.setStatus(1);
					sku = skuRepository.save(sku);
				} else {
//					if (row.getServicePrice() == null) {
//						sku.setServicePrice("0");
//					} else {
//						sku.setServicePrice(row.getServicePrice());
//					}
//
//					if (row.getRetailPrice() == null) {
//						sku.setRetailPrice("0");
//					} else {
//						sku.setRetailPrice(row.getRetailPrice());
//					}
//
//					if (row.getPgmPrice() == null) {
//						sku.setPgmPrice("0");
//					} else {
//						sku.setPgmPrice(row.getPgmPrice());
//					}
//
//					if (row.getDealerPrice() == null) {
//						sku.setDealerPrice("0");
//					} else {
//						sku.setDealerPrice(row.getDealerPrice());
//					}

//					sku.setCreatedBy("admin");
//					sku.setStatus(1);
//					sku.setCategoryId(Long.valueOf(row.getCategoryId()));
//					sku = skuRepository.save(sku);
				}

				BinsEntity bin = binRepository.findByBinName(row.getBinName());
				if (bin == null) {
					bin = new BinsEntity();
					bin.setBinName(row.getBinName());
					bin.setBinLocation(row.getBinName());
					bin.setStoreId(9L);
					bin.setCreatedBy("admin");
					bin.setStatus(1);
					bin = binRepository.save(bin);
				}

				StockinEntity stockIn = new StockinEntity();
				stockIn.setSkuId(sku.getId());
				stockIn.setSkuQuantity(0L);
				stockIn.setBinId(bin.getId());
				stockIn.setCreatedBy("admin");
				stockIn.setSkuHold(0L);
				stockIn.setStoreId(9L);
				stockIn.setSkuName(sku.getSkuName());
				stockIn.setCreatedBy("admin");
				stockIn.setStatus(1);
				stockinRepository.save(stockIn);
			} else {
				SkuEntity sku = skuRepository.findBySkuCode(row.getSkuCode());
				if (sku == null) {
					sku = new SkuEntity();
					sku.setSkuCode(row.getSkuCode());
					sku.setSkuName(row.getSkuName());
					if (row.getServicePrice() == null) {
						sku.setServicePrice("0");
					} else {
						sku.setServicePrice(row.getServicePrice());
					}

					if (row.getRetailPrice() == null) {
						sku.setRetailPrice("0");
					} else {
						sku.setRetailPrice(row.getRetailPrice());
					}

					if (row.getPgmPrice() == null) {
						sku.setPgmPrice("0");
					} else {
						sku.setPgmPrice(row.getPgmPrice());
					}

					if (row.getDealerPrice() == null) {
						sku.setDealerPrice("0");
					} else {
						sku.setDealerPrice(row.getDealerPrice());
					}
					sku.setCategoryId(Long.valueOf(row.getCategoryId()));
					sku.setCreatedBy("admin");
					sku.setStatus(1);
					sku = skuRepository.save(sku);
				} else {
//					if (row.getServicePrice() == null) {
//						sku.setServicePrice("0");
//					} else {
//						sku.setServicePrice(row.getServicePrice());
//					}
//
//					if (row.getRetailPrice() == null) {
//						sku.setRetailPrice("0");
//					} else {
//						sku.setRetailPrice(row.getRetailPrice());
//					}
//
//					if (row.getPgmPrice() == null) {
//						sku.setPgmPrice("0");
//					} else {
//						sku.setPgmPrice(row.getPgmPrice());
//					}
//
//					if (row.getDealerPrice() == null) {
//						sku.setDealerPrice("0");
//					} else {
//						sku.setDealerPrice(row.getDealerPrice());
//					}
//					sku.setCategoryId(Long.valueOf(row.getCategoryId()));
//					sku.setCreatedBy("admin");
//					sku.setStatus(1);
//					sku = skuRepository.save(sku);
				}

				BinsEntity bin = binRepository.findByBinName(row.getBinName());
				if (bin == null) {
					bin = new BinsEntity();
					bin.setBinName(row.getBinName());
					bin.setBinLocation(row.getBinName());
					bin.setStoreId(9L);
					bin.setCreatedBy("admin");
					bin.setStatus(1);
					bin = binRepository.save(bin);
				}

				StockinEntity stockIn = new StockinEntity();
				stockIn.setSkuId(sku.getId());
				stockIn.setSkuQuantity(Long.valueOf(row.getSkuQuantity()));
				stockIn.setBinId(bin.getId());
				stockIn.setCreatedBy("admin");
				stockIn.setSkuHold(0L);
				stockIn.setStoreId(9L);
				stockIn.setSkuName(sku.getSkuName());
				stockIn.setCreatedBy("admin");
				stockIn.setStatus(1);
				stockinRepository.save(stockIn);
			}
			count++;
			System.out.println("Saved:" + count + "/" + rowsSize);
		}
	}

	@Override
	public List<StockinEntity> getAllStockinByStore(Long storeId, Pageable pageable) {
		return stockinRepository.findByStoreIdAndDeleteFlagOrderByDateTimeDesc(storeId, 0, pageable);
	}

	@Override
	public List<StockinEntity> searchStockinByStore(Long storeId, String name, Pageable pageable) {
		return stockinRepository.searchByStoreIdAndFields(storeId, 0, name, pageable);
	}

	public void reduceStockQuantity(String binName, String partNumber, Long damageQuantity) {
		int rowsUpdated = stockinRepository.updateStockQuantityByBinAndPartNumber(binName, partNumber, damageQuantity);
		if (rowsUpdated > 0) {
			System.out.println("Stock quantity updated successfully.");
		} else {
			System.out.println("No matching stock found or quantity was not updated.");
		}
	}
	
    public List<StockinEntity> getStockinExcludingDamaged1(int deleteFlag, String search, Pageable pageable) {
        return stockinRepository.findStockInExcludingDamaged(deleteFlag, search, pageable);
    }

    @Override
//  @Transactional
	public void processPriceExcelData(List<ExcelRow> rows) {
		Long count = 0L;
		Long rowsSize = Long.valueOf(rows.size());
		for (ExcelRow row : rows) {

			if (row.getSkuCode().equalsIgnoreCase("")) {
				continue;
			} else {
				SkuEntity sku = skuRepository.findBySkuCode(row.getSkuCode());
				if (sku == null) {
					sku = new SkuEntity();
					sku.setSkuCode(row.getSkuCode());
					sku.setSkuName(row.getSkuName());

					if (row.getServicePrice() == null) {
						sku.setServicePrice("0");
					} else {
						sku.setServicePrice(row.getServicePrice());
					}

					if (row.getRetailPrice() == null) {
						sku.setRetailPrice("0");
					} else {
						sku.setRetailPrice(row.getRetailPrice());
					}

					if (row.getPgmPrice() == null) {
						sku.setPgmPrice("0");
					} else {
						sku.setPgmPrice(row.getPgmPrice());
					}

					if (row.getDealerPrice() == null) {
						sku.setDealerPrice("0");
					} else {
						sku.setDealerPrice(row.getDealerPrice());
					}

					sku.setCategoryId(2L);
					sku.setCreatedBy("admin");
					sku.setStatus(1);
					sku = skuRepository.save(sku);
				} else {
					if (row.getServicePrice() == null) {
						sku.setServicePrice("0");
					} else {
						sku.setServicePrice(row.getServicePrice());
					}

					if (row.getRetailPrice() == null) {
						sku.setRetailPrice("0");
					} else {
						sku.setRetailPrice(row.getRetailPrice());
					}

					if (row.getPgmPrice() == null) {
						sku.setPgmPrice("0");
					} else {
						sku.setPgmPrice(row.getPgmPrice());
					}

					if (row.getDealerPrice() == null) {
						sku.setDealerPrice("0");
					} else {
						sku.setDealerPrice(row.getDealerPrice());
					}

					sku.setCreatedBy("admin");
					sku.setStatus(1);
//					sku.setCategoryId(Long.valueOf(row.getCategoryId()));
					sku = skuRepository.save(sku);
				}

			}
			count++;
			System.out.println("Saved:" + count + "/" + rowsSize);
		}
	}
}

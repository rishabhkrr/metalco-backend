package com.indona.invento.services.impl;

import com.indona.invento.dao.HsnCodeMasterRepository;
import com.indona.invento.dao.PackingListJobWorkRepository;
import com.indona.invento.dao.PackingSubmissionRepository;
import com.indona.invento.dao.SalesOrderLineItemRepository;
import com.indona.invento.dao.StockSummaryRepository;
import com.indona.invento.dao.UnitMasterRepository;
import com.indona.invento.dto.PackingListJobWorkDTO;
import com.indona.invento.dto.PackingListJobWorkResponseDTO;
import com.indona.invento.entities.PackingListJobWorkEntity;
import com.indona.invento.entities.PackingSubmission;
import com.indona.invento.entities.SalesOrderLineItem;
import com.indona.invento.entities.UnitContactDetailsEntity;
import com.indona.invento.entities.UnitMasterEntity;
import com.indona.invento.services.PackingListJobWorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PackingListJobWorkServiceImpl implements PackingListJobWorkService {

	@Autowired
	private PackingListJobWorkRepository repository;

	@Autowired
	private StockSummaryRepository stockSummaryRepository;

	@Autowired
	private HsnCodeMasterRepository hsnCodeMasterRepository;
	
	@Autowired
	private UnitMasterRepository unitMasterRepository;
	
	@Autowired
    private SalesOrderLineItemRepository salesOrderLineItemRepository;

    @Autowired
    private PackingSubmissionRepository packingSubmissionRepository;
    
	@Override
	public List<PackingListJobWorkEntity> savePackingList(List<PackingListJobWorkDTO> dtos) {

		LocalDateTime now = LocalDateTime.now();
		String generatedCode = "MEPC"
				+ now.format(DateTimeFormatter.ofPattern("ddMMyy"))
				+ now.format(DateTimeFormatter.ofPattern("mmss"));

		return dtos.stream().map(dto -> {

			// --- 1. SO + Line Item Fetch ---
			SalesOrderLineItem lineItem = salesOrderLineItemRepository
					.findBySalesOrder_SoNumberAndLineNumber(
							dto.getSoNumber(),
							dto.getLineNumber()
					)
					.orElseThrow(() -> new RuntimeException(
							"SO + Line Number not found: "
									+ dto.getSoNumber()
									+ "/" + dto.getLineNumber()
					));

	        // --- 2. Packing Status Logic ---
	        String finalPackingStatus;

			if (Boolean.TRUE.equals(lineItem.getPacking())) {

				PackingSubmission submission =
						packingSubmissionRepository
								.findBySoNumberAndLineNumber(
										dto.getSoNumber(),
										dto.getLineNumber()
								);

				finalPackingStatus =
						(submission == null) ? "PENDING" : submission.getPackingStatus();

			} else {
				finalPackingStatus = "NOT APPLICABLE";
			}

			// --- 3. Build Entity ---
			PackingListJobWorkEntity entity = PackingListJobWorkEntity.builder()
					.timestamp(now)
					.packingListNumber(generatedCode)
					.soNumber(dto.getSoNumber())
					.lineNumber(dto.getLineNumber())
					.unit(dto.getUnit())
					.customerCode(dto.getCustomerCode())
					.customerName(dto.getCustomerName())
					.packingStatus(finalPackingStatus)
					.orderType(dto.getOrderType())
					.productCategory(dto.getProductCategory())
					.itemDescription(dto.getItemDescription())
					.brand(dto.getBrand())
					.grade(dto.getGrade())
					.temper(dto.getTemper())
					.dimension(dto.getDimension())
					.quantityKg(dto.getQuantityKg())
					.uomKg(dto.getUomKg())
					.quantityNo(dto.getQuantityNo())
					.uomNo(dto.getUomNo())
					.build();

	        return repository.save(entity);

	    }).collect(Collectors.toList());
	}


	@Override
	public List<PackingListJobWorkEntity> getAllPackingLists() {
		return repository.findAll();
	}

	// 1. Get all packing list numbers
	@Override
	public List<String> getAllPackingListNumbers() {
		return repository.findAll().stream().map(PackingListJobWorkEntity::getPackingListNumber)
				.collect(Collectors.toList());
	}

	// 2. Get all records by a specific packing list number
	@Override
	public List<PackingListJobWorkResponseDTO> getByPackingListNumber(String packingListNumber) {
		List<PackingListJobWorkEntity> entities = repository.findByPackingListNumber(packingListNumber);

		return entities.stream().map(entity -> {
			PackingListJobWorkResponseDTO dto = new PackingListJobWorkResponseDTO();

			dto.setId(entity.getId());
			dto.setTimestamp(entity.getTimestamp());
			dto.setPackingListNumber(entity.getPackingListNumber());
			dto.setSoNumber(entity.getSoNumber());
			dto.setLineNumber(entity.getLineNumber());
			dto.setUnit(entity.getUnit());
			dto.setCustomerCode(entity.getCustomerCode());
			dto.setCustomerName(entity.getCustomerName());
			dto.setPackingStatus(entity.getPackingStatus());
			dto.setOrderType(entity.getOrderType());
			dto.setProductCategory(entity.getProductCategory());
			dto.setItemDescription(entity.getItemDescription());
			dto.setBrand(entity.getBrand());
			dto.setGrade(entity.getGrade());
			dto.setTemper(entity.getTemper());
			dto.setDimension(entity.getDimension());
			dto.setQuantityKg(entity.getQuantityKg());
			dto.setUomKg(entity.getUomKg());
			dto.setQuantityNo(entity.getQuantityNo());
			dto.setUomNo(entity.getUomNo());

			// Updated: Fetch first matching price even if duplicates exist
			List<BigDecimal> prices =
			        stockSummaryRepository.findPricesByItemDescription(entity.getItemDescription());

			BigDecimal price = (prices != null && !prices.isEmpty())
			        ? prices.get(0)
			        : null;

			dto.setItemPrice(price);


			// Fetch hsnCode from HsnCodeMasterRepository using productCategory
			String hsnCode = Optional.ofNullable(entity.getProductCategory())
					.map(hsnCodeMasterRepository::findHsnCodeByProductCategory).orElse(null);
			dto.setHsnCode(hsnCode);
			
			 UnitMasterEntity unitEntity =
	                    unitMasterRepository.findByUnitName(entity.getUnit()).orElse(null);

	            if (unitEntity != null) {

	                dto.setUnitAddress(unitEntity.getUnitAddress());
	                dto.setPan(unitEntity.getPan());
	                dto.setGstOrUin(unitEntity.getGstOrUin());
	                dto.setGstStateCode(unitEntity.getGstStateCode());
	                dto.setState(unitEntity.getState());

	                // Primary contact email
	                String email = unitEntity.getContactDetails()
	                        .stream()
	                        .filter(UnitContactDetailsEntity::getPrimary)
	                        .map(UnitContactDetailsEntity::getEmailId)
	                        .findFirst()
	                        .orElse(null);

	                dto.setEmailId(email);
	            }


			return dto;
		}).collect(Collectors.toList());
	}
}

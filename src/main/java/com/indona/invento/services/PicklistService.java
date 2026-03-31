package com.indona.invento.services;

import java.util.List;

import com.indona.invento.dto.PicklistDto;
import com.indona.invento.dto.PicklistRequestDto;
import com.indona.invento.entities.StockTransferSkuEntity;

public interface PicklistService {

	List<PicklistDto> generateSOPicklist(PicklistRequestDto req, List<StockTransferSkuEntity> skus);
	List<PicklistDto> generateSILocation(PicklistRequestDto req);
	List<PicklistDto> generateSOReturn(PicklistRequestDto req, List<StockTransferSkuEntity> skus);
	
}

package com.indona.invento.services;

import com.indona.invento.dto.DeliveryChallanItemDetailsDTO;
import com.indona.invento.dto.DeliveryChallanMergedDTO;
import com.indona.invento.entities.DeliveryChallanJWEntity;

import java.util.List;

public interface DeliveryChallanJWService {
	 List<DeliveryChallanJWEntity> saveAll(List<DeliveryChallanJWEntity> challans);

	    DeliveryChallanJWEntity save(DeliveryChallanJWEntity challan);

	    List<DeliveryChallanJWEntity> getAll();

	    DeliveryChallanJWEntity getByMedcNumber(String medcNumber);

	    DeliveryChallanJWEntity updateById(Long id, DeliveryChallanJWEntity updatedChallan);

	    void deleteById(Long id);

	    DeliveryChallanJWEntity getById(Long id);

	    List<String> getAllMedcNumbers();

	    List<DeliveryChallanMergedDTO> getMergedDataByMedcNumbers(List<String> medcNumbers);

	    List<DeliveryChallanItemDetailsDTO> getItemDetailsByMedcNumber(String medcNumber);
	    
	List<String> getDimensionsByMedcNumber(String medcNumber);

	List<DeliveryChallanItemDetailsDTO> getItemDetailsByMedcAndDimension(String medcNumber, String dimension);

	void deleteAll();

}
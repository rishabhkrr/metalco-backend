package com.indona.invento.services;

import com.indona.invento.dto.POGenerationDTO;
import com.indona.invento.dto.POGenerationResponseDTO;
import com.indona.invento.entities.POGenerationEntity;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;

public interface POGenerationService {
    POGenerationEntity savePOGeneration(POGenerationDTO dto);
    List<POGenerationEntity> getPOsBySupplier(String supplierCode, String supplierName);
    POGenerationEntity getPOById(Long id);
    POGenerationEntity getPOByPoNumber(String poNumber);
    Page<POGenerationResponseDTO> getAllPOs(int page, int size);
    List<POGenerationResponseDTO> getAllPOsWithoutPagination();
    POGenerationEntity updateRemarks(String poNumber, String remarks);
    POGenerationEntity updatePOGeneration(Long id, POGenerationDTO dto);
    List<POGenerationEntity> getPOGenerationsBetweenDates(Date fromDate, Date toDate);
    POGenerationEntity updatePOStatusAndItems(String poNumber);
    void deleteAllPOGenerations();

}


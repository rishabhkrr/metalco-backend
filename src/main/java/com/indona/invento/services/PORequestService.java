package com.indona.invento.services;

import com.indona.invento.dto.PORequestDTO;
import com.indona.invento.dto.SupplierCodeNameDTO;
import com.indona.invento.dto.UnitNameCodeDTO;
import com.indona.invento.dto.PORequestDetailsDto;
import com.indona.invento.entities.PORequestEntity;

import java.util.Date;
import java.util.List;
public interface PORequestService {

    PORequestEntity createPORequest(PORequestDTO request);

    PORequestEntity updatePORequest(Long id, PORequestDTO request);

    PORequestEntity deletePORequest(Long id);

    PORequestEntity getPORequestById(Long id);

    List<PORequestEntity> getAllPORequests();

    List<PORequestEntity> getPOsBySupplier(String supplierCode, String supplierName, String unitCode);

    List<SupplierCodeNameDTO> getAllSupplierCodeNamePairs();

    List<UnitNameCodeDTO> getAllUnitNamesWithCodes();

    List<PORequestEntity> getPORequestsBetweenDates(Date fromDate, Date toDate);

    PORequestDetailsDto getPORequestDetailsByPrNumber(String prNumber);

    void deleteAllPORequests();

}


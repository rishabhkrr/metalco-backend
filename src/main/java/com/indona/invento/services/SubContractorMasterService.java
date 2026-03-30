package com.indona.invento.services;

import com.indona.invento.dto.SubContractorMasterDto;
import com.indona.invento.entities.SubContractorMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SubContractorMasterService {
	SubContractorMasterEntity save(SubContractorMasterDto dto);

	SubContractorMasterEntity update(Long id, SubContractorMasterDto dto);

	boolean delete(Long id);

	Page<SubContractorMasterEntity> getAll(Pageable pageable);

	SubContractorMasterEntity getById(Long id);

	boolean isSubContractorNameExists(String name);

	List<SubContractorMasterEntity> getAllWithoutPagination();

	List<String> getAllSubContractorCodes();

	List<String> getAllApprovedSubContractorCodes();

	List<String> getAllSubContractorNames();

	Map<String, Object> getDetailsByCode(String subContractorCode);

	Map<String, Object> getDetailsByName(String subContractorName);

	SubContractorMasterEntity approveSubContractor(Long id) throws Exception;

	SubContractorMasterEntity rejectSubContractor(Long id) throws Exception;

}
package com.indona.invento.services;

import com.indona.invento.dto.UnitMasterDto;
import com.indona.invento.entities.UnitMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UnitMasterService {
    UnitMasterEntity create(UnitMasterDto dto);
    UnitMasterEntity update(Long id, UnitMasterDto dto);
    boolean delete(Long id);
    Page<UnitMasterEntity> getAll(Pageable pageable);
    UnitMasterEntity getById(Long id);
    UnitMasterEntity getByUnitCode(String unitCode);
    UnitMasterEntity getByUnitName(String unitName);
    List<String> getAllUnitCodes();
    List<String> getAllApprovedUnitCodes();
    List<UnitMasterEntity> getAllWithoutPagination();
    List<String> getUnitCodesByName(String unitName);
    UnitMasterEntity approveUnit(Long id) throws Exception;
    UnitMasterEntity rejectUnit(Long id) throws Exception;

}


package com.indona.invento.services;

import com.indona.invento.dto.SalesmanMasterDto;
import com.indona.invento.entities.SalesmanMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SalesmanMasterService {
    SalesmanMasterEntity createSalesman(SalesmanMasterDto dto);
    SalesmanMasterEntity update(Long id, SalesmanMasterDto dto);
    Page<SalesmanMasterEntity> getAll(Pageable pageable);
    SalesmanMasterEntity getById(Long id);
    boolean delete(Long id);
    List<SalesmanMasterEntity> getAllWithoutPagination();
    SalesmanMasterEntity approveSalesman(Long id) throws Exception;
    SalesmanMasterEntity rejectSalesman(Long id) throws Exception;

}

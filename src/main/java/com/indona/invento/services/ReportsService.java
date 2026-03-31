package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.RequisitionEntity;

public interface ReportsService {

	List<RequisitionEntity> getAllAreas();
    RequisitionEntity getAreaById(Long id);
    RequisitionEntity createArea(RequisitionEntity area);
    RequisitionEntity updateArea(Long id, RequisitionEntity area);
    void deleteArea(Long id);
}

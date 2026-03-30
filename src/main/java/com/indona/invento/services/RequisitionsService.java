package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.RequisitionEntity;

public interface RequisitionsService {

	List<RequisitionEntity> getAllRequisitions();
    RequisitionEntity getRequisitionById(Long id);
    RequisitionEntity createRequisition(RequisitionEntity area);
    RequisitionEntity updateRequisition(Long id, RequisitionEntity area);
    void deleteRequisition(Long id);
}

package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.RequisitionsRepository;
import com.indona.invento.entities.RequisitionEntity;
import com.indona.invento.services.RequisitionsService;

@Service
public class RequisitionsServiceImpl implements RequisitionsService {

    @Autowired
    private RequisitionsRepository requisitionsRepository;

    @Override
    public List<RequisitionEntity> getAllRequisitions() {
        return requisitionsRepository.findAll();
    }

    @Override
    public RequisitionEntity getRequisitionById(Long id) {
        return requisitionsRepository.findById(id).orElse(null);
    }

    @Override
    public RequisitionEntity createRequisition(RequisitionEntity area) {
        return requisitionsRepository.save(area);
    }

    @Override
    public RequisitionEntity updateRequisition(Long id, RequisitionEntity area) {
        if (requisitionsRepository.existsById(id)) {
            area.setId(id);
            return requisitionsRepository.save(area);
        }
        return null; // Or throw an exception indicating area not found
    }

    @Override
    public void deleteRequisition(Long id) {
    	requisitionsRepository.deleteById(id);
    }
}

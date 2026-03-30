package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.RequisitionsRepository;
import com.indona.invento.entities.RequisitionEntity;
import com.indona.invento.services.RequisitionsService;
import com.indona.invento.services.ReportsService;

@Service
public class ReportsServiceImpl implements ReportsService {

    @Autowired
    private RequisitionsRepository areasRepository;

    @Override
    public List<RequisitionEntity> getAllAreas() {
        return areasRepository.findAll();
    }

    @Override
    public RequisitionEntity getAreaById(Long id) {
        return areasRepository.findById(id).orElse(null);
    }

    @Override
    public RequisitionEntity createArea(RequisitionEntity area) {
        return areasRepository.save(area);
    }

    @Override
    public RequisitionEntity updateArea(Long id, RequisitionEntity area) {
        if (areasRepository.existsById(id)) {
            area.setId(id);
            return areasRepository.save(area);
        }
        return null; // Or throw an exception indicating area not found
    }

    @Override
    public void deleteArea(Long id) {
        areasRepository.deleteById(id);
    }
}

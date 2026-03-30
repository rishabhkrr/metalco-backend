package com.indona.invento.services.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.indona.invento.dao.BinsRepository;
import com.indona.invento.entities.BinsEntity;
import com.indona.invento.services.BinsService;

@Service
public class BinsServiceImpl implements BinsService {

	@Autowired
    private BinsRepository binsRepository;

    @Override
    public List<BinsEntity> getAllBins() {
        return binsRepository.findAll();
    }

    @Override
    public BinsEntity getBinById(Long id) {
        return binsRepository.findById(id).get();
    }

    @Override
    public BinsEntity createBin(BinsEntity customer) {
        return binsRepository.save(customer);
    }

    @Override
    public BinsEntity updateBin(Long id, BinsEntity customer) {
        if (binsRepository.existsById(id)) {
            customer.setId(id);
            return binsRepository.save(customer);
        }
        return null; // Or throw an exception indicating customer not found
    }

    @Override
    public void deleteBin(Long id) {
        binsRepository.deleteById(id);
    }
}

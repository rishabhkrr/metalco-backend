package com.indona.invento.services.impl;

import com.indona.invento.entities.ManualCashInHandEntity;
import com.indona.invento.dao.ManualCashInHandRepository;
import com.indona.invento.services.ManualCashInHandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ManualCashInHandServiceImpl implements ManualCashInHandService {

    @Autowired
    private ManualCashInHandRepository repository;

    @Override
    public ManualCashInHandEntity saveManualCashInHand(ManualCashInHandEntity manualCashInHand) {
        return repository.save(manualCashInHand);
    }

    @Override
    public List<ManualCashInHandEntity> getManualCashInHandByDateRange(Date fromDate, Date toDate, Long storeId) {
        return repository.findByCreationDateBetweenAndStoreId(fromDate, toDate, storeId);
    }
}


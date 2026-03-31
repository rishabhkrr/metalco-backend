package com.indona.invento.services;

import java.util.List;

import com.indona.invento.entities.BinsEntity;

public interface BinsService {

	List<BinsEntity> getAllBins();
    BinsEntity getBinById(Long id);
    BinsEntity createBin(BinsEntity customer);
    BinsEntity updateBin(Long id, BinsEntity customer);
    void deleteBin(Long id);
}

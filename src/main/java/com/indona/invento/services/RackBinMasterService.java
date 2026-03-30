package com.indona.invento.services;

import com.indona.invento.dto.RackBinMasterDto;
import com.indona.invento.dto.RackBinStorageQtyUpdateDto;
import com.indona.invento.entities.RackBinMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface RackBinMasterService {
    RackBinMasterEntity create(RackBinMasterDto dto);

    RackBinMasterEntity update(Long id, RackBinMasterDto dto);

    boolean delete(Long id);

    Page<RackBinMasterEntity> getAll(Pageable pageable);

    RackBinMasterEntity getById(Long id);

    List<RackBinMasterEntity> getAllWithoutPagination();

    RackBinMasterEntity approveRackBin(Long id) throws Exception;

    RackBinMasterEntity rejectRackBin(Long id) throws Exception;

    // Bulk upload
    List<RackBinMasterEntity> bulkCreate(List<RackBinMasterDto> dtoList);

    List<RackBinMasterEntity> getByStorageArea(String storageArea);

    // Excel upload
    Map<String, Object> uploadExcelData(MultipartFile file);

    // Delete all
    void deleteAll();

//    List<RackBinMasterEntity> getEligibleBins(String itemCategory, Double bundleNetWeight, String store);

    // New method with proper error handling and single best bin selection
    Map<String, Object> findBestEligibleBin(String itemCategory, Double bundleNetWeight, String store, String unitName);


    void updateCurrentStorage(RackBinStorageQtyUpdateDto dto);
}
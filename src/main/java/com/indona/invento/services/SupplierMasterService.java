package com.indona.invento.services;

import com.indona.invento.dto.SupplierFilterRequest;
import com.indona.invento.dto.SupplierMasterDto;
import com.indona.invento.entities.SupplierMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface SupplierMasterService {
    SupplierMasterEntity saveSupplier(SupplierMasterDto dto) throws Exception;

    void deleteSupplierById(Long id) throws Exception;

    Page<SupplierMasterEntity> getAllSuppliers(int page, int size) throws Exception;



    SupplierMasterEntity getSupplierById(Long id) throws Exception;

    SupplierMasterEntity updateSupplier(Long id, SupplierMasterDto dto) throws Exception;


    String getSupplierCodeByName(String name);
    String getSupplierNameByCode(String code);
    List<String> getAllSupplierNames();
    List<String> getAllSupplierCodes();
    Page<SupplierMasterEntity> filterByGstType(String gstType, int page, int size);

    Page<SupplierMasterEntity> filterByCategory(String category, int page, int size);

    Page<SupplierMasterEntity> filterByName(String name, int page, int size);

    Page<SupplierMasterEntity> filterByCode(String code, int page, int size);

    Page<SupplierMasterEntity> filterByNickname(String nickname, int page, int size);

    Page<SupplierMasterEntity> filterSuppliers(SupplierFilterRequest filter, Pageable pageable);

    boolean isSupplierNameExists(String supplierName);

    List<Map<String, String>> getSupplierNamesAndCodesByBrand(String brand);


    List<SupplierMasterEntity> getAllSuppliersWithoutPagination() throws Exception;


    SupplierMasterEntity getSupplierByCode(String supplierCode);

    SupplierMasterEntity approveSupplier(Long id) throws Exception;

    SupplierMasterEntity rejectSupplier(Long id) throws Exception;

}

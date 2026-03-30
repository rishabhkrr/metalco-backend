package com.indona.invento.dao;
import com.indona.invento.entities.SupplierMasterEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierMasterRepository extends JpaRepository<SupplierMasterEntity, Long>, JpaSpecificationExecutor<SupplierMasterEntity> {
    boolean existsBySupplierCode(String supplierCode);

    @Query("SELECT MAX(s.supplierCode) FROM SupplierMasterEntity s")
    String findMaxSupplierCode();

    @Query("SELECT s.supplierCode FROM SupplierMasterEntity s WHERE s.supplierName = :name")
    String findCodeByName(@Param("name") String name);

    @Query("SELECT s.supplierName FROM SupplierMasterEntity s WHERE s.supplierCode = :code")
    String findNameByCode(@Param("code") String code);

    @Query("SELECT s.supplierName FROM SupplierMasterEntity s")
    List<String> findAllSupplierNames();

    @Query("SELECT s.supplierCode FROM SupplierMasterEntity s")
    List<String> findAllSupplierCodes();


    Page<SupplierMasterEntity> findByGstRegistrationType(String gstRegistrationType, Pageable pageable);

    Page<SupplierMasterEntity> findBySupplierCategory(String category, Pageable pageable);


    Page<SupplierMasterEntity> findBySupplierNameContainingIgnoreCase(String supplierName, Pageable pageable);

    Page<SupplierMasterEntity> findBySupplierCodeContainingIgnoreCase(String supplierCode, Pageable pageable);

    Page<SupplierMasterEntity> findBySupplierNicknameContainingIgnoreCase(String supplierNickname, Pageable pageable);


    boolean existsBySupplierNameIgnoreCase(String supplierName);

    List<SupplierMasterEntity> findByBrandIgnoreCase(String brand);


    Optional<SupplierMasterEntity> findBySupplierCode(String supplierCode);
}


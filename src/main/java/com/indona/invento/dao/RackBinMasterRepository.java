package com.indona.invento.dao;

import com.indona.invento.entities.RackBinMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RackBinMasterRepository extends JpaRepository<RackBinMasterEntity, Long> {

    List<RackBinMasterEntity> findByStorageArea(String storageArea);

    List<RackBinMasterEntity> findByItemCategory(String itemCategory);

    // Find all bins by storage type (store)
    @Query("SELECT r FROM RackBinMasterEntity r WHERE LOWER(TRIM(r.storageType)) = LOWER(TRIM(:storageType))")
    List<RackBinMasterEntity> findByStorageType(@Param("storageType") String storageType);

    @Query("SELECT r FROM RackBinMasterEntity r WHERE " +
            "LOWER(TRIM(r.storageType)) = LOWER(TRIM(:storageType)) AND " +
            "LOWER(TRIM(r.unitName)) = LOWER(TRIM(:unitName))")
    List<RackBinMasterEntity> findByStorageTypeAndUnitName(
            @Param("storageType") String storageType,
            @Param("unitName") String unitName);

    @Query("SELECT r FROM RackBinMasterEntity r WHERE LOWER(r.unitName) = LOWER(:unitName) " +
           "AND LOWER(r.storageType) = LOWER(:storageType) " +
           "AND LOWER(r.storageArea) = LOWER(:storageArea) " +
           "AND LOWER(r.binCapacity) = LOWER(:binCapacity) " )
    Optional<RackBinMasterEntity> findByUnitNameAndStorageTypeAndStorageAreaAndBinCapacity(
            @Param("unitName") String unitName,
            @Param("storageType") String storageType,
            @Param("storageArea") String storageArea,
            @Param("binCapacity") String binCapacity);

    Optional<RackBinMasterEntity> findByStorageTypeAndStorageAreaAndRackNoAndColumnNoAndBinNoAndUnitName(
            String storageType,
            String storageArea,
            String rackNo,
            String columnNo,
            String binNo,
            String unitName
    );

    Optional<RackBinMasterEntity> findByStorageTypeAndBinNoAndStorageAreaAndUnitName(
            String storageType,
            String binNo,
            String storageArea,
            String unitName
    );

    /**
     * FRD: RBA-001 to RBA-008 — Rack & Bin Auto-Allocation Algorithm
     * Filter 1: Store (storageType) + Item Category
     * Filter 2: Available capacity (binCapacity - currentStorage) >= requiredWeight
     * Sort 1: Storage Area Order ASC (lowest number first)
     * Sort 2: Distance ASC (closest first)
     */
    @Query("SELECT r FROM RackBinMasterEntity r " +
           "WHERE LOWER(TRIM(r.storageType)) = LOWER(TRIM(:storageType)) " +
           "AND LOWER(TRIM(r.itemCategory)) = LOWER(TRIM(:itemCategory)) " +
           "AND (CAST(r.binCapacity AS double) - COALESCE(r.currentStorage, 0)) >= :requiredWeight " +
           "ORDER BY r.storageAreaOrder ASC, r.distance ASC")
    List<RackBinMasterEntity> findAvailableBinsForAllocation(
            @Param("storageType") String storageType,
            @Param("itemCategory") String itemCategory,
            @Param("requiredWeight") double requiredWeight);

}

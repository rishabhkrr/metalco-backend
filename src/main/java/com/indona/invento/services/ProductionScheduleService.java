package com.indona.invento.services;

import com.indona.invento.dto.ProductionScheduleDto;
import com.indona.invento.entities.ProductionScheduleEntity;

import java.util.List;
import java.util.Map;

public interface ProductionScheduleService {
    ProductionScheduleEntity createProduction(ProductionScheduleEntity production);

    void deleteProduction(Long id);

    List<ProductionScheduleEntity> getMarkingAndCuttingSchedules();

    List<Map<String, String>> getMachineNames();

    List<ProductionScheduleEntity> getAllProductionSchedule();

    List<ProductionScheduleEntity> getAllProductionScheduleByCategory(String productCategory);

    List<String> getDistinctProductCategories();

    ProductionScheduleEntity updateProductionSchedule(Long id, ProductionScheduleDto dto);

    Map<String, Object> getMachineDetails(String machineName);

    /**
     * Get all PENDING production schedules with SO Number + Line Number
     */
    List<Map<String, String>> getPendingSoAndLineNumbers();

    /**
     * Update production schedule machine details (machineName, targetBladeSpeed, targetFeed)
     * Only these 3 fields will be updated, other fields remain unchanged
     */
    ProductionScheduleEntity updateMachineDetails(String soNumber, String lineNumber, String machineName, Double targetBladeSpeed, Double targetFeed);
}

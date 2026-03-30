package com.indona.invento.services;

import com.indona.invento.dto.PackingInstructionDTO;
import com.indona.invento.dto.PackingScheduleDetailsDTO;
import com.indona.invento.entities.PackingEntityScheduler;

import java.util.List;

public interface PackingSchedulerService {
    List<PackingEntityScheduler> getAllPackingSchedules();
    PackingInstructionDTO getPackingInstructionBySoNumber(String soNumber);
    void deleteAllPackingSchedules();

    /**
     * Get packing schedule details by SO Number and Line Number
     * Returns customerCode, customerName, orderType, productCategory, brand, grade, temper, dimension
     */
    PackingScheduleDetailsDTO getDetailsBySoNumberAndLineNumber(String soNumber, String lineNumber);
}

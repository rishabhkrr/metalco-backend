package com.indona.invento.services;

import com.indona.invento.entities.GRNInterUnitEntity;

import java.util.List;
import java.util.Map;

/**
 * FRD: IUMT-GRN Service Interface
 * Covers all 7 sub-modules of the Inter-Unit Material Transfer GRN flow
 */
public interface GRNInterUnitService {

    // Sub-Module 1: GRN Interunit Material Request Entry
    GRNInterUnitEntity createInterUnitGRN(GRNInterUnitEntity entity);
    GRNInterUnitEntity updateInterUnitGRN(Long id, GRNInterUnitEntity entity);
    GRNInterUnitEntity getById(Long id);
    GRNInterUnitEntity getByRefNumber(String refNumber);
    List<GRNInterUnitEntity> getAll();

    // Sub-Module 2: Material Request InterUnit Summary (MRS-001 to MRS-007)
    List<Map<String, Object>> getMaterialRequestSummary(String unit);

    // Sub-Module 3: GRN Interunit Request Summary (GIS-001 to GIS-008)
    List<GRNInterUnitEntity> getGrnSummary(String status, String unit);
    GRNInterUnitEntity approveInterUnitGRN(Long id, String approvedBy);
    GRNInterUnitEntity rejectInterUnitGRN(Long id, String rejectedBy, String remarks);

    // Sub-Module 4: Stock Transfer Integration (STI-001 to STI-010)
    List<GRNInterUnitEntity> getApprovedForStockTransfer(String unit);

    // Sub-Module 5: Bundle Creation & Bin Allocation (ABP, RBA, QRG)
    Map<String, Object> allocateRackBin(String storageType, String itemCategory,
                                         double requiredWeight, String materialAcceptance);

    // Sub-Module 6: Submit Validation (SUB-001 to SUB-009)
    Map<String, Object> validateSubmit(Long stockTransferId, String iuGrnNumber);

    // MEDCI filtering
    boolean isMedciUsed(String medcNumber, String unit);
}

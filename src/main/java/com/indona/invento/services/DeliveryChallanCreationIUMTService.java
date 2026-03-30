package com.indona.invento.services;

import com.indona.invento.entities.DeliveryChallanCreationIUMTEntity;
import com.indona.invento.entities.unitAddressEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DeliveryChallanCreationIUMTService {

    List<DeliveryChallanCreationIUMTEntity> createDeliveryChallanIUMT(List<DeliveryChallanCreationIUMTEntity> entity);

    List<DeliveryChallanCreationIUMTEntity> getAllDeliveryChallansIUMT(String fromDate, String toDate);

    List<Map<String, String>> getUnitCodes();

    List<Map<String, String>> getUnitNames();

    List<Map<String, String>> getDCNumbers(String mode);

    Map<String, Object> getItemPrice(String unit, String description);

    List<unitAddressEntity> getPrimaryAddresses(String unitCode, String unitName);

    void deleteAll();

    // FRD: GRN-002 — Update GRN Status to Completed
    void updateGrnStatusToCompleted(String dcNumber);

    // FRD: ITD-001 to ITD-004 — DC Summary grouped by DC Number
    List<Map<String, Object>> getDcSummaryGrouped();

    // FRD: DCC-002/003/004 — Available packing list numbers (not yet submitted)
    List<Map<String, String>> getAvailablePackingListNumbers(String unit);
}

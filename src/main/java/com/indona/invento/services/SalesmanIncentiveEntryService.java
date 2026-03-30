package com.indona.invento.services;

import com.indona.invento.dto.CustomerOverdueResponseDTO;
import com.indona.invento.dto.SalesmanIncentiveUpdateDTO;
import com.indona.invento.entities.SalesmanIncentiveEntryEntity;

import java.util.List;

public interface SalesmanIncentiveEntryService {
    List<SalesmanIncentiveEntryEntity> getAllIncentives();
    boolean updatePaymentDetails(SalesmanIncentiveUpdateDTO dto);
    CustomerOverdueResponseDTO getCustomerOverdueDetails(String customerName, String customerCode);
    boolean updateQuotationStatus(String quotationNo, String status, boolean hasOverdue);
}

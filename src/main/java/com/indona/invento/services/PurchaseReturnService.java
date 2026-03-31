package com.indona.invento.services;

import com.indona.invento.dto.PurchaseReturnDTO;
import com.indona.invento.entities.PurchaseReturnEntity;

import java.util.List;

public interface PurchaseReturnService {
    
    /**
     * Create a new purchase return record
     */
    PurchaseReturnEntity createPurchaseReturn(PurchaseReturnDTO dto);
    
    /**
     * Get all purchase return records for summary display
     */
    List<PurchaseReturnEntity> getAllPurchaseReturns();
    
    /**
     * Get purchase return by ID
     */
    PurchaseReturnEntity getPurchaseReturnById(Long id);
}


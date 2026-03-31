package com.indona.invento.services;

import com.indona.invento.dto.BillStockReturnDTO;
import com.indona.invento.entities.BillStockReturnEntity;

import java.util.List;

public interface BillStockReturnService {
    
    /**
     * Create a new stock return record
     */
    BillStockReturnEntity createStockReturn(BillStockReturnDTO dto);
    
    /**
     * Get all stock return records for summary display
     */
    List<BillStockReturnEntity> getAllStockReturns();
    
    /**
     * Get stock returns by invoice number
     */
    List<BillStockReturnEntity> getStockReturnsByInvoice(String invoiceNumber);
    
    /**
     * Get stock returns by SO number
     */
    List<BillStockReturnEntity> getStockReturnsBySo(String soNumber);
    
    /**
     * Get stock returns by stock selection type (REJECTION/GENERAL)
     */
    List<BillStockReturnEntity> getStockReturnsByType(String stockSelection);
    
    /**
     * Get stock return by ID
     */
    BillStockReturnEntity getStockReturnById(Long id);

    void deleteAllStockReturns();
}


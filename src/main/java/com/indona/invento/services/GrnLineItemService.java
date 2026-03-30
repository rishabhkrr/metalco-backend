package com.indona.invento.services;

import com.indona.invento.dto.AddBundleRequestDto;
import com.indona.invento.dto.GenerateQrRequestDto;
import com.indona.invento.dto.GenerateQrResponseDto;
import com.indona.invento.dto.GrnLineItemDto;
import com.indona.invento.entities.GrnLineItemEntity;

import java.util.List;

public interface GrnLineItemService {

    /**
     * Add bundles (line items) from GRN to stock transfer
     */
    List<GrnLineItemDto> addBundles(AddBundleRequestDto request);

    /**
     * Generate QR code for a line item
     */
    GenerateQrResponseDto generateQrCode(GenerateQrRequestDto request);

    /**
     * Get all line items for a GRN
     */
    List<GrnLineItemDto> getLineItemsByGrnNumber(String grnNumber);

    /**
     * Get all line items for a stock transfer
     */
    List<GrnLineItemDto> getLineItemsByTransferNumber(String transferNumber);

    /**
     * Get line item by ID
     */
    GrnLineItemDto getLineItemById(Long id);

    /**
     * Update line item
     */
    GrnLineItemDto updateLineItem(Long id, GrnLineItemDto dto);

    /**
     * Delete line item
     */
    void deleteLineItem(Long id);

    /**
     * Get all line items with QR generated
     */
    List<GrnLineItemDto> getLineItemsWithQrGenerated();

    /**
     * Get line items by status
     */
    List<GrnLineItemDto> getLineItemsByStatus(String status);

    /**
     * Get item summary by unit and item description from stock summary
     * Returns: All locations (store + storageArea) with totals, grouped by storage location
     */
    java.util.List<java.util.Map<String, Object>> getItemSummaryByUnitAndDescription(String unit, String itemDescription);
}

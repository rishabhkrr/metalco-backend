package com.indona.invento.services;

import com.indona.invento.dto.*;
import com.indona.invento.entities.StockSummaryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StockSummaryService {

    // Create a new stock entry
    StockSummaryEntity create(StockSummaryDto dto);

    // Update an existing stock entry by ID
    StockSummaryEntity update(Long id, StockSummaryDto dto);

    // Get a single stock entry by ID
    StockSummaryEntity getById(Long id);

    // Get all stock entries
    List<StockSummaryEntity> getAll();

    // Delete a stock entry by ID
    StockSummaryEntity delete(Long id);

    // Delete all stock entries
    void deleteAll();

    List<StockSummaryWithItemDetailsDTO> getFilteredSummary(String unit, List<String> brands, List<String> productCategories, List<String> materialTypes);

    StockSummaryItemDetailsDTO getDetailsByItemDescription(String itemDescription);

    List<StockSummaryFormattedDTO> getFormattedSummary();

    List<RackOnlyDTO> getRackOnlySummary(
            String itemDescription,
            String unit,
            List<String> stores,
            List<String> storageAreas
    );

    List<StockSummaryEntity> bulkCreate(List<StockSummaryDto> dtoList);

    StockAnalysisDto getStockAnalysisByItemAndUnit(String itemDescription, String unit, String productCategory);

    List<java.util.Map<String, Object>> searchByUnitAndItemDescription(String unit, String itemDescription);

    /**
     * Save return stock to Stock Summary
     * Store is always "Loose Piece"
     * If same unit + item description + "Loose Piece" exists, update it
     * Otherwise create new entry
     */
    java.util.Map<String, Object> saveReturnStock(ReturnStockDTO dto);

    /**
     * Allocate Return Rack based on Product Category
     * Store is always "LOOSE PIECE"
     * Finds suitable rack by:
     * 1. Matching Product Category with ITEM_CATEGORY in Rack & Bin
     * 2. Checking available capacity (Storage Capacity - Current Storage >= Return Quantity)
     * 3. Sorting by Storage Area Order (ascending) and Distance (ascending)
     * 4. Returns first available rack
     */
    AllocateReturnRackDTO.SuggestedRackDTO allocateReturnRack(AllocateReturnRackDTO dto);

    /**
     * Get all stock summaries with complete bundle/GRN data
     * This returns all stock summary entries along with their associated bundles
     * that contain full GRN information instead of just GRN numbers
     */
    List<StockSummaryWithBundlesDTO> getAllWithBundles();

    /**
     * Get merged stock summary with GRN details
     * Filter by: unit → itemDescription → itemGroup → dimension
     * Merge all matching entries and return GRN details for each rack
     */
    java.util.Map<String, Object> getMergedStockWithGrnDetails(String unit, String itemDescription, String itemGroup, String dimension);

    /**
     * Get ALL stock summaries merged by unit + itemDescription + itemGroup + dimension
     * Returns all data grouped/merged with complete GRN details
     */
    java.util.List<java.util.Map<String, Object>> getAllMergedWithGrnDetails();
}

package com.indona.invento.services;


import com.indona.invento.dto.PickListDTOs;
import com.indona.invento.dto.SalesOrderSchedulerDTO;
import com.indona.invento.dto.WarehouseStockTransferSchedulerDTO;
import com.indona.invento.entities.SalesOrderSchedulerEntity;

import java.util.List;

public interface SalesOrderSchedulerService {
    void saveSchedule(List<SalesOrderSchedulerDTO> dtoList);
    List<SalesOrderSchedulerDTO> getAllSchedules();
    List<PickListDTOs> generatePickList(List<SalesOrderSchedulerDTO> schedulerList);

    List<SalesOrderSchedulerEntity> updateSchedulerWithPickList(List<PickListDTOs> dtos);
    List<SalesOrderSchedulerEntity> updateSchedulerWithStockTransfer(List<WarehouseStockTransferSchedulerDTO> dtos);

    void deleteAllSchedulers();

    List<SalesOrderSchedulerEntity> getBySoNumber(String soNumber);

    SalesOrderSchedulerEntity getBySoNumberAndLineNumber(String soNumber, String lineNumber);

    List<SalesOrderSchedulerEntity> getAllSchedulersEntities();

    /**
     * Generate Pick List for FULL orders only
     * Sorts by Unit, then Item Description, extracts racks with GRN numbers
     * @param schedulerDto Single SalesOrderSchedulerDTO
     * @return Map with sorted data and racks with GRN numbers
     */
    java.util.Map<String, Object> generatePickListForFullOrder(SalesOrderSchedulerDTO schedulerDto);

    /**
     * Generate Pick List for CUT orders
     * Matches dimensions (Thickness/Dia, Width, Length) and selects from End Piece, Loose Piece, Warehouse
     * @param schedulerDto Single SalesOrderSchedulerDTO
     * @return Map with matched stocks and selection result
     */
    java.util.Map<String, Object> generatePickListForCutOrder(SalesOrderSchedulerDTO schedulerDto);

    /**
     * Save Pick List with selected bundle details from generatePickListForFullOrder response
     * @param dtos List of PickListDTOs with selected bundles
     * @return Map with success status and saved data
     */
    java.util.Map<String, Object> savePickListWithBundles(List<PickListDTOs> dtos);

    /**
     * Get Pick List bundle details by SO number and Line number
     * Returns: STORE, STORAGE AREA, RACK COLUMN & BIN, Retrieval Quantity (Kg), Retrieval Quantity (No), Batch number, Date of inward
     * @param soNumber Sales Order number
     * @param lineNumber Line number
     * @return Map with bundle details
     */
    java.util.Map<String, Object> getPickListBundleDetails(String soNumber, String lineNumber);

    /**
     * Save Stock Transfer with retrieval entries (like pickList save)
     * Uses existing stockTransfer OneToOne relationship in SalesOrderSchedulerEntity
     * @param dtos List of WarehouseStockTransferSchedulerDTO with retrieval entries
     * @return Map with success status and saved data
     */
    java.util.Map<String, Object> saveStockTransferWithEntries(List<WarehouseStockTransferSchedulerDTO> dtos);

    /**
     * Get Stock Transfer Retrieval Entries by SO number and Line number
     * Returns all warehouse stock retrieval entries saved during stock transfer
     * @param soNumber Sales Order number
     * @param lineNumber Line number
     * @return Map with retrieval entries
     */
    java.util.Map<String, Object> getStockTransferRetrievalEntries(String soNumber, String lineNumber);

}

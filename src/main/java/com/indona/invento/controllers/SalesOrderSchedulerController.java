package com.indona.invento.controllers;

import com.indona.invento.dto.PickListDTOs;
import com.indona.invento.dto.SalesOrderSchedulerDTO;
import com.indona.invento.dto.WarehouseStockTransferSchedulerDTO;
import com.indona.invento.entities.SalesOrderSchedulerEntity;
import com.indona.invento.services.SalesOrderSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/sales_order_scheduler")
@RequiredArgsConstructor
public class SalesOrderSchedulerController {

    private final SalesOrderSchedulerService service;

    @PostMapping("/save")
    public ResponseEntity<?> saveSchedule(@RequestBody List<SalesOrderSchedulerDTO> dtoList) {
        service.saveSchedule(dtoList);
        return ResponseEntity.ok(Map.of("message", "Schedule saved successfully", "count", dtoList.size()));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllSchedules() {
        List<SalesOrderSchedulerDTO> data = service.getAllSchedules();
        return ResponseEntity.ok(Map.of("totalCount", data.size(), "schedules", data));
    }

    @PostMapping("/generate-picklist")
    public ResponseEntity<?> generatePickList(@RequestBody List<SalesOrderSchedulerDTO> schedulerList) {
        List<PickListDTOs> pickList = service.generatePickList(schedulerList);
        return ResponseEntity.ok(Map.of("totalCount", pickList.size(), "pickList", pickList));
    }

    @GetMapping("/by-so/{soNumber}/{lineNumber}")
    public ResponseEntity<SalesOrderSchedulerEntity> getBySoNumber(@PathVariable String soNumber, @PathVariable String lineNumber) {
        SalesOrderSchedulerEntity result = service.getBySoNumberAndLineNumber(soNumber, lineNumber);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/scheduler/picklist/save")
    public ResponseEntity<List<SalesOrderSchedulerEntity>> savePickList(@RequestBody List<PickListDTOs> dtos) {
        List<SalesOrderSchedulerEntity> updated = service.updateSchedulerWithPickList(dtos);
        return ResponseEntity.ok(updated);
    }

    /**
     * Save Pick List with selected bundle details (new format from generatePickListForFullOrder)
     *
     * Usage: POST /api/metalco/sales_order_scheduler/scheduler/picklist/save-bundles
     */
    @PostMapping("/scheduler/picklist/save-bundles")
    public ResponseEntity<?> savePickListWithBundles(@RequestBody List<PickListDTOs> dtos) {
        log.info("📋 [SavePickList-Bundles] Saving picklist with {} entries", dtos.size());

        try {
            Map<String, Object> response = service.savePickListWithBundles(dtos);
            log.info("✅ [SavePickList-Bundles] Completed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [SavePickList-Bundles] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to save pick list: " + e.getMessage()
            ));
        }
    }


    @PostMapping("/scheduler/stock-transfer/save")
    public ResponseEntity<List<SalesOrderSchedulerEntity>> saveStockTransfer(@RequestBody List<WarehouseStockTransferSchedulerDTO> dtos) {
        List<SalesOrderSchedulerEntity> updated = service.updateSchedulerWithStockTransfer(dtos);
        return ResponseEntity.ok(updated);
    }


    @GetMapping("/scheduler/all")
    public ResponseEntity<List<SalesOrderSchedulerEntity>> getAllSchedulers() {
        List<SalesOrderSchedulerEntity> all = service.getAllSchedulersEntities();
        return ResponseEntity.ok(all);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllSchedulers() {
        service.deleteAllSchedulers();
        return ResponseEntity.ok("All scheduler entries deleted successfully.");
    }

    /**
     * Generate Pick List for FULL orders only
     * Sorts by Unit, then Item Description, extracts racks with GRN numbers
     *
     * Usage: POST /api/metalco/sales_order_scheduler/picklist/generate
     */
    @PostMapping("/picklist/generate")
    public ResponseEntity<?> generatePickListForFullOrder(@RequestBody SalesOrderSchedulerDTO schedulerDto) {
        log.info("📋 [PickList] Received single DTO request - SO: {}, Unit: {}, Item: {}",
                schedulerDto.getSoNumber(), schedulerDto.getUnit(), schedulerDto.getItemDescription());

        try {
            // Pass single DTO directly to service
            Map<String, Object> response = service.generatePickListForFullOrder(schedulerDto);
            log.info("✅ [PickList] Generation successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [PickList] Error generating pick list: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to generate pick list: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate Pick List for CUT orders
     * Matches dimensions (Thickness/Dia, Width, Length) and selects from available stores
     *
     * Usage: POST /api/metalco/sales_order_scheduler/picklist/generate-cut
     */
    @PostMapping("/picklist/generate-cut")
    public ResponseEntity<?> generatePickListForCutOrder(@RequestBody SalesOrderSchedulerDTO schedulerDto) {
        log.info("📋 [PickList-CUT] Received request - SO: {}, Unit: {}, Item: {}, Dimension: {}",
                schedulerDto.getSoNumber(), schedulerDto.getUnit(), schedulerDto.getItemDescription(), schedulerDto.getDimension());

        try {
            Map<String, Object> response = service.generatePickListForCutOrder(schedulerDto);
            log.info("✅ [PickList-CUT] Generation successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [PickList-CUT] Error generating pick list: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to generate pick list for CUT order: " + e.getMessage()
            ));
        }
    }

    /**
     * Get Pick List bundle details by SO number and Line number
     * Returns: STORE, STORAGE AREA, RACK COLUMN & BIN, Retrieval Quantity (Kg), Retrieval Quantity (No), Batch number, Date of inward
     *
     * Usage: GET /api/metalco/sales_order_scheduler/picklist/bundle-details/{soNumber}/{lineNumber}
     */
    @GetMapping("/picklist/bundle-details/{soNumber}/{lineNumber}")
    public ResponseEntity<?> getPickListBundleDetails(
            @PathVariable String soNumber,
            @PathVariable String lineNumber) {
        log.info("📋 [PickList-Details] Fetching bundle details for SO: {}, Line: {}", soNumber, lineNumber);

        try {
            Map<String, Object> response = service.getPickListBundleDetails(soNumber, lineNumber);
            log.info("✅ [PickList-Details] Fetch successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [PickList-Details] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to fetch picklist bundle details: " + e.getMessage()
            ));
        }
    }

    /**
     * Save Stock Transfer with retrieval entries (like picklist save)
     * Uses existing stockTransfer OneToOne relationship in SalesOrderSchedulerEntity
     *
     * Request Body: Single object with multiple retrievalEntries inside
     *
     * Usage: POST /api/metalco/sales_order_scheduler/stock-transfer/save
     */
    @PostMapping("/stock-transfer/save")
    public ResponseEntity<?> saveStockTransferWithEntries(@RequestBody WarehouseStockTransferSchedulerDTO dto) {
        log.info("📦 [SaveStockTransfer] Saving stock transfer for SO: {} | Line: {} | Entries: {}",
                dto.getSoNumber(), dto.getLineNumber(),
                dto.getRetrievalEntries() != null ? dto.getRetrievalEntries().size() : 0);

        try {
            // Wrap single DTO in list for service method
            List<WarehouseStockTransferSchedulerDTO> dtos = List.of(dto);
            Map<String, Object> response = service.saveStockTransferWithEntries(dtos);
            log.info("✅ [SaveStockTransfer] Completed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [SaveStockTransfer] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to save stock transfer: " + e.getMessage()
            ));
        }
    }

    /**
     * Get Stock Transfer Retrieval Entries by SO Number and Line Number
     * Returns all warehouse stock retrieval entries saved during stock transfer
     *
     * Usage: GET /api/metalco/sales_order_scheduler/stock-transfer/retrieval-entries/{soNumber}/{lineNumber}
     */
    @GetMapping("/stock-transfer/retrieval-entries/{soNumber}/{lineNumber}")
    public ResponseEntity<?> getStockTransferRetrievalEntries(
            @PathVariable String soNumber,
            @PathVariable String lineNumber) {
        log.info("📦 [StockTransfer-Entries] Fetching retrieval entries for SO: {}, Line: {}", soNumber, lineNumber);

        try {
            Map<String, Object> response = service.getStockTransferRetrievalEntries(soNumber, lineNumber);
            log.info("✅ [StockTransfer-Entries] Fetch successful");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ [StockTransfer-Entries] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Failed to fetch stock transfer retrieval entries: " + e.getMessage()
            ));
        }
    }
}

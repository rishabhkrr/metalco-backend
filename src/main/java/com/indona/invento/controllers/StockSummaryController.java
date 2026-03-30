package com.indona.invento.controllers;

import com.indona.invento.dto.AllocateReturnRackDTO;
import com.indona.invento.dto.RackOnlyDTO;
import com.indona.invento.dto.ReturnStockDTO;
import com.indona.invento.dto.StockSummaryDto;
import com.indona.invento.dto.StockSummaryFormattedDTO;
import com.indona.invento.dto.StockSummaryItemDetailsDTO;
import com.indona.invento.dto.StockSummaryWithBundlesDTO;
import com.indona.invento.entities.StockSummaryEntity;
import com.indona.invento.services.ItemDetailsProjection;
import com.indona.invento.services.StockSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stock-summary")
public class StockSummaryController {

    @Autowired
    private StockSummaryService service;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody StockSummaryDto dto) {
        try {
            StockSummaryEntity saved = service.create(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody StockSummaryDto dto) {
        try {
            StockSummaryEntity updated = service.update(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public ResponseEntity<?> getAll() {
        try {
            List<StockSummaryEntity> list = service.getAll();
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", list.size());
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch stock list"));
        }
    }

    /**
     * Get all stock summaries with complete bundle/GRN data
     * Returns all stock summary entries along with their associated bundles
     * that contain full GRN information instead of just GRN numbers
     *
     * Usage: GET /api/metalco/stock-summary/getall-with-bundles
     */
    @GetMapping("/getall-with-bundles")
    public ResponseEntity<?> getAllWithBundles() {
        try {
            List<StockSummaryWithBundlesDTO> list = service.getAllWithBundles();
            Map<String, Object> response = new HashMap<>();
            response.put("totalCount", list.size());
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch stock list with bundles: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            StockSummaryEntity entity = service.getById(id);
            return ResponseEntity.ok(entity);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            StockSummaryEntity deletedEntity = service.delete(id);
            return ResponseEntity.ok(deletedEntity);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAll() {
        try {
            service.deleteAll();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All stock summary entries deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "❌ Failed to delete all entries",
                            "details", e.getMessage()
                    ));
        }
    }

    /**
     * Get merged stock summary with GRN details
     * Filter by: unit → itemDescription → itemGroup → dimension
     * Merges all matching entries and returns GRN details for each rack
     *
     * Usage: GET /api/metalco/stock-summary/merged-with-grn-details?unit=X&itemDescription=Y&itemGroup=Z&dimension=W
     */
    @GetMapping("/merged-with-grn-details")
    public ResponseEntity<?> getMergedStockWithGrnDetails(
            @RequestParam(required = false) String unit,
            @RequestParam(required = false) String itemDescription,
            @RequestParam(required = false) String itemGroup,
            @RequestParam(required = false) String dimension) {
        try {
            Map<String, Object> result = service.getMergedStockWithGrnDetails(unit, itemDescription, itemGroup, dimension);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch merged stock with GRN details: " + e.getMessage()
                    ));
        }
    }

    /**
     * Get ALL stock summaries merged by unit + itemDescription + itemGroup + dimension
     * Returns all data grouped/merged with complete GRN details for each rack
     *
     * Usage: GET /api/metalco/stock-summary/getall-merged
     */
    @GetMapping("/getall-merged")
    public ResponseEntity<?> getAllMergedWithGrnDetails() {
        try {
            List<Map<String, Object>> result = service.getAllMergedWithGrnDetails();
            Map<String, Object> response = new HashMap<>();
            response.put("totalMergedGroups", result.size());
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to fetch all merged stock with GRN details: " + e.getMessage()
                    ));
        }
    }


    @GetMapping("/details")
    public ResponseEntity<?> getDetails(@RequestParam String itemDescription) {
        try {
            StockSummaryItemDetailsDTO details = service.getDetailsByItemDescription(itemDescription);
            return ResponseEntity.ok(details);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/formatted-summary")
    public ResponseEntity<?> getFormattedSummary() {
        try {
            List<StockSummaryFormattedDTO> summary = service.getFormattedSummary();
            return ResponseEntity.ok(Map.of(
                    "totalCount", summary.size(),
                    "data", summary
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch formatted stock summary"));
        }

    }

    @GetMapping("/rackwise-summary")
    public ResponseEntity<?> getRackOnlySummary(
            @RequestParam String itemDescription,
            @RequestParam String unit,
            @RequestParam(required = false) List<String> store,
            @RequestParam(required = false) List<String> storageArea
    ) {
        List<RackOnlyDTO> data = service.getRackOnlySummary(itemDescription, unit, store, storageArea);
        return ResponseEntity.ok(Map.of(
                "totalCount", data.size(),
                "rackWise", data
        ));
    }

    
    @PostMapping("/bulk-create")
    public ResponseEntity<List<StockSummaryEntity>> bulkCreate(@RequestBody List<StockSummaryDto> dtoList) {
        List<StockSummaryEntity> saved = service.bulkCreate(dtoList);
        return ResponseEntity.ok(saved);
    }

    /**
     * Save Return Stock to Stock Summary
     * Store is always "Loose Piece"
     * If same unit + item description + "Loose Piece" exists, update quantity
     * Otherwise create new entry
     *
     * Usage: POST /api/metalco/stock-summary/return-stock/save
     */
    @PostMapping("/return-stock/save")
    public ResponseEntity<?> saveReturnStock(@RequestBody ReturnStockDTO dto) {
        try {
            Map<String, Object> response = service.saveReturnStock(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to save return stock: " + e.getMessage()
                    ));
        }
    }

    /**
     * Allocate Return Rack based on Product Category
     * Store is always "LOOSE PIECE"
     * Finds suitable rack by:
     * 1. Matching Product Category with ITEM_CATEGORY in Rack & Bin
     * 2. Checking available capacity (Storage Capacity - Current Storage >= Return Quantity)
     * 3. Sorting by Storage Area Order (ascending) and Distance (ascending)
     * 4. Returns first available rack
     *
     * Usage: POST /api/metalco/stock-summary/allocate-return-rack
     */
    @PostMapping("/allocate-return-rack")
    public ResponseEntity<?> allocateReturnRack(@RequestBody AllocateReturnRackDTO dto) {
        try {
            AllocateReturnRackDTO.SuggestedRackDTO suggested = service.allocateReturnRack(dto);

            if (suggested == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "No suitable rack found for allocation"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Rack allocated successfully",
                    "data", suggested
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "error", "Failed to allocate rack: " + e.getMessage()
                    ));
        }
    }
}

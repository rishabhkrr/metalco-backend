package com.indona.invento.controllers;

import com.indona.invento.dto.AddBundleRequestDto;
import com.indona.invento.dto.GenerateQrRequestDto;
import com.indona.invento.dto.GenerateQrResponseDto;
import com.indona.invento.dto.GrnLineItemDto;
import com.indona.invento.services.GrnLineItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/grn-line-items")
@RequiredArgsConstructor
public class GrnLineItemController {

    private final GrnLineItemService grnLineItemService;

    /**
     * Add bundles (line items) from GRN to stock transfer
     */
    @PostMapping("/add-bundles")
    public ResponseEntity<?> addBundles(@RequestBody AddBundleRequestDto request) {
        try {
            List<GrnLineItemDto> lineItems = grnLineItemService.addBundles(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Bundles added successfully",
                    "data", lineItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to add bundles: " + e.getMessage()
            ));
        }
    }

    /**
     * Generate QR code for a line item
     */
    @PostMapping("/generate-qr")
    public ResponseEntity<?> generateQrCode(@RequestBody GenerateQrRequestDto request) {
        try {
            GenerateQrResponseDto response = grnLineItemService.generateQrCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to generate QR code: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all line items for a GRN
     */
    @GetMapping("/grn/{grnNumber}")
    public ResponseEntity<?> getLineItemsByGrnNumber(@PathVariable String grnNumber) {
        try {
            List<GrnLineItemDto> lineItems = grnLineItemService.getLineItemsByGrnNumber(grnNumber);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", lineItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch line items: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all line items for a stock transfer
     */
    @GetMapping("/transfer/{transferNumber}")
    public ResponseEntity<?> getLineItemsByTransferNumber(@PathVariable String transferNumber) {
        try {
            List<GrnLineItemDto> lineItems = grnLineItemService.getLineItemsByTransferNumber(transferNumber);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", lineItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch line items: " + e.getMessage()
            ));
        }
    }

    /**
     * Get line item by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLineItemById(@PathVariable Long id) {
        try {
            GrnLineItemDto lineItem = grnLineItemService.getLineItemById(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", lineItem
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Line item not found: " + e.getMessage()
            ));
        }
    }

    /**
     * Update line item
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLineItem(@PathVariable Long id, @RequestBody GrnLineItemDto dto) {
        try {
            GrnLineItemDto updated = grnLineItemService.updateLineItem(id, dto);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Line item updated successfully",
                    "data", updated
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to update line item: " + e.getMessage()
            ));
        }
    }

    /**
     * Delete line item
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLineItem(@PathVariable Long id) {
        try {
            grnLineItemService.deleteLineItem(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Line item deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to delete line item: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all line items with QR generated
     */
    @GetMapping("/qr-generated/all")
    public ResponseEntity<?> getLineItemsWithQrGenerated() {
        try {
            List<GrnLineItemDto> lineItems = grnLineItemService.getLineItemsWithQrGenerated();
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", lineItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch line items: " + e.getMessage()
            ));
        }
    }

    /**
     * Get line items by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getLineItemsByStatus(@PathVariable String status) {
        try {
            List<GrnLineItemDto> lineItems = grnLineItemService.getLineItemsByStatus(status);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", lineItems
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch line items: " + e.getMessage()
            ));
        }
    }

    /**
     * Get GRN summary by unit and item description
     * Pass unit + itemDescription, returns all locations with totals (store + storage area wise)
     */
    @GetMapping("/item-summary")
    public ResponseEntity<?> getItemSummaryByUnitAndDescription(
            @RequestParam String unit,
            @RequestParam String itemDescription) {
        try {
            List<Map<String, Object>> summary = grnLineItemService.getItemSummaryByUnitAndDescription(unit, itemDescription);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", summary,
                    "count", summary.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Failed to fetch summary: " + e.getMessage()
            ));
        }
    }
}

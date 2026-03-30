package com.indona.invento.controllers;


import com.indona.invento.entities.ProductMargin;
import com.indona.invento.services.ProductMarginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/product-margins")
public class ProductMarginController {

    private final ProductMarginService productMarginService;

    public ProductMarginController(ProductMarginService productMarginService) {
        this.productMarginService = productMarginService;
    }

    // ✅ POST API to save Product Margin
    @PostMapping
    public ResponseEntity<?> createMargin(@RequestBody ProductMargin margin) {
        try {
            log.info("📍 [POST] Creating Product Margin - MaterialType: {}, ProductCategory: {}",
                    margin.getMaterialType(), margin.getProductCategory());

            ProductMargin createdMargin = productMarginService.createMargin(margin);

            log.info("✅ [POST] Product Margin created successfully - ID: {}", createdMargin.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdMargin);
        } catch (RuntimeException e) {
            log.error("❌ [POST] Error: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", "DUPLICATE_COMBINATION_EXISTS");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<ProductMargin>> getAllMargins() {
        log.info("📍 [GET] Fetching all Product Margins");
        List<ProductMargin> margins = productMarginService.getAllMargins();
        log.info("✅ [GET] Fetched {} Product Margins", margins.size());
        return ResponseEntity.ok(margins);
    }

    // ✅ PATCH API to approve Product Margin
    @PutMapping("/approve/{id}")
    public ResponseEntity<ProductMargin> approveMargin(@PathVariable Long id) {
        log.info("📍 [PUT] Approving Product Margin - ID: {}", id);
        ProductMargin approvedMargin = productMarginService.approveMargin(id);
        log.info("✅ [PUT] Product Margin approved - ID: {}", id);
        return ResponseEntity.ok(approvedMargin);
    }

    // ✅ PATCH API to reject Product Margin
    @PutMapping("/reject/{id}")
    public ResponseEntity<ProductMargin> rejectMargin(@PathVariable Long id) {
        log.info("📍 [PUT] Rejecting Product Margin - ID: {}", id);
        ProductMargin rejectedMargin = productMarginService.rejectMargin(id);
        log.info("✅ [PUT] Product Margin rejected - ID: {}", id);
        return ResponseEntity.ok(rejectedMargin);
    }

    // ✅ PUT API to edit Product Margin (partial update)
    @PutMapping("/{id}")
    public ResponseEntity<?> editMargin(@PathVariable Long id, @RequestBody ProductMargin margin) {
        try {
            log.info("📍 [PUT] Updating Product Margin - ID: {}", id);
            ProductMargin updatedMargin = productMarginService.editMargin(id, margin);
            log.info("✅ [PUT] Product Margin updated successfully - ID: {}", id);
            return ResponseEntity.ok(updatedMargin);
        } catch (RuntimeException e) {
            log.error("❌ [PUT] Error: {}", e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            errorResponse.put("error", "DUPLICATE_COMBINATION_EXISTS");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ✅ DELETE API to delete Product Margin
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductMargin> deleteMargin(@PathVariable Long id) {
        log.info("📍 [DELETE] Deleting Product Margin - ID: {}", id);
        ProductMargin deletedMargin = productMarginService.deleteMargin(id);
        log.info("✅ [DELETE] Product Margin deleted - ID: {}", id);
        return ResponseEntity.ok(deletedMargin);
    }
}
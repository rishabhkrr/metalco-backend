package com.indona.invento.controllers;

import com.indona.invento.dto.ItemMasterDto;
import com.indona.invento.entities.ItemMasterEntity;
import com.indona.invento.services.ItemMasterService;
import com.indona.invento.services.SupplierMasterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item-master")
public class ItemMasterController {

    private static final Logger logger = LoggerFactory.getLogger(ItemMasterController.class);

    @Autowired
    private ItemMasterService service;

    @Autowired
    private SupplierMasterService supplierService;

    @PostMapping
    public ResponseEntity<ItemMasterEntity> createItem(@Valid @RequestBody ItemMasterDto dto) {
        logger.info("=== Item Master Create Request Received ===");
        logger.info("Product Category: {}", dto.getProductCategory());
        logger.info("Material Type: {}", dto.getMaterialType());
        logger.info("Section Number: {}", dto.getSectionNumber());
        logger.info("SKU Description: {}", dto.getSkuDescription());
        logger.info("Brand: {}", dto.getBrand());
        logger.info("Grade: {}", dto.getGrade());
        logger.info("Temper: {}", dto.getTemper());
        logger.info("Dimension: {}", dto.getDimension());
        logger.info("Dimension1: {}, Dimension2: {}, Dimension3: {}", dto.getDimension1(), dto.getDimension2(), dto.getDimension3());
        logger.info("Primary UOM: {}", dto.getPrimaryUom());
        logger.info("Opening Stock (Kgs): {}", dto.getOpeningStockInKgs());
        logger.info("Opening Stock (Nos): {}", dto.getOpeningStockInNos());
        logger.info("Unit Name: {}", dto.getUnitName());

        ItemMasterEntity created = service.createItem(dto);

        logger.info("✅ Item Created Successfully!");
        logger.info("Item ID: {}", created.getId());
        logger.info("Section Number Saved: {}", created.getSectionNumber());
        logger.info("Dimension Saved: {}", created.getDimension());
        logger.info("Unit Name Saved: {}", created.getUnitName());
        logger.info("Status: {}", created.getStatus());
        logger.info("=== Item Master Create Response ===");

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemMasterEntity> updateItem(@PathVariable Long id, @Valid @RequestBody ItemMasterDto dto) {
        try {
            ItemMasterEntity updated = service.updateItem(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null); // Or return error message if needed
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ItemMasterEntity> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(service.getItemById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllItems() {
        try {
            List<ItemMasterEntity> items = service.getAllItemsWithoutPagination();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/supplier/allCodes")
    public List<String> getAllSupplierCodes() {
        return supplierService.getAllSupplierCodes();
    }

    @GetMapping("/supplier/allNames")
    public List<String> getAllSupplierNames() {
        return supplierService.getAllSupplierNames();
    }

    @GetMapping("/supplier/fetch-by-code/{code}")
    public Map<String, String> getSupplierNameByCode(@PathVariable String code) {
        String name = supplierService.getSupplierNameByCode(code);
        return Collections.singletonMap("supplierName", name);
    }

    @GetMapping("/supplier/fetch-by-name/{name}")
    public Map<String, String> getSupplierCodeByName(@PathVariable String name) {
        String code = supplierService.getSupplierCodeByName(name);
        return Collections.singletonMap("supplierCode", code);
    }

    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<ItemMasterEntity>> getAllItemsWithoutPagination() {
        try {
            List<ItemMasterEntity> items = service.getAllItemsWithoutPagination();
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/fetch-by-category-and-description")
    public ResponseEntity<ItemMasterEntity> getItemByCategoryAndDescription(
            @RequestParam String category,
            @RequestParam String description
    ) {
        return service.getItemByCategoryAndDescription(category, description)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveItem(@PathVariable Long id) {
        try {
            ItemMasterEntity approvedItem = service.approveItem(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ Item approved successfully",
                    "id", approvedItem.getId(),
                    "skuDescription", approvedItem.getSkuDescription(),
                    "status", approvedItem.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to approve item",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectItem(@PathVariable Long id) {
        try {
            ItemMasterEntity rejectedItem = service.rejectItem(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "❌ Item rejected successfully",
                    "id", rejectedItem.getId(),
                    "skuDescription", rejectedItem.getSkuDescription(),
                    "status", rejectedItem.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to reject item",
                    "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/search/by-category-and-brand")
    public ResponseEntity<?> getItemsByCategoryAndBrand(
            @RequestParam String productCategory,
            @RequestParam String brand) {
        try {
            logger.info("🔍 Searching items - Product Category: {}, Brand: {}", productCategory, brand);
            List<ItemMasterEntity> items = service.getItemsByCategoryAndBrand(productCategory, brand);
            logger.info("✅ Found {} items matching criteria", items.size());
            return ResponseEntity.ok(items);
        } catch (Exception e) {
            logger.error("❌ Error searching items: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Failed to search items",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Get dimension by item description
     * Usage: GET /api/metalco/item-master/dimension?itemDescription=XXX
     */
    @GetMapping("/dimension")
    public ResponseEntity<?> getDimensionByItemDescription(@RequestParam String itemDescription) {
        logger.info("🔍 [API] Get dimension for itemDescription: {}", itemDescription);
        try {
            Map<String, Object> response = service.getDimensionByItemDescription(itemDescription);

            if (Boolean.FALSE.equals(response.get("success"))) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("❌ Error getting dimension: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Failed to get dimension",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Bulk upload items from CSV file
     *
     * CSV Columns (in order):
     * brand, grade, lead_time_days, moq, rm_type, sku_description, supplier_code, supplier_name,
     * temper, uom, alt_uom, alt_uom_applicable, dimension1, dimension2, dimension3, Dimension,
     * gst_applicable, gst_rate, hsn_code, material_type, primary_uom, product_category,
     * reporting_uom, section_number, item_price, opening_stock_kg, opening_stock_no
     *
     * Usage: POST /api/metalco/item-master/bulk-upload
     *        Form Data: file (CSV)
     */
    @PostMapping(value = "/bulk-upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> bulkUploadItems(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        logger.info("📤 [API] Bulk Upload Items - File: {}", file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "File is empty"
                ));
            }

            Map<String, Object> response = service.bulkUploadItems(file);

            if (Boolean.TRUE.equals(response.get("success"))) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            logger.error("❌ Bulk upload failed: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "Bulk upload failed",
                    "details", e.getMessage()
            ));
        }
    }

}

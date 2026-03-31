package com.indona.invento.controllers;

import com.indona.invento.dto.HsnCodeMasterDto;
import com.indona.invento.dto.MaterialTypeDto;
import com.indona.invento.dto.ProductCategoryDto;
import com.indona.invento.entities.HsnCodeMasterEntity;
import com.indona.invento.entities.MaterialTypeEntity;
import com.indona.invento.entities.ProductCategoryEntity;
import com.indona.invento.services.HsnCodeMasterService;
import com.indona.invento.services.MaterialTypeService;
import com.indona.invento.services.ProductCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/hsn-code")
public class HsnCodeMasterController {

    @Autowired
    private HsnCodeMasterService hsnCodeMasterService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private MaterialTypeService materialTypeService;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody HsnCodeMasterDto dto) {
        try {
            HsnCodeMasterEntity saved = hsnCodeMasterService.createHsnCode(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "HSN code already exists."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while creating HSN code."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody HsnCodeMasterDto dto) {
        try {
            HsnCodeMasterEntity updated = hsnCodeMasterService.updateHsnCode(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            // Check if it's a duplicate HSN code error
            if (e.getMessage() != null && e.getMessage().contains("already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "HSN code not found for update."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while updating HSN code."));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAll() {
        try {
            List<HsnCodeMasterEntity> hsnCodes = hsnCodeMasterService.getAllHsnCodesWithoutPagination();
            return ResponseEntity.ok(hsnCodes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while fetching HSN codes."));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            HsnCodeMasterEntity entity = hsnCodeMasterService.getHsnCodeById(id);
            if (entity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "HSN code not found for ID: " + id));
            }
            return ResponseEntity.ok(entity);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid HSN code ID."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while fetching HSN code."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@PathVariable Long id) {
        try {
            HsnCodeMasterEntity deleted = hsnCodeMasterService.deleteHsnCodeAndReturn(id);
            if (deleted == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "HSN code not found for deletion with ID: " + id));
            }
            return ResponseEntity.ok(deleted);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Invalid HSN code ID."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while deleting HSN code."));
        }
    }

    @PostMapping("/category")
    public ResponseEntity<?> addCategory(@RequestBody ProductCategoryDto dto) {
        try {
            ProductCategoryEntity saved = productCategoryService.addCategory(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Category already exists."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while creating category."));
        }
    }

    @GetMapping("/category")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<ProductCategoryDto> list = productCategoryService.getAllCategoryNames();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while fetching categories."));
        }
    }

    @PostMapping("/material-type")
    public ResponseEntity<?> addMaterialType(@RequestBody MaterialTypeDto dto) {
        try {
            MaterialTypeEntity saved = materialTypeService.addMaterialType(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", saved.getName()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while creating material type."));
        }
    }

    @GetMapping("/material-type")
    public ResponseEntity<?> getAllMaterialTypes() {
        try {
            List<MaterialTypeDto> list = materialTypeService.getAllMaterialTypes();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error while fetching material types."));
        }
    }

    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<HsnCodeMasterEntity>> getAllHsnCodesWithoutPagination() {
        try {
            List<HsnCodeMasterEntity> hsnCodes = hsnCodeMasterService.getAllHsnCodesWithoutPagination();
            return ResponseEntity.ok(hsnCodes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveHsnCode(@PathVariable Long id) {
        try {
            HsnCodeMasterEntity approvedHsn = hsnCodeMasterService.approveHsnCode(id);
            return ResponseEntity.ok(Map.of(
                    "message", "✅ HSN Code approved successfully",
                    "id", approvedHsn.getId(),
                    "hsnCode", approvedHsn.getHsnCode(),
                    "status", approvedHsn.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to approve HSN Code",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectHsnCode(@PathVariable Long id) {
        try {
            HsnCodeMasterEntity rejectedHsn = hsnCodeMasterService.rejectHsnCode(id);
            return ResponseEntity.ok(Map.of(
                    "message", "❌ HSN Code rejected successfully",
                    "id", rejectedHsn.getId(),
                    "hsnCode", rejectedHsn.getHsnCode(),
                    "status", rejectedHsn.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to reject HSN Code",
                    "details", e.getMessage()
            ));
        }
    }

}

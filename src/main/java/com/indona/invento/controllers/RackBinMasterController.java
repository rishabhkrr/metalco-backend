package com.indona.invento.controllers;

import com.indona.invento.dto.RackBinMasterDto;
import com.indona.invento.dto.RackBinStorageQtyUpdateDto;
import com.indona.invento.entities.RackBinMasterEntity;
import com.indona.invento.services.RackBinMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rack-bin-master")
public class RackBinMasterController {

    @Autowired
    private RackBinMasterService service;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody RackBinMasterDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody RackBinMasterDto dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        boolean deleted = service.delete(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Rack-Bin deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Rack-Bin not found with ID: " + id));
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllRackBins() {
        try {
            List<RackBinMasterEntity> rackBins = service.getAllWithoutPagination();
            return ResponseEntity.ok(rackBins);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<RackBinMasterEntity>> getAllRackBinsWithoutPagination() {
        try {
            List<RackBinMasterEntity> rackBins = service.getAllWithoutPagination();
            return ResponseEntity.ok(rackBins);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveRackBin(@PathVariable Long id) {
        try {
            RackBinMasterEntity approvedRackBin = service.approveRackBin(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "✅ Rack-Bin approved successfully");
            response.put("id", approvedRackBin.getId());
            response.put("rackNo", approvedRackBin.getRackNo() != null ? approvedRackBin.getRackNo() : "");
            response.put("binNo", approvedRackBin.getBinNo() != null ? approvedRackBin.getBinNo() : "");
            response.put("status", approvedRackBin.getStatus() != null ? approvedRackBin.getStatus() : "APPROVED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to approve Rack-Bin",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectRackBin(@PathVariable Long id) {
        try {
            RackBinMasterEntity rejectedRackBin = service.rejectRackBin(id);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "❌ Rack-Bin rejected successfully");
            response.put("id", rejectedRackBin.getId());
            response.put("rackNo", rejectedRackBin.getRackNo() != null ? rejectedRackBin.getRackNo() : "");
            response.put("binNo", rejectedRackBin.getBinNo() != null ? rejectedRackBin.getBinNo() : "");
            response.put("status", rejectedRackBin.getStatus() != null ? rejectedRackBin.getStatus() : "REJECTED");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to reject Rack-Bin",
                    "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<?> bulkUpload(@RequestBody List<RackBinMasterDto> dtoList) {
        try {
            List<RackBinMasterEntity> savedEntities = service.bulkCreate(dtoList);
            return ResponseEntity.ok(Map.of(
                    "message", "Bulk upload successful",
                    "totalSaved", savedEntities.size(),
                    "data", savedEntities
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Bulk upload failed",
                    "details", e.getMessage()
            ));
        }
    }

    @GetMapping("/by-storage-area/{storageArea}")
    public ResponseEntity<?> getByStorageArea(@PathVariable String storageArea) {
        try {
            List<RackBinMasterEntity> rackBins = service.getByStorageArea(storageArea);
            if (rackBins.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "No Rack-Bin found for storage area: " + storageArea));
            }
            return ResponseEntity.ok(rackBins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to fetch Rack-Bin by storage area",
                    "details", e.getMessage()
            ));
        }
    }

    @PostMapping(value = "/upload-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadExcel(@RequestPart("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                        "status", "error",
                        "message", "File is empty or not provided"
                ));
            }
            Map<String, Object> result = service.uploadExcelData(file);
            if ("error".equals(result.get("status"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAll() {
        try {
            service.deleteAll();
            return ResponseEntity.ok(Map.of("message", "All Rack-Bin records deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "error", "Failed to delete all records",
                    "details", e.getMessage()
            ));
        }
    }


    @GetMapping("/eligible")
    public ResponseEntity<?> getEligibleBins(
            @RequestParam String itemCategory,
            @RequestParam Double bundleNetWeight,
            @RequestParam String store,
            @RequestParam String unitName) {
        try {
            Map<String, Object> result = service.findBestEligibleBin(itemCategory, bundleNetWeight, store, unitName);

            // Check if it's a success response (has binId) or error response (has errorCode)
            if (result.containsKey("binId")) {
                return ResponseEntity.ok(result);
            } else {
                // Error response - return appropriate HTTP status based on error code
                String errorCode = (String) result.get("errorCode");
                HttpStatus status = switch (errorCode) {
                    case "INVALID_STORE", "INVALID_CATEGORY", "INVALID_WEIGHT" -> HttpStatus.BAD_REQUEST;
                    case "NO_BINS_FOR_STORE", "NO_BINS_FOR_CATEGORY", "NO_BINS_WITH_CAPACITY" -> HttpStatus.NOT_FOUND;
                    default -> HttpStatus.INTERNAL_SERVER_ERROR;
                };
                return ResponseEntity.status(status).body(result);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error: " + e.getMessage());
            errorResponse.put("errorCode", "INTERNAL_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/update-current-storage")
    public ResponseEntity<String> updateCurrentStorage(@RequestBody RackBinStorageQtyUpdateDto dto) {
        service.updateCurrentStorage(dto);
        return ResponseEntity.ok("Storage quantity updated successfully");
    }

}


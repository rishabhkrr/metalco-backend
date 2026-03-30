package com.indona.invento.controllers;
import com.indona.invento.dto.SupplierFilterRequest;
import com.indona.invento.dto.SupplierMasterDto;
import com.indona.invento.entities.SupplierMasterEntity;
import com.indona.invento.services.SupplierMasterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/supplier-master")
public class SupplierMasterController {

    @Autowired
    private SupplierMasterService supplierMasterService;

    @PostMapping("/create")
    public ResponseEntity<?> createSupplier(@RequestBody SupplierMasterDto dto) {
        try {
            SupplierMasterEntity saved = supplierMasterService.saveSupplier(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Map<String, String>> deleteSupplier(@PathVariable Long id) {
        try {
            supplierMasterService.deleteSupplierById(id);
            return ResponseEntity.ok(Map.of("message", "Supplier deleted successfully with ID: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Supplier not found with ID: " + id));
        }
    }


    @GetMapping("/all")
    public ResponseEntity<?> getAllSuppliers() {
        try {
            List<SupplierMasterEntity> suppliers = supplierMasterService.getAllSuppliersWithoutPagination();
            return ResponseEntity.ok(suppliers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }




    @GetMapping("/{id}")
    public ResponseEntity<?> getSupplierById(@PathVariable Long id) {
        try {
            SupplierMasterEntity supplier = supplierMasterService.getSupplierById(id);
            return ResponseEntity.ok(supplier);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSupplier(
            @PathVariable Long id,
            @RequestBody SupplierMasterDto dto
    ) {
        try {
            SupplierMasterEntity updatedEntity = supplierMasterService.updateSupplier(id, dto);
            return ResponseEntity.ok(updatedEntity);
        } catch (Exception e) {
            log.error("Error during supplier update for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to update supplier. Please check the payload format and values.");
        }
    }

    @GetMapping("/fetch-by-name/{supplierName}")
    public Map<String, String> getSupplierCodeByName(@PathVariable String supplierName) {
        String code = supplierMasterService.getSupplierCodeByName(supplierName);
        return Collections.singletonMap("supplierCode", code);
    }

    @GetMapping("/fetch-by-code/{supplierCode}")
    public Map<String, String> getSupplierNameByCode(@PathVariable String supplierCode) {
        String name = supplierMasterService.getSupplierNameByCode(supplierCode);
        return Collections.singletonMap("supplierName", name);
    }

    @GetMapping("/allNames")
    public List<String> getAllSupplierNames() {
        return supplierMasterService.getAllSupplierNames();
    }

    @GetMapping("/allCodes")
    public List<String> getAllSupplierCodes() {
        return supplierMasterService.getAllSupplierCodes();
    }

//    @GetMapping("/filter")
//    public ResponseEntity<List<SupplierMasterEntity>> filterByGstType(
//            @RequestParam String gstType,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size) {
//
//        Page<SupplierMasterEntity> result = supplierMasterService.filterByGstType(gstType, page, size);
//        return ResponseEntity.ok(result.getContent()); // ✅ Only content returned
//    }

    @GetMapping("/filter-by-category")
    public ResponseEntity<List<SupplierMasterEntity>> filterByCategory(
            @RequestParam String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SupplierMasterEntity> result = supplierMasterService.filterByCategory(category, page, size);
        return ResponseEntity.ok(result.getContent()); // ✅ No extra metadata
    }


    @GetMapping("/filter-by-name")
    public ResponseEntity<List<SupplierMasterEntity>> filterByName(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SupplierMasterEntity> result = supplierMasterService.filterByName(name, page, size);
        return ResponseEntity.ok(result.getContent());
    }

    @GetMapping("/filter-by-code")
    public ResponseEntity<List<SupplierMasterEntity>> filterByCode(
            @RequestParam String code,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SupplierMasterEntity> result = supplierMasterService.filterByCode(code, page, size);
        return ResponseEntity.ok(result.getContent());
    }

    @GetMapping("/filter-by-nickname")
    public ResponseEntity<List<SupplierMasterEntity>> filterByNickname(
            @RequestParam String nickname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<SupplierMasterEntity> result = supplierMasterService.filterByNickname(nickname, page, size);
        return ResponseEntity.ok(result.getContent());
    }


    @GetMapping("/filter")
    public ResponseEntity<List<SupplierMasterEntity>> filterSuppliers(
            @RequestParam(required = false) String gstType,
            @RequestParam(required = false) String supplierCategory,
            @RequestParam(required = false) String supplierType,
            @RequestParam(required = false) String supplierCode,
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String mailingBillingName,
            @RequestParam(required = false) String supplierNickname,
            @RequestParam(required = false) Boolean multipleAddress,
            @RequestParam(required = false) String gstOrUin,
            @RequestParam(required = false) String gstStateCode,
            @RequestParam(required = false) String pan,
            @RequestParam(required = false) String isTanAvailable,
            @RequestParam(required = false) String tanNumber,
            @RequestParam(required = false) String isUdyamAvailable,
            @RequestParam(required = false) String udyamNumber,
            @RequestParam(required = false) String isIecAvailable,
            @RequestParam(required = false) String iecCode,
            @RequestParam(required = false) String interestCalculation,
            @RequestParam(required = false) Double rateOfInterest,
            @RequestParam(required = false) String brand,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        SupplierFilterRequest filter = new SupplierFilterRequest();
        filter.setGstRegistrationType(gstType);
        filter.setSupplierCategory(supplierCategory);
        filter.setSupplierType(supplierType);
        filter.setSupplierCode(supplierCode);
        filter.setSupplierName(supplierName);
        filter.setMailingBillingName(mailingBillingName);
        filter.setSupplierNickname(supplierNickname);
        filter.setMultipleAddress(multipleAddress);
        filter.setGstOrUin(gstOrUin);
        filter.setGstStateCode(gstStateCode);
        filter.setPan(pan);
        filter.setIsTanAvailable(isTanAvailable);
        filter.setTanNumber(tanNumber);
        filter.setIsUdyamAvailable(isUdyamAvailable);
        filter.setUdyamNumber(udyamNumber);
        filter.setIsIecAvailable(isIecAvailable);
        filter.setIecCode(iecCode);
        filter.setInterestCalculation(interestCalculation);
        filter.setRateOfInterest(rateOfInterest);
        filter.setBrand(brand);

        Pageable pageable = PageRequest.of(page, size);
        Page<SupplierMasterEntity> result = supplierMasterService.filterSuppliers(filter, pageable);

        return ResponseEntity.ok(result.getContent());
    }


    @GetMapping("/checkName")
    public ResponseEntity<Map<String, Object>> checkSupplierName(@RequestParam String name) {
        boolean exists = supplierMasterService.isSupplierNameExists(name);
        return ResponseEntity.ok(Map.of(
                "supplierName", name,
                "exists", exists
        ));
    }



    @GetMapping("/brand-suppliers")
    public ResponseEntity<List<Map<String, String>>> getSuppliersByBrand(@RequestParam String brand) {
        List<Map<String, String>> result = supplierMasterService.getSupplierNamesAndCodesByBrand(brand);
        return ResponseEntity.ok(result);
    }



    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<SupplierMasterEntity>> getAllSuppliersWithoutPagination() {
        try {
            List<SupplierMasterEntity> suppliers = supplierMasterService.getAllSuppliersWithoutPagination();
            return ResponseEntity.ok(suppliers);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/get-by-code/{code}")
    public ResponseEntity<SupplierMasterEntity> getSupplierByCode(@PathVariable("code") String code) {
        SupplierMasterEntity supplier = supplierMasterService.getSupplierByCode(code);
        return ResponseEntity.ok(supplier);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveSupplier(@PathVariable Long id) {
        try {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   ✅ APPROVING SUPPLIER                ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            System.out.println("📍 Supplier ID: " + id);

            SupplierMasterEntity approvedSupplier = supplierMasterService.approveSupplier(id);

            System.out.println("✅ Supplier approved successfully!");
            System.out.println("   Supplier Code: " + approvedSupplier.getSupplierCode());
            System.out.println("   Supplier Name: " + approvedSupplier.getSupplierName());
            System.out.println("   Status: " + approvedSupplier.getStatus());

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Supplier approved successfully",
                    "supplierCode", approvedSupplier.getSupplierCode(),
                    "supplierName", approvedSupplier.getSupplierName(),
                    "status", approvedSupplier.getStatus()
            ));
        } catch (Exception e) {
            System.out.println("❌ Error approving supplier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to approve supplier",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectSupplier(@PathVariable Long id) {
        try {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║   ❌ REJECTING SUPPLIER                ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            System.out.println("📍 Supplier ID: " + id);

            SupplierMasterEntity rejectedSupplier = supplierMasterService.rejectSupplier(id);

            System.out.println("❌ Supplier rejected successfully!");
            System.out.println("   Supplier Code: " + rejectedSupplier.getSupplierCode());
            System.out.println("   Supplier Name: " + rejectedSupplier.getSupplierName());
            System.out.println("   Status: " + rejectedSupplier.getStatus());

            return ResponseEntity.ok(Map.of(
                    "message", "❌ Supplier rejected successfully",
                    "supplierCode", rejectedSupplier.getSupplierCode(),
                    "supplierName", rejectedSupplier.getSupplierName(),
                    "status", rejectedSupplier.getStatus()
            ));
        } catch (Exception e) {
            System.out.println("❌ Error rejecting supplier: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "error", "Failed to reject supplier",
                    "details", e.getMessage()
            ));
        }
    }

}

package com.indona.invento.controllers;

import com.indona.invento.dao.BillingSummaryRepository;
import com.indona.invento.dto.*;
import com.indona.invento.entities.BillingSummaryEntity;
import com.indona.invento.entities.GradeEntity;
import com.indona.invento.entities.ItemEnquiry;
import com.indona.invento.entities.TemperEntity;
import com.indona.invento.services.GradeService;
import com.indona.invento.services.ItemEnquiryService;
import com.indona.invento.services.TemperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item-enquiries")
public class  ItemEnquiryController {

    @Autowired
    private ItemEnquiryService itemEnquiryService;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private TemperService temperService;

    @Autowired
    private BillingSummaryRepository billingSummaryRepository;

    @PostMapping("/create")
    public ResponseEntity<ItemEnquiry> create(@RequestBody ItemEnquiryDTO dto) {
        ItemEnquiry saved = itemEnquiryService.create(dto);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/getall")
    public ResponseEntity<List<ItemEnquiry>> getAll(
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        List<ItemEnquiry> result;

        if (from != null && to != null) {
            result = itemEnquiryService.getEnquiriesBetweenDatesWithoutPagination(from, to);
        } else {
            result = itemEnquiryService.getAllWithoutPagination();
        }

        return ResponseEntity.ok(result);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ItemEnquiry> getById(@PathVariable Long id) {
        return itemEnquiryService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemEnquiryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ItemEnquiry> update(@PathVariable Long id, @RequestBody ItemEnquiryDTO dto) {
        ItemEnquiry updated = itemEnquiryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/check-stock-list")
    public ResponseEntity<AvailableStockDto> getAvailableStockList(@RequestBody ItemEnquiryStockCheckDto dto) {
        System.out.println("========== CHECK-STOCK-LIST API CALLED ==========");
        System.out.println("📥 Request received: " + dto);
        System.out.println("📥 productCategory: " + dto.getProductCategory());
        System.out.println("📥 itemDescription: " + dto.getItemDescription());
        System.out.println("📥 brand: " + dto.getBrand());
        System.out.println("📥 grade: " + dto.getGrade());
        System.out.println("📥 temper: " + dto.getTemper());
        System.out.println("📥 materialType: " + dto.getMaterialType());
        System.out.println("📥 dimension: " + dto.getDimension());
        System.out.println("📥 requiredQuantity: " + dto.getRequiredQuantity());
        System.out.println("📥 uom: " + dto.getUom());

        AvailableStockDto availableItem = itemEnquiryService.getAvailableStockList(
                dto.getProductCategory(),
                dto.getItemDescription(),
                dto.getBrand(),
                dto.getGrade(),
                dto.getTemper(),
                dto.getMaterialType(),
                dto.getDimension(),
                dto.getRequiredQuantity(),
                dto.getUom(),
                dto.getUnit(),
                dto.getOrderType()
        );

        System.out.println("📤 Response: " + availableItem);
        System.out.println("========== CHECK-STOCK-LIST API END ==========");
        return ResponseEntity.ok(availableItem); // ✅ return single aggregated DTO
    }






    @PostMapping("/grades/add")
    public ResponseEntity<?> addGrade(@RequestBody GradeDto dto) {
        try {
            GradeEntity saved = gradeService.addGrade(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/grades/all")
    public ResponseEntity<List<GradeDto>> getAllGrades() {
        return ResponseEntity.ok(gradeService.getAllGrades());
    }

    @PostMapping("/tempers/add")
    public ResponseEntity<?> addTemper(@RequestBody TemperDto dto) {
        try {
            TemperEntity saved = temperService.addTemper(dto);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/tempers/all")
    public ResponseEntity<List<TemperDto>> getAllTempers() {
        return ResponseEntity.ok(temperService.getAllTempers());
    }


    @PutMapping("/cancel/{id}")
    public ResponseEntity<ItemEnquiry> cancelEnquiry(@PathVariable Long id) {
        ItemEnquiry updated = itemEnquiryService.cancelAndDeleteEnquiry(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/mark-received")
    public ResponseEntity<?> markStatusAsReceived(@RequestParam String quotationNo) {
        try {
            ItemEnquiry updated = itemEnquiryService.markStatusAsReceived(quotationNo);
            return ResponseEntity.ok(Map.of(
                    "quotationNo", updated.getQuotationNo(),
                    "status", updated.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/status-summary")
    public ResponseEntity<Map<String, Long>> getStatusSummary() {
        Map<String, Long> summary = itemEnquiryService.getStatusSummary();
        return ResponseEntity.ok(summary);
    }

    @GetMapping("/item-info")
    public ResponseEntity<?> getItemInfo(@RequestParam String itemDescription) {
        try {
            Map<String, String> itemInfo = itemEnquiryService.getItemInfoByDescription(itemDescription);
            return ResponseEntity.ok(itemInfo);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/update-remarks")
    public ResponseEntity<?> updateRemarksByQuotationNo(
            @RequestParam String quotationNo,
            @RequestParam String remarks
    ) {
        try {
            ItemEnquiry updated = itemEnquiryService.updateRemarks(quotationNo, remarks);
            return ResponseEntity.ok(Map.of(
                    "quotationNo", updated.getQuotationNo(),
                    "updatedRemarks", updated.getRemarks()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }



    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllEnquiries() {
        itemEnquiryService.deleteAllEnquiries();
        return ResponseEntity.ok("All item enquiries deleted successfully.");
    }


    @GetMapping("/price-detail")
    public ResponseEntity<?> getPriceDetail(
            @RequestParam String itemDescription,
            @RequestParam String orderType) {
        try {
            Map<String, Object> response = itemEnquiryService.getPriceDetail(itemDescription, orderType);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/available-quantity")
    public ResponseEntity<?> getAvailableQuantity(
            @RequestParam String itemDescription,
            @RequestParam String unit) {
        Map<String, Object> response = itemEnquiryService.getAvailableQuantity(itemDescription, unit);
        return ResponseEntity.ok(response);
    }

    /**
     * Approve an item enquiry - sets approvalStatus to APPROVED
     * Usage: PUT /api/metalco/item-enquiries/approve/{id}
     */
    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveEnquiry(@PathVariable Long id) {
        try {
            ItemEnquiry updated = itemEnquiryService.approveEnquiry(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Item Enquiry approved successfully",
                    "id", updated.getId(),
                    "quotationNo", updated.getQuotationNo(),
                    "approvalStatus", updated.getApprovalStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Reject an item enquiry - sets approvalStatus to REJECTED
     * Usage: PUT /api/metalco/item-enquiries/reject/{id}
     */
    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectEnquiry(@PathVariable Long id) {
        try {
            ItemEnquiry updated = itemEnquiryService.rejectEnquiry(id);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Item Enquiry rejected successfully",
                    "id", updated.getId(),
                    "quotationNo", updated.getQuotationNo(),
                    "approvalStatus", updated.getApprovalStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}

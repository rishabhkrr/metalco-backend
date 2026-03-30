package com.indona.invento.controllers;

import com.indona.invento.dto.SOApprovalRequestDTO;
import com.indona.invento.dto.SalesOrderAddressCreditDTO;
import com.indona.invento.dto.SalesOrderDTO;
import com.indona.invento.dto.SalesOrderLineItemDetailsDto;
import com.indona.invento.entities.SalesAuthority;
import com.indona.invento.entities.SalesOrder;
import com.indona.invento.entities.SalesOrderLineItem;
import com.indona.invento.services.SalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sales-order")
@RequiredArgsConstructor
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @PostMapping("/create")
    public ResponseEntity<SalesOrder> createSalesOrder(@RequestBody SalesOrderDTO dto) {
        SalesOrder savedOrder = salesOrderService.createSalesOrder(dto);
       salesOrderService.storePurchaseFollowUpsV2(savedOrder);
        return ResponseEntity.ok(savedOrder);
    }




    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateSalesOrder(@PathVariable Long id, @RequestBody SalesOrderDTO dto) {
        try {
            SalesOrder updated = salesOrderService.updateSalesOrder(id, dto);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrder> getSalesOrder(@PathVariable Long id) {
        SalesOrder order = salesOrderService.getSalesOrderById(id);
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<SalesOrder> deleteSalesOrder(@PathVariable Long id) {
        SalesOrder deleted = salesOrderService.deleteSalesOrder(id);
        return ResponseEntity.ok(deleted);
    }

//    @PutMapping("/cancel/{id}")
//    public ResponseEntity<SalesOrder> cancelAndDeleteSalesOrder(@PathVariable Long id) {
//        SalesOrder cancelled = salesOrderService.cancelAndDeleteSalesOrder(id);
//        return ResponseEntity.ok(cancelled);
//    }

    @GetMapping("/all")
    public ResponseEntity<List<SalesOrder>> getAllSalesOrder() {
        List<SalesOrder> result = salesOrderService.getAllSalesOrdersWithoutPagination();
        return ResponseEntity.ok(result);
    }



    @GetMapping("/all-with-Pagination")
    public ResponseEntity<?> getAllSalesOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
            Page<SalesOrder> result = salesOrderService.getAllSalesOrder(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("data", result.getContent());
            response.put("totalElements", result.getTotalElements());
            response.put("totalPages", result.getTotalPages());
            response.put("currentPage", result.getNumber());
            response.put("pageSize", result.getSize());
            response.put("isLastPage", result.isLast());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/authorities")
    public ResponseEntity<List<SalesAuthority>> getAllSalesAuthorities() {
        return ResponseEntity.ok(salesOrderService.getAllSalesAuthorities());
    }

    @PostMapping("/authorities")
    public ResponseEntity<SalesAuthority> addSalesAuthority(@RequestBody SalesAuthority authority) {
        SalesAuthority saved = salesOrderService.addSalesAuthority(authority.getName());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/orderNo")
    public ResponseEntity<?> getSalesOrderBySoNumber(@RequestParam String orderNo) {
        try {
            SalesOrder order = salesOrderService.getSalesOrderBySoNumber(orderNo);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/cancel")
    public ResponseEntity<?> cancelSalesOrderStatus(@RequestParam String soNumber) {
        try {
            SalesOrder updated = salesOrderService.cancelSalesOrder(soNumber);
            return ResponseEntity.ok(Map.of(
                    "orderNo", updated.getSoNumber(),
                    "status", updated.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/reject")
    public ResponseEntity<?> rejectSalesOrder(@RequestParam String soNumber) {
        try {
            SalesOrder order = salesOrderService.updateStatus(soNumber, "Rejected");
            return ResponseEntity.ok(Map.of(
                    "soNumber", order.getSoNumber(),
                    "status", order.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/approve")
    public ResponseEntity<?> approveSalesOrder(@RequestParam String soNumber) {
        try {
            SalesOrder order = salesOrderService.approveSalesOrderAfterCustomerClick(soNumber);

            return ResponseEntity.ok(Map.of(
                    "soNumber", order.getSoNumber(),
                    "status", order.getStatus()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @PutMapping("/line-items/status")
    public ResponseEntity<?> updateMultipleLineItemStatuses(
            @RequestBody Map<String, Object> payload
    ) {
        try {
            List<String> lineNumbers = (List<String>) payload.get("lineNumbers");
            String status = (String) payload.get("status");
            String soNumber = (String) payload.get("soNumber");

            Map<String, Object> result = salesOrderService.updateMultipleLineItemStatuses(lineNumbers, status, soNumber);

            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/orderNo/view")
    public ResponseEntity<?> viewSalesOrderBySoNumber(@RequestParam String orderNo) {
        try {
            SalesOrder order = salesOrderService.viewSalesOrderBySoNumber(orderNo);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }



    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllSalesOrders() {
        salesOrderService.deleteAllSalesOrders();
        return ResponseEntity.ok("All sales orders deleted successfully.");
    }

    // ========== SO Approval for Overdue Customers ==========

    @GetMapping("/pending-overdue")
    public ResponseEntity<?> getPendingOverdueSalesOrders() {
        try {
            List<SalesOrder> result = salesOrderService.getPendingOverdueSalesOrders();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/approve-overdue/{id}")
    public ResponseEntity<?> approveSalesOrderOverdue(
            @PathVariable Long id,
            @RequestBody SOApprovalRequestDTO dto
    ) {
        try {
            SalesOrder approved = salesOrderService.approveSalesOrder(id, dto.getApprovalRemarks());
            return ResponseEntity.ok(Map.of(
                    "message", "✅ Sales Order approved successfully",
                    "soNumber", approved.getSoNumber(),
                    "status", approved.getStatus(),
                    "approvalRemarks", approved.getApprovalRemarks()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/reject-overdue/{id}")
    public ResponseEntity<?> rejectSalesOrderOverdue(
            @PathVariable Long id,
            @RequestBody SOApprovalRequestDTO dto
    ) {
        try {
            SalesOrder rejected = salesOrderService.rejectSalesOrder(id, dto.getApprovalRemarks());
            return ResponseEntity.ok(Map.of(
                    "message", "❌ Sales Order rejected",
                    "soNumber", rejected.getSoNumber(),
                    "status", rejected.getStatus(),
                    "approvalRemarks", rejected.getApprovalRemarks()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/so/address-credit")
    public ResponseEntity<SalesOrderAddressCreditDTO> getAddressAndCredit(
            @RequestParam String soNumber,
            @RequestParam String lineNumber) {
        SalesOrderAddressCreditDTO dto = salesOrderService.getAddressAndCreditDetails(soNumber, lineNumber);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/line-item-details")
    public ResponseEntity<?> getLineItemDetails(
            @RequestParam String soNumber,
            @RequestParam String lineNumber) {
        try {
            var lineItemDetails = salesOrderService.getLineItemDetailsBySoAndLineNumber(soNumber, lineNumber);
            return ResponseEntity.ok(lineItemDetails);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

}


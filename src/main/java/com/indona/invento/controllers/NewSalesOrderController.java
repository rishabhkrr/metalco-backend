package com.indona.invento.controllers;

import com.indona.invento.dto.NewSalesOrderDTO;
import com.indona.invento.entities.NewSalesOrder;
import com.indona.invento.services.NewSalesOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/new-sales-order")
@RequiredArgsConstructor
public class NewSalesOrderController {

    private final NewSalesOrderService newSalesOrderService;

    @PostMapping("/create")
    public ResponseEntity<NewSalesOrder> createNewSalesOrder(@RequestBody NewSalesOrderDTO dto) {
        NewSalesOrder savedOrder = newSalesOrderService.createNewSalesOrder(dto);
        return ResponseEntity.ok(savedOrder);
    }

    @PutMapping("/update/{soNumber}")
    public ResponseEntity<NewSalesOrder> updateSalesOrder(
            @PathVariable String soNumber,
            @RequestBody NewSalesOrderDTO dto) {

        NewSalesOrder updatedOrder = newSalesOrderService.updateSalesOrder(soNumber, dto);
        return ResponseEntity.ok(updatedOrder);
    }


    @GetMapping("/{soNumber}")
    public ResponseEntity<?> getSalesOrderBySoNumber(@PathVariable String soNumber) {
        try {
            NewSalesOrder order = newSalesOrderService.getSalesOrderBySoNumber(soNumber);
            return ResponseEntity.ok(order);   // ✅ full entity with items
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}

package com.indona.invento.controllers;

import com.indona.invento.dto.SalesOrderDTO;
import com.indona.invento.dto.SalesOrderLineItemDTO;
import com.indona.invento.entities.SalesOrder;
import com.indona.invento.entities.SoUpdate;
import com.indona.invento.entities.SoUpdateItem;
import com.indona.invento.services.SalesOrderService;
import com.indona.invento.services.SoUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/so-update")
@RequiredArgsConstructor
public class SoUpdateController {

    private final SoUpdateService soUpdateService;

    private final SalesOrderService salesOrderService;

    @GetMapping("/all")
    public ResponseEntity<List<SoUpdate>> getAllSoUpdates() {
        List<SoUpdate> updates = soUpdateService.getAllSoUpdates();
        return ResponseEntity.ok(updates);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllSoUpdates() {
        try {
            soUpdateService.deleteAllSoUpdates();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All SO updates deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all SO updates",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/verify")
    public ResponseEntity<?> verifySoUpdate(
            @RequestParam String soNumber,
            @RequestBody SalesOrderDTO dto) {
        try {
            // 1️⃣ Verify SO Update
            SoUpdate updated = soUpdateService.verifyStatus(soNumber);

            // 2️⃣ Update only orderMode & productionStrategy in existing items
            if (dto.getItems() != null && !dto.getItems().isEmpty() && updated.getItems() != null) {
                for (int i = 0; i < dto.getItems().size() && i < updated.getItems().size(); i++) {
                    SalesOrderLineItemDTO dtoItem = dto.getItems().get(i);
                    SoUpdateItem existingItem = updated.getItems().get(i);

                    // Sirf ye do fields update karo
                    existingItem.setOrderMode(dtoItem.getOrderMode());
                    existingItem.setProductionStrategy(dtoItem.getProductionStrategy());
                }
            }

            // Save updated SoUpdate (baaki fields untouched rahenge)
            soUpdateService.save(updated);

            // 3️⃣ Create new SalesOrder with DTO
            SalesOrder savedOrder = salesOrderService.createSalesOrder(dto);

            // 4️⃣ Trigger purchase follow-ups
            salesOrderService.storePurchaseFollowUpsV2(savedOrder);

            // 5️⃣ Response
            return ResponseEntity.ok(Map.of(
                    "verifiedSoNumber", updated.getSoNumber(),
                    "updateStatus", updated.getStatus(),
                    "newSalesOrderNumber", savedOrder.getSoNumber(),
                    "soUpdateItemsCount", updated.getItems().size()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }


}

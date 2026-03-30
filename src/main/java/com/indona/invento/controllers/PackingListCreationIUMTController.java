package com.indona.invento.controllers;

import com.indona.invento.entities.PackingListCreationIUMTEntity;
import com.indona.invento.services.PackingListCreationIUMTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/packing-list-iumt")
public class PackingListCreationIUMTController {

    @Autowired
    private PackingListCreationIUMTService packingListCreationIUMTService;

    @PostMapping("/create")
    public ResponseEntity<List<PackingListCreationIUMTEntity>> createPackingListIUMT(@RequestBody List<PackingListCreationIUMTEntity> entities) {
        List<PackingListCreationIUMTEntity> created = packingListCreationIUMTService.createPackingListIUMT(entities);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/all")
    public ResponseEntity<List<PackingListCreationIUMTEntity>> getAllPackingListsIUMT() {
        List<PackingListCreationIUMTEntity> allEntities = packingListCreationIUMTService.getAllPackingListsIUMT();
        return ResponseEntity.ok(allEntities);
    }

    @GetMapping("/packing-list-no")
    public ResponseEntity<Map<String, Object>> getPackingListNo() {
        List<Map<String, String>> rm = packingListCreationIUMTService.getPackingListNo();
        return ResponseEntity.ok(Map.of("data", rm, "status", "success"));
    }

    @GetMapping("/get-by-packing-list-no")
    public ResponseEntity<List<PackingListCreationIUMTEntity>> getByPackingListNo(@RequestParam String packingListNo) {
        List<PackingListCreationIUMTEntity> entity = packingListCreationIUMTService.getByPackingListNo(packingListNo);
        return ResponseEntity.ok(entity);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllPackingLists() {
        try {
            packingListCreationIUMTService.deleteAllPackingLists();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All packing lists deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all packing lists",
                    "details", e.getMessage()
            ));
        }
    }
}

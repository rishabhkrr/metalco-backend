package com.indona.invento.controllers;

import com.indona.invento.dto.GateEntryEditDTO;
import com.indona.invento.dto.GateEntryPackingAndDispatchRequestDTO;
import com.indona.invento.dto.GateEntryUpdateDTO;
import com.indona.invento.entities.GateEntryPackingAndDispatch;
import com.indona.invento.services.GateEntryPackingAndDispatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/gate-entry-packing-and-dispatch")
public class GateEntryPackingAndDispatchController {

    @Autowired
    private GateEntryPackingAndDispatchService service;

    @PostMapping("/create")
    public ResponseEntity<GateEntryPackingAndDispatch> createGateEntry(
            @RequestBody GateEntryPackingAndDispatchRequestDTO dto) {

        GateEntryPackingAndDispatch savedEntry = service.saveGateEntry(dto);
        return ResponseEntity.ok(savedEntry);
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<GateEntryPackingAndDispatch>> getAllGateEntries() {
        List<GateEntryPackingAndDispatch> entries = service.getAllGateEntries();
        return ResponseEntity.ok(entries);
    }
    
 // Dropdown API — returns only vehicle numbers whose OutStatus = IN
    @GetMapping("/vehicle-dropdown")
    public ResponseEntity<List<String>> getVehicleNumbersWithInStatus() {
        return ResponseEntity.ok(service.getVehicleNumbersWithInStatus());
    }

    @PutMapping("/gate-entry/update/{refNo}")
    public ResponseEntity<GateEntryPackingAndDispatch> updateGateEntry(
            @PathVariable String refNo,
            @RequestBody GateEntryUpdateDTO dto) {
        GateEntryPackingAndDispatch updated = service.updateGateEntryByRefNo(refNo, dto);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/gate-entry/mark-out/{refNo}")
    public ResponseEntity<GateEntryPackingAndDispatch> markVehicleOut(@PathVariable String refNo) {
        GateEntryPackingAndDispatch updated = service.markVehicleOut(refNo);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/get-by-ref/{refNo}")
    public ResponseEntity<GateEntryPackingAndDispatch> getGateEntryByRefNo(@PathVariable String refNo) {
        GateEntryPackingAndDispatch entry = service.getGateEntryByRefNo(refNo);
        return ResponseEntity.ok(entry);
    }

    @PutMapping("/edit/{refNo}")
    public ResponseEntity<GateEntryPackingAndDispatch> editGateEntry(
            @PathVariable String refNo,
            @RequestBody GateEntryEditDTO dto) {
        GateEntryPackingAndDispatch updated = service.editGateEntryByRefNo(refNo, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllGateEntries() {
        try {
            service.deleteAllGateEntries();
            return ResponseEntity.ok(Map.of(
                    "message", "✅ All gate entries deleted successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "❌ Failed to delete all gate entries",
                    "details", e.getMessage()
            ));
        }
    }

}

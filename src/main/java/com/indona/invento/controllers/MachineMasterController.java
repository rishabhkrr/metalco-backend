package com.indona.invento.controllers;

import com.indona.invento.dto.MachineMasterDto;
import com.indona.invento.entities.MachineMasterEntity;
import com.indona.invento.services.MachineMasterService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/machines")
public class MachineMasterController {

    private final MachineMasterService machineService;

    @Autowired
    public MachineMasterController(MachineMasterService machineService) {
        this.machineService = machineService;
    }

    // ✅ Create new machine
    @PostMapping
    public ResponseEntity<MachineMasterEntity> createMachine(@RequestBody MachineMasterDto dto) {
        MachineMasterEntity saved = machineService.createMachine(dto);
        return ResponseEntity.ok(saved);
    }

    // 🔍 Get machine by ID
    @GetMapping("/{id}")
    public ResponseEntity<MachineMasterEntity> getMachineById(@PathVariable Long id) {
        MachineMasterEntity machine = machineService.getMachineById(id);
        return ResponseEntity.ok(machine);
    }

    // 📃 Get all machines
    @GetMapping("/all")
    public ResponseEntity<?> getAllMachines() {
        try {
            List<MachineMasterEntity> machines = machineService.getAllMachinesWithoutPagination();
            return ResponseEntity.ok(machines);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<MachineMasterEntity> deleteMachine(@PathVariable Long id) {
        try {
            MachineMasterEntity deletedMachine = machineService.deleteMachine(id);
            return ResponseEntity.ok(deletedMachine);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<MachineMasterEntity> updateMachine(@PathVariable Long id, @RequestBody MachineMasterDto dto) {
        MachineMasterEntity updatedMachine = machineService.updateMachine(id, dto);
        return ResponseEntity.ok(updatedMachine);
    }

    @GetMapping("/all-no-pagination")
    public ResponseEntity<List<MachineMasterEntity>> getAllMachinesWithoutPagination() {
        try {
            List<MachineMasterEntity> machines = machineService.getAllMachinesWithoutPagination();
            return ResponseEntity.ok(machines);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/by-unit")
    public ResponseEntity<List<MachineMasterEntity>> getMachinesByUnit(@RequestParam String unitCode) {
        try {
            List<MachineMasterEntity> machines = machineService.getMachinesByUnitCode(unitCode);
            return ResponseEntity.ok(machines);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<String> deleteAllMachines() {
        machineService.deleteAllMachines();
        return ResponseEntity.ok("✅ All machines deleted successfully");
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveMachine(@PathVariable Long id) {
        try {
            MachineMasterEntity approvedMachine = machineService.approveMachine(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "✅ Machine approved successfully",
                    "id", approvedMachine.getId(),
                    "machineName", approvedMachine.getMachineName(),
                    "status", approvedMachine.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to approve machine",
                    "details", e.getMessage()
            ));
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> rejectMachine(@PathVariable Long id) {
        try {
            MachineMasterEntity rejectedMachine = machineService.rejectMachine(id);
            return ResponseEntity.ok(java.util.Map.of(
                    "message", "❌ Machine rejected successfully",
                    "id", rejectedMachine.getId(),
                    "machineName", rejectedMachine.getMachineName(),
                    "status", rejectedMachine.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "error", "Failed to reject machine",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Get all APPROVED machine names
     * Usage: GET /machines/approved-names
     */
    @GetMapping("/approved-names")
    public ResponseEntity<?> getApprovedMachineNames() {
        try {
            List<String> approvedMachines = machineService.getApprovedMachineNames();
            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "totalCount", approvedMachines.size(),
                    "data", approvedMachines
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "success", false,
                    "error", "Failed to fetch approved machines",
                    "details", e.getMessage()
            ));
        }
    }

    /**
     * Get machine cutting config by machine name
     * Returns idealBladeSpeed and idealCuttingFeed
     * Usage: GET /machines/cutting-config?machineName=MACHINE-1
     */
    @GetMapping("/cutting-config")
    public ResponseEntity<?> getMachineCuttingConfig(@RequestParam String machineName) {
        try {
            java.util.Map<String, Object> config = machineService.getMachineCuttingConfig(machineName);

            if (config == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(java.util.Map.of(
                        "success", false,
                        "error", "Machine not found: " + machineName
                ));
            }

            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "data", config
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(java.util.Map.of(
                    "success", false,
                    "error", "Failed to fetch cutting config",
                    "details", e.getMessage()
            ));
        }
    }
}

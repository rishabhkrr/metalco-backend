package com.indona.invento.controllers;

import com.indona.invento.dto.MachineMaintenanceFilterDto;
import com.indona.invento.entities.MachineMaintenanceActivityEntity;
import com.indona.invento.services.MachineMaintenanceActivityService;
import com.indona.invento.services.MachineMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/machine-maintenance")
@RequiredArgsConstructor
public class MachineMaintenanceActivityController {

    private final MachineMaintenanceActivityService service;
    private final MachineMasterService machineMasterService;

    @GetMapping("/machines") // for dropdown
    public List<?> getAllMachines() {
        return machineMasterService.getAllMachinesWithoutPagination();
    }

    @PostMapping("/start")
    public MachineMaintenanceActivityEntity startBreakdown(
            @RequestParam(required = false) String machineName,
            @RequestBody(required = false) Map<String, Object> body) {

        // If UI sends JSON (not query param)
        if ((machineName == null || machineName.isBlank()) && body != null) {
            machineName = (String) body.get("machineName");
        }

        if (machineName == null || machineName.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "machineName is required"
            );
        }

        return service.startBreakdown(machineName, null, null);
    }


    @PostMapping("/stop/{id}")
    public MachineMaintenanceActivityEntity stopBreakdown(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestParam(required = false) String remarks) {

        return service.stopBreakdown(id, reason, remarks);
    }

    @GetMapping("/filter")
    public List<MachineMaintenanceFilterDto> filter(
            @RequestParam(required = false) String machineName,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) String status,
            @RequestParam(value = "fromStartDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromStartDate,
            @RequestParam(value = "toStartDate", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toStartDate) {

        return service.filter(machineName, reason, status, fromStartDate, toStartDate);
    }
}

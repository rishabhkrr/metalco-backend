package com.indona.invento.controllers;

import com.indona.invento.dto.ChartsPayloadDto;
import com.indona.invento.dto.ChartsResponseDto;
import com.indona.invento.services.impl.DashboardServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private DashboardServiceImpl dashBoardService;

    /**
     * KPI summary for dashboard cards.
     * Returns counts of total SOs, customers, suppliers, items, pending approvals, etc.
     */
    @GetMapping("/kpi")
    public ResponseEntity<Map<String, Object>> getKpiSummary(
            @RequestParam(required = false) String unitCode) {
        return ResponseEntity.ok(dashBoardService.getKpiSummary(unitCode));
    }

    /**
     * Recent activity feed for dashboard.
     * Shows last 50 actions across all modules.
     */
    @GetMapping("/recent-activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity(
            @RequestParam(required = false) String unitCode) {
        return ResponseEntity.ok(dashBoardService.getRecentActivity(unitCode));
    }

    /**
     * Pending approvals summary across all modules.
     */
    @GetMapping("/pending-approvals")
    public ResponseEntity<Map<String, Object>> getPendingApprovals() {
        return ResponseEntity.ok(dashBoardService.getPendingApprovals());
    }

    // Legacy chart endpoints
    @PostMapping("/dhu")
    public ResponseEntity<ChartsResponseDto> getDhuChart(@RequestBody ChartsPayloadDto payload) {
        ChartsResponseDto chart = dashBoardService.getDhuChart(payload);
        return new ResponseEntity<>(chart, HttpStatus.OK);
    }

    @PostMapping("/defect")
    public ResponseEntity<ChartsResponseDto> getDefectChart(@RequestBody ChartsPayloadDto payload) {
        ChartsResponseDto chart = dashBoardService.getDefectChart(payload);
        return new ResponseEntity<>(chart, HttpStatus.OK);
    }

    @PostMapping("/area")
    public ResponseEntity<ChartsResponseDto> getAreaChart(@RequestBody ChartsPayloadDto payload) {
        ChartsResponseDto chart = dashBoardService.getAreaChart(payload);
        return new ResponseEntity<>(chart, HttpStatus.OK);
    }

    @PostMapping("/occ")
    public ResponseEntity<ChartsResponseDto> getOccChart(@RequestBody ChartsPayloadDto payload) {
        ChartsResponseDto chart = dashBoardService.getOccChart(payload);
        return new ResponseEntity<>(chart, HttpStatus.OK);
    }
}
package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.ChartsPayloadDto;
import com.indona.invento.dto.ChartsResponseDto;
import com.indona.invento.services.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired(required = false)
    private SalesOrderRepository salesOrderRepository;

    @Autowired(required = false)
    private CustomerMasterRepository customerMasterRepository;

    @Autowired(required = false)
    private SupplierMasterRepository supplierMasterRepository;

    @Autowired(required = false)
    private ItemMasterRepository itemMasterRepository;

    @Autowired(required = false)
    private POGenerationRepository poGenerationRepository;

    @Autowired(required = false)
    private GRNRepository grnRepository;

    @Autowired(required = false)
    private GateInwardRepository gateInwardRepository;

    @Autowired(required = false)
    private StockSummaryRepository stockSummaryRepository;

    @Autowired(required = false)
    private AuditLogRepository auditLogRepository;

    @Autowired(required = false)
    private NotificationRepository notificationRepository;

    /**
     * Get KPI summary — counts of key entities for dashboard cards.
     */
    public Map<String, Object> getKpiSummary(String unitCode) {
        Map<String, Object> kpis = new LinkedHashMap<>();

        try {
            kpis.put("totalSalesOrders", salesOrderRepository != null ? salesOrderRepository.count() : 0);
            kpis.put("totalCustomers", customerMasterRepository != null ? customerMasterRepository.count() : 0);
            kpis.put("totalSuppliers", supplierMasterRepository != null ? supplierMasterRepository.count() : 0);
            kpis.put("totalItems", itemMasterRepository != null ? itemMasterRepository.count() : 0);
            kpis.put("totalPurchaseOrders", poGenerationRepository != null ? poGenerationRepository.count() : 0);
            kpis.put("totalGRNs", grnRepository != null ? grnRepository.count() : 0);

            // Pending approvals
            long pendingSuppliers = supplierMasterRepository != null ?
                    supplierMasterRepository.findAll().stream()
                            .filter(s -> "PENDING_APPROVAL".equalsIgnoreCase(s.getStatus()) || "PENDING".equalsIgnoreCase(s.getStatus()))
                            .count() : 0;
            kpis.put("pendingSupplierApprovals", pendingSuppliers);

            long pendingCustomers = customerMasterRepository != null ?
                    customerMasterRepository.findAll().stream()
                            .filter(c -> "PENDING_APPROVAL".equalsIgnoreCase(c.getStatus()) || "Pending".equalsIgnoreCase(c.getStatus()))
                            .count() : 0;
            kpis.put("pendingCustomerApprovals", pendingCustomers);

            // Gate entries with vehicles IN
            long vehiclesIn = gateInwardRepository != null ?
                    gateInwardRepository.findByVehicleOutStatus("IN").size() : 0;
            kpis.put("vehiclesCurrentlyIn", vehiclesIn);

        } catch (Exception e) {
            kpis.put("error", e.getMessage());
        }

        return kpis;
    }

    /**
     * Get recent activity for the dashboard activity feed.
     */
    public List<Map<String, Object>> getRecentActivity(String unitCode) {
        List<Map<String, Object>> activities = new ArrayList<>();

        try {
            if (auditLogRepository != null) {
                var logs = unitCode != null && !unitCode.isEmpty() ?
                        auditLogRepository.findTop50ByUnitCodeOrderByPerformedAtDesc(unitCode) :
                        auditLogRepository.findTop50ByOrderByPerformedAtDesc();

                activities = logs.stream().map(log -> {
                    Map<String, Object> activity = new LinkedHashMap<>();
                    activity.put("id", log.getId());
                    activity.put("action", log.getAction());
                    activity.put("module", log.getModuleName());
                    activity.put("description", log.getDescription());
                    activity.put("performedBy", log.getPerformedBy());
                    activity.put("timestamp", log.getPerformedAt());
                    activity.put("entityRef", log.getEntityRef());
                    return activity;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            activities.add(error);
        }

        return activities;
    }

    /**
     * Get pending approvals across all modules.
     */
    public Map<String, Object> getPendingApprovals() {
        Map<String, Object> pending = new LinkedHashMap<>();

        try {
            // Suppliers
            if (supplierMasterRepository != null) {
                long count = supplierMasterRepository.findAll().stream()
                        .filter(s -> "PENDING_APPROVAL".equalsIgnoreCase(s.getStatus()) || "PENDING".equalsIgnoreCase(s.getStatus()))
                        .count();
                pending.put("suppliers", count);
            }

            // Customers
            if (customerMasterRepository != null) {
                long count = customerMasterRepository.findAll().stream()
                        .filter(c -> "PENDING_APPROVAL".equalsIgnoreCase(c.getStatus()) || "Pending".equalsIgnoreCase(c.getStatus()))
                        .count();
                pending.put("customers", count);
            }

            // PO Approvals
            if (poGenerationRepository != null) {
                long count = poGenerationRepository.findAll().stream()
                        .filter(po -> "PENDING_APPROVAL".equalsIgnoreCase(po.getPoStatus()))
                        .count();
                pending.put("purchaseOrders", count);
            }

        } catch (Exception e) {
            pending.put("error", e.getMessage());
        }

        return pending;
    }

    // Legacy chart methods — return empty response instead of null
    @Override
    public ChartsResponseDto getDhuChart(ChartsPayloadDto payload) {
        return new ChartsResponseDto();
    }

    @Override
    public ChartsResponseDto getDefectChart(ChartsPayloadDto payload) {
        return new ChartsResponseDto();
    }

    @Override
    public ChartsResponseDto getAreaChart(ChartsPayloadDto payload) {
        return new ChartsResponseDto();
    }

    @Override
    public ChartsResponseDto getOccChart(ChartsPayloadDto payload) {
        return new ChartsResponseDto();
    }
}

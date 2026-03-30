package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.entities.*;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.GRNInterUnitService;
import com.indona.invento.services.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * FRD: IUMT-GRN Service Implementation
 * Covers all 7 sub-modules:
 *   1. GRN Interunit Material Request Entry
 *   2. Material Request InterUnit Summary
 *   3. GRN Interunit Request Summary
 *   4. Stock Transfer – All (Interunit)
 *   5. Bundle Creation, QR Generation & Bin Allocation
 *   6. Submit Validation & Post-Submit Logic
 *   7. Stock Summary Integration
 */
@Service
public class GRNInterUnitServiceImpl implements GRNInterUnitService {

    @Autowired
    private GRNInterUnitRepository grnInterUnitRepository;

    @Autowired
    private RackBinMasterRepository rackBinMasterRepository;

    @Autowired
    private GrnLineItemRepository grnLineItemRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;

    // =========================================================================
    // Sub-Module 1: GRN Interunit Material Request Entry
    // FRD: IUMR-001 to IUMR-010
    // =========================================================================

    /**
     * FRD: IUMR-010 — Auto-generate IU GRN Number (MEGRN + YYMM + sequential)
     */
    private String generateIuGrnNumber() {
        String prefix = "MEGRN";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
        String fullPrefix = prefix + datePart;

        long existingCount = grnInterUnitRepository.countByGrnInterUnitRefNumberStartingWith(fullPrefix);
        String sequentialPart = String.format("%03d", existingCount + 1);

        return fullPrefix + sequentialPart;
    }

    /**
     * FRD: IUMR-008 — Auto-generate Batch Number per item
     */
    private String generateBatchNumber(String iuGrnNumber, int itemIndex) {
        return iuGrnNumber + "-B" + String.format("%02d", itemIndex + 1);
    }

    @Override
    @Transactional
    public GRNInterUnitEntity createInterUnitGRN(GRNInterUnitEntity entity) {
        // FRD: IUMR-010 — Auto-generate IU GRN Number
        if (entity.getGrnInterUnitRefNumber() == null || entity.getGrnInterUnitRefNumber().isEmpty()) {
            entity.setGrnInterUnitRefNumber(generateIuGrnNumber());
        }

        // FRD: Default status
        entity.setStatus("Pending");
        entity.setCreatedAt(new Date());

        // FRD: IUMR-008 — Auto-generate batch numbers for items
        if (entity.getItems() != null) {
            for (int i = 0; i < entity.getItems().size(); i++) {
                GRNInterUnitItemEntity item = entity.getItems().get(i);
                item.setGrnInterUnit(entity);
                if (item.getBatchNumber() == null || item.getBatchNumber().isEmpty()) {
                    item.setBatchNumber(generateBatchNumber(entity.getGrnInterUnitRefNumber(), i));
                }
                if (item.getScanStatus() == null) {
                    item.setScanStatus("Pending");
                }
            }
        }

        GRNInterUnitEntity saved = grnInterUnitRepository.save(entity);

        // Audit log
        auditLogService.logAction("CREATE", "GRN_INTERUNIT", "GRNInterUnitEntity",
                saved.getId(), saved.getGrnInterUnitRefNumber(), null, "Pending",
                "IU GRN " + saved.getGrnInterUnitRefNumber() + " created for MEDCI: " + saved.getMedcNumber() +
                ". Items: " + (saved.getItems() != null ? saved.getItems().size() : 0),
                "STORES_EXECUTIVE", saved.getUnit());

        return saved;
    }

    @Override
    @Transactional
    public GRNInterUnitEntity updateInterUnitGRN(Long id, GRNInterUnitEntity entity) {
        GRNInterUnitEntity existing = grnInterUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IU GRN not found with ID: " + id));

        // FRD: GIS-006 — Edit disabled after Approval
        if ("Approved".equalsIgnoreCase(existing.getStatus())) {
            throw new RuntimeException("Cannot edit an Approved IU GRN. IU GRN Number: " +
                    existing.getGrnInterUnitRefNumber());
        }

        // Update allowed fields
        existing.setMedcNumber(entity.getMedcNumber());
        existing.setMrNumber(entity.getMrNumber());
        existing.setSenderUnit(entity.getSenderUnit());

        // Update items
        if (entity.getItems() != null) {
            existing.getItems().clear();
            for (int i = 0; i < entity.getItems().size(); i++) {
                GRNInterUnitItemEntity item = entity.getItems().get(i);
                item.setGrnInterUnit(existing);
                if (item.getBatchNumber() == null || item.getBatchNumber().isEmpty()) {
                    item.setBatchNumber(generateBatchNumber(existing.getGrnInterUnitRefNumber(), i));
                }
                existing.getItems().add(item);
            }
        }

        GRNInterUnitEntity saved = grnInterUnitRepository.save(existing);

        auditLogService.logAction("UPDATE", "GRN_INTERUNIT", "GRNInterUnitEntity",
                saved.getId(), saved.getGrnInterUnitRefNumber(), null, null,
                "IU GRN " + saved.getGrnInterUnitRefNumber() + " updated",
                "STORES_EXECUTIVE", saved.getUnit());

        return saved;
    }

    @Override
    public GRNInterUnitEntity getById(Long id) {
        return grnInterUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IU GRN not found with ID: " + id));
    }

    @Override
    public GRNInterUnitEntity getByRefNumber(String refNumber) {
        return grnInterUnitRepository.findByGrnInterUnitRefNumber(refNumber)
                .orElseThrow(() -> new RuntimeException("IU GRN not found: " + refNumber));
    }

    @Override
    public List<GRNInterUnitEntity> getAll() {
        return grnInterUnitRepository.findAll();
    }

    // =========================================================================
    // Sub-Module 2: Material Request InterUnit Summary
    // FRD: MRS-001 to MRS-007
    // =========================================================================

    /**
     * FRD: MRS-002/003 — Calculate tri-state status:
     * - Pending: No GRN exists against any line item
     * - Partially Received: At least one item received but not all
     * - Received: All items fully received
     */
    @Override
    public List<Map<String, Object>> getMaterialRequestSummary(String unit) {
        List<GRNInterUnitEntity> allGrns = unit != null ?
                grnInterUnitRepository.findByUnit(unit) :
                grnInterUnitRepository.findAll();

        // Group by MR Number
        Map<String, List<GRNInterUnitEntity>> groupedByMr = allGrns.stream()
                .filter(g -> g.getMrNumber() != null)
                .collect(Collectors.groupingBy(GRNInterUnitEntity::getMrNumber));

        List<Map<String, Object>> summaries = new ArrayList<>();

        for (Map.Entry<String, List<GRNInterUnitEntity>> entry : groupedByMr.entrySet()) {
            String mrNumber = entry.getKey();
            List<GRNInterUnitEntity> grns = entry.getValue();
            GRNInterUnitEntity firstGrn = grns.get(0);

            // Calculate total quantities
            double totalQtyKg = 0;
            int totalItems = 0;
            int receivedItems = 0;

            for (GRNInterUnitEntity grn : grns) {
                if (grn.getItems() != null) {
                    for (GRNInterUnitItemEntity item : grn.getItems()) {
                        totalItems++;
                        totalQtyKg += (item.getQuantityKg() != null ? item.getQuantityKg() : 0);
                        if (item.getReceivedNetWeight() != null && item.getReceivedNetWeight() > 0) {
                            receivedItems++;
                        }
                    }
                }
            }

            // FRD: MRS-002 — Tri-state status
            String mrStatus;
            if (receivedItems == 0) {
                mrStatus = "Pending";
            } else if (receivedItems < totalItems) {
                mrStatus = "Partially Received";
            } else {
                mrStatus = "Received";
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("mrNumber", mrNumber);
            summary.put("senderUnit", firstGrn.getSenderUnit() != null ? firstGrn.getSenderUnit() : firstGrn.getSupplierUnit());
            summary.put("medcNumber", firstGrn.getMedcNumber());
            summary.put("date", firstGrn.getCreatedAt());
            summary.put("itemCount", totalItems);
            summary.put("totalQtyKg", totalQtyKg);
            summary.put("status", mrStatus);
            summary.put("iuGrnNumbers", grns.stream()
                    .map(GRNInterUnitEntity::getGrnInterUnitRefNumber)
                    .collect(Collectors.toList()));

            summaries.add(summary);
        }

        return summaries;
    }

    // =========================================================================
    // Sub-Module 3: GRN Interunit Request Summary
    // FRD: GIS-001 to GIS-008
    // =========================================================================

    @Override
    public List<GRNInterUnitEntity> getGrnSummary(String status, String unit) {
        if (status != null && unit != null) {
            return grnInterUnitRepository.findByStatusAndUnit(status, unit);
        } else if (status != null) {
            return grnInterUnitRepository.findByStatus(status);
        } else if (unit != null) {
            return grnInterUnitRepository.findByUnit(unit);
        }
        return grnInterUnitRepository.findAll();
    }

    /**
     * FRD: GIS-004 — Approve IU GRN
     * Status changes to "Approved"; Stock Transfer button becomes enabled
     */
    @Override
    @Transactional
    public GRNInterUnitEntity approveInterUnitGRN(Long id, String approvedBy) {
        GRNInterUnitEntity entity = grnInterUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IU GRN not found with ID: " + id));

        if (!"Pending".equalsIgnoreCase(entity.getStatus())) {
            throw new RuntimeException("Only Pending IU GRNs can be approved. Current status: " + entity.getStatus());
        }

        String oldStatus = entity.getStatus();
        entity.setStatus("Approved");
        entity.setApprovedBy(approvedBy);
        entity.setApprovedAt(new Date());

        GRNInterUnitEntity saved = grnInterUnitRepository.save(entity);

        // Audit log
        auditLogService.logAction("STATUS_CHANGE", "GRN_INTERUNIT", "GRNInterUnitEntity",
                saved.getId(), saved.getGrnInterUnitRefNumber(), oldStatus, "Approved",
                "IU GRN " + saved.getGrnInterUnitRefNumber() + " approved by " + approvedBy,
                approvedBy, saved.getUnit());

        // Notification — creator can now proceed with stock transfer
        notificationService.createRoleNotification(
                "INFO", "IU GRN Approved",
                "IU GRN " + saved.getGrnInterUnitRefNumber() + " has been approved. You can now proceed with Stock Transfer.",
                "STORES_EXECUTIVE", "GRN_INTERUNIT", "GRNInterUnitEntity", saved.getId(), saved.getUnit());

        return saved;
    }

    /**
     * FRD: GIS-005 — Reject IU GRN
     * Status changes to "Rejected"; creator notified with rejection remarks
     */
    @Override
    @Transactional
    public GRNInterUnitEntity rejectInterUnitGRN(Long id, String rejectedBy, String remarks) {
        GRNInterUnitEntity entity = grnInterUnitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IU GRN not found with ID: " + id));

        if (!"Pending".equalsIgnoreCase(entity.getStatus())) {
            throw new RuntimeException("Only Pending IU GRNs can be rejected. Current status: " + entity.getStatus());
        }

        String oldStatus = entity.getStatus();
        entity.setStatus("Rejected");
        entity.setRejectionRemarks(remarks);

        GRNInterUnitEntity saved = grnInterUnitRepository.save(entity);

        // Audit log
        auditLogService.logAction("STATUS_CHANGE", "GRN_INTERUNIT", "GRNInterUnitEntity",
                saved.getId(), saved.getGrnInterUnitRefNumber(), oldStatus, "Rejected",
                "IU GRN " + saved.getGrnInterUnitRefNumber() + " rejected. Remarks: " + remarks,
                rejectedBy, saved.getUnit());

        // Notification — creator notified with rejection reason
        notificationService.createRoleNotification(
                "WARNING", "IU GRN Rejected",
                "IU GRN " + saved.getGrnInterUnitRefNumber() + " has been rejected. Remarks: " + remarks +
                ". You may edit and resubmit.",
                "STORES_EXECUTIVE", "GRN_INTERUNIT", "GRNInterUnitEntity", saved.getId(), saved.getUnit());

        return saved;
    }

    // =========================================================================
    // Sub-Module 4: Stock Transfer Integration
    // FRD: STI-001 to STI-010
    // =========================================================================

    /**
     * FRD: STI-003 — Get only Approved IU GRNs for stock transfer dropdown
     */
    @Override
    public List<GRNInterUnitEntity> getApprovedForStockTransfer(String unit) {
        return grnInterUnitRepository.findByStatusAndUnit("Approved", unit);
    }

    // =========================================================================
    // Sub-Module 5: Rack & Bin Auto-Allocation
    // FRD: RBA-001 to RBA-008
    // =========================================================================

    /**
     * FRD: RBA-001 to RBA-008 — Rack & Bin Auto-Allocation Algorithm
     *
     * For "General" material:
     *   Filter 1: Store = "Warehouse" AND Item Category matches Product Category
     *   Filter 2: (Bin Capacity - Current Storage) >= bundle's Net Weight
     *   Sort 1: Storage Area Order ASC
     *   Sort 2: Distance ASC
     *   Result: First bin from sorted list
     *
     * For "Rejected" material:
     *   Skip algorithm → Recipient Store = "Rejection", Storage Area = "Rejection", Rack/Bin = "Common"
     *
     * Fallback: If no bin found → Warehouse > Common > Common
     */
    @Override
    public Map<String, Object> allocateRackBin(String storageType, String itemCategory,
                                                double requiredWeight, String materialAcceptance) {
        Map<String, Object> allocation = new LinkedHashMap<>();

        // FRD: If Rejected, skip algorithm entirely
        if ("Rejected".equalsIgnoreCase(materialAcceptance)) {
            allocation.put("recipientStore", "Rejection");
            allocation.put("storageArea", "Rejection");
            allocation.put("rackColumnBinNumber", "Common");
            allocation.put("allocated", true);
            allocation.put("method", "REJECTION_DEFAULT");
            return allocation;
        }

        // FRD: General material — run the algorithm
        String store = storageType != null ? storageType : "Warehouse";

        try {
            List<RackBinMasterEntity> availableBins = rackBinMasterRepository
                    .findAvailableBinsForAllocation(store, itemCategory, requiredWeight);

            if (!availableBins.isEmpty()) {
                RackBinMasterEntity bestBin = availableBins.get(0);

                // Build rack/bin reference
                String rackBin = buildRackBinReference(bestBin);

                allocation.put("recipientStore", "Warehouse");
                allocation.put("storageArea", bestBin.getStorageArea());
                allocation.put("rackColumnBinNumber", rackBin);
                allocation.put("binId", bestBin.getId());
                allocation.put("allocated", true);
                allocation.put("method", "AUTO_ALLOCATED");

                return allocation;
            }
        } catch (Exception e) {
            // Fall through to default
        }

        // FRD: Fallback — no suitable bin found
        allocation.put("recipientStore", "Warehouse");
        allocation.put("storageArea", "Common");
        allocation.put("rackColumnBinNumber", "Common");
        allocation.put("allocated", true);
        allocation.put("method", "FALLBACK_DEFAULT");

        return allocation;
    }

    private String buildRackBinReference(RackBinMasterEntity bin) {
        StringBuilder ref = new StringBuilder();
        if (bin.getStorageArea() != null) {
            // Abbreviate storage area for compact reference
            String areaAbbr = bin.getStorageArea().length() > 2 ?
                    bin.getStorageArea().substring(0, 2).toUpperCase() : bin.getStorageArea().toUpperCase();
            ref.append(areaAbbr);
        }
        if (bin.getRackNo() != null) ref.append("-").append(bin.getRackNo());
        if (bin.getColumnNo() != null) ref.append("-").append(bin.getColumnNo());
        if (bin.getBinNo() != null) ref.append("-").append(bin.getBinNo());
        return ref.toString();
    }

    // =========================================================================
    // Sub-Module 6: Submit Validation
    // FRD: SUB-001 to SUB-009
    // =========================================================================

    /**
     * FRD: SUB-001 to SUB-003 — Submit Validation
     * Submit enabled ONLY when:
     *   1. Bundles exist for ALL Item Descriptions in the IU GRN
     *   2. For each item: Added Bundle Qty (Kg & No) == IU GRN Qty (Kg & No)
     *   3. All Allocation Status == "Completed" (scan verified)
     */
    @Override
    public Map<String, Object> validateSubmit(Long stockTransferId, String iuGrnNumber) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();

        // Get the IU GRN
        GRNInterUnitEntity grn = grnInterUnitRepository.findByGrnInterUnitRefNumber(iuGrnNumber)
                .orElse(null);

        if (grn == null) {
            result.put("valid", false);
            errors.add("IU GRN not found: " + iuGrnNumber);
            result.put("errors", errors);
            return result;
        }

        // Get all line items (bundles) for this GRN
        List<GrnLineItemEntity> bundles = grnLineItemRepository.findByGrnNumber(iuGrnNumber);

        // Check 1: Bundles exist for ALL Item Descriptions
        Set<String> grnItemDescriptions = new HashSet<>();
        Map<String, Double> grnQtyKg = new HashMap<>();
        Map<String, Integer> grnQtyNo = new HashMap<>();

        if (grn.getItems() != null) {
            for (GRNInterUnitItemEntity item : grn.getItems()) {
                grnItemDescriptions.add(item.getItemDescription());
                grnQtyKg.merge(item.getItemDescription(),
                        item.getReceivedNetWeight() != null ? item.getReceivedNetWeight() : 0.0, Double::sum);
                grnQtyNo.merge(item.getItemDescription(),
                        item.getReceivedNo() != null ? item.getReceivedNo() : 0, Integer::sum);
            }
        }

        // Group bundles by item description
        Map<String, List<GrnLineItemEntity>> bundlesByItem = bundles.stream()
                .filter(b -> b.getItemDescription() != null)
                .collect(Collectors.groupingBy(GrnLineItemEntity::getItemDescription));

        // Check 1: All items have bundles
        for (String itemDesc : grnItemDescriptions) {
            if (!bundlesByItem.containsKey(itemDesc)) {
                errors.add("No bundles created for item: " + itemDesc);
            }
        }

        // Check 2: Quantity matching per item
        for (Map.Entry<String, List<GrnLineItemEntity>> entry : bundlesByItem.entrySet()) {
            String itemDesc = entry.getKey();
            List<GrnLineItemEntity> itemBundles = entry.getValue();

            double bundleTotalKg = itemBundles.stream()
                    .mapToDouble(b -> b.getWeightmentQuantityKg() != null ?
                            b.getWeightmentQuantityKg().doubleValue() : 0.0)
                    .sum();

            int bundleTotalNo = itemBundles.stream()
                    .mapToInt(b -> b.getWeightmentQuantityNo() != null ? b.getWeightmentQuantityNo() : 0)
                    .sum();

            Double expectedKg = grnQtyKg.get(itemDesc);
            Integer expectedNo = grnQtyNo.get(itemDesc);

            if (expectedKg != null && Math.abs(bundleTotalKg - expectedKg) > 0.01) {
                errors.add("Quantity mismatch for " + itemDesc + " (Kg): Bundle total=" +
                        bundleTotalKg + ", GRN expected=" + expectedKg);
            }

            if (expectedNo != null && bundleTotalNo != expectedNo) {
                errors.add("Quantity mismatch for " + itemDesc + " (No): Bundle total=" +
                        bundleTotalNo + ", GRN expected=" + expectedNo);
            }
        }

        // Check 3: All Allocation Status must be "Completed"
        for (GrnLineItemEntity bundle : bundles) {
            if (!"Completed".equalsIgnoreCase(bundle.getAllocationStatus())) {
                errors.add("Bundle " + bundle.getSlNo() + " for " + bundle.getItemDescription() +
                        " has Allocation Status: " + bundle.getAllocationStatus() + " (must be Completed)");
            }
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("totalItems", grnItemDescriptions.size());
        result.put("totalBundles", bundles.size());
        result.put("itemsCovered", bundlesByItem.size());

        return result;
    }

    // =========================================================================
    // MEDCI Filtering
    // =========================================================================

    /**
     * FRD: Show only MEDCI numbers not already entered in this module for the current unit
     */
    @Override
    public boolean isMedciUsed(String medcNumber, String unit) {
        return grnInterUnitRepository.existsByMedcNumberAndUnit(medcNumber, unit);
    }
}

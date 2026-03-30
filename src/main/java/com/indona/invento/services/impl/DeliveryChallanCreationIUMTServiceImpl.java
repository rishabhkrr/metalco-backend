package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.entities.*;
import com.indona.invento.services.DeliveryChallanCreationIUMTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DeliveryChallanCreationIUMTServiceImpl implements DeliveryChallanCreationIUMTService {
    private static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
    private static final String PREFIX = "MEDCI";
    private static final DateTimeFormatter MEDCI_FMT = DateTimeFormatter.ofPattern("yyMM"); // FRD: MEDCI + YYMM + seq
    private static final ZoneId KOLKATA = ZoneId.of("Asia/Kolkata");

    @Autowired
    private DeliveryChallanCreationIUMTRepository deliveryChallanCreationIUMTRepository;

    @Autowired
    private MaterialRequestSummaryHeaderRepository materialRequestSummaryHeaderRepository;

    @Autowired
    private GateEntryPackingAndDispatchRepository gateEntryPackingAndDispatchRepository;

    @Autowired
    private ItemMasterRepository itemMasterRepository;

    @Autowired
    private UnitMasterRepository unitMasterRepository;

    @Autowired
    private PackingListCreationIUMTRepository packingListCreationIUMTRepository;

    @Transactional
    @Override
    public List<DeliveryChallanCreationIUMTEntity> createDeliveryChallanIUMT(List<DeliveryChallanCreationIUMTEntity> entities) {
        if (entities == null || entities.isEmpty()) return List.of();

        String dcNumber = generateDcNumber();
        for (DeliveryChallanCreationIUMTEntity entity : entities) {

            String vehicleNo = entity.getVehicleNumberPackingAndDispatch();
            if (vehicleNo != null) {
                vehicleNo = vehicleNo.trim();

                Optional<GateEntryPackingAndDispatch> gateOpt =
                        gateEntryPackingAndDispatchRepository.findLatestByVehicleNumber(vehicleNo);

                String mappedStatus = null;
                if (gateOpt.isPresent() && gateOpt.get().getVehicleOutStatusPackingAndDispatch() != null) {
                    String gateStatus = gateOpt.get().getVehicleOutStatusPackingAndDispatch().trim().toLowerCase();

                    switch (gateStatus) {
                        case "in":
                            mappedStatus = "Pending";
                            break;
                        case "out":
                            mappedStatus = "Complete";
                            break;
                        case "in yard":
                        case "inyard":
                        case "in_yard":
                        case "in-yard":
                            mappedStatus = "In Progress";
                            break;
                        default:
                            mappedStatus = capitalizeFirst(gateStatus);
                    }
                } else {
                    mappedStatus = "Unknown";
                }

                entity.setVehicleOutStatusPackingAndDispatch(mappedStatus);
                entity.setDCNumber(dcNumber);
            } else {
                entity.setVehicleOutStatusPackingAndDispatch("Unknown");
            }
        }

        return deliveryChallanCreationIUMTRepository.saveAll(entities);
    }

    @Override
    public List<DeliveryChallanCreationIUMTEntity> getAllDeliveryChallansIUMT(String fromDate, String toDate) {
        Instant fromInstant = null;
        Instant toInstant = null;

        try {
            if (fromDate != null && !fromDate.isBlank()) {

                LocalDate ld = LocalDate.parse(fromDate, DateTimeFormatter.ISO_DATE);
                fromInstant = ld.atStartOfDay(ZONE).toInstant();
            }
            if (toDate != null && !toDate.isBlank()) {

                LocalDate ld = LocalDate.parse(toDate, DateTimeFormatter.ISO_DATE);
                LocalTime endOfDay = LocalTime.of(23, 59, 59, 999_000_000); // 999ms
                toInstant = ZonedDateTime.of(ld, endOfDay, ZONE).toInstant();
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd", ex);
        }

        if (fromInstant != null && toInstant != null) {
            return deliveryChallanCreationIUMTRepository.findByTimestampBetween(fromInstant, toInstant);
        } else if (fromInstant != null) {
            return deliveryChallanCreationIUMTRepository.findByTimestampAfter(fromInstant);
        } else if (toInstant != null) {
            return deliveryChallanCreationIUMTRepository.findByTimestampBefore(toInstant);
        } else {
            return deliveryChallanCreationIUMTRepository.findAll();
        }
    }

    @Override
    public List<Map<String, String>> getUnitCodes() {
        List<String> name = materialRequestSummaryHeaderRepository.findUnitCodes();
        return name.stream()
                .map(res -> Map.of("label", res, "value", res))
                .collect(Collectors.toList());

    }

    @Override
    public List<Map<String, String>> getUnitNames() {
        List<String> name = materialRequestSummaryHeaderRepository.findUnitNames();
        return name.stream()
                .map(res -> Map.of("label", res, "value", res))
                .collect(Collectors.toList());

    }

    @Override
    public List<Map<String, String>> getDCNumbers(String mode) {
        List<String> name = deliveryChallanCreationIUMTRepository.findDCNumbers(mode);
        return name.stream()
                .map(res -> Map.of("label", res, "value", res))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getItemPrice(String unit, String description) {

        Map<String, Object> result = new HashMap<>();
        if (unit == null || description == null) {
            return null;
        }

        try {
            List<ItemMasterEntity> stockRows =
                    itemMasterRepository.findBySupplierCodeAndSkuDescription(unit.trim(), description.trim());

            if (stockRows != null && !stockRows.isEmpty()) {

                ItemMasterEntity item = stockRows.get(0);

                result.put("itemPrice", item.getItemPrice());
                result.put("hsnCode", item.getHsnCode());
                return result;
            }

        } catch (Exception ignored) { }

        result.put("itemPrice", null);
        result.put("hsnCode", null);
        return result;
    }

    public List<unitAddressEntity> getPrimaryAddresses(String unitCode, String unitName) {

        UnitMasterEntity unit = unitMasterRepository.findByUnitCodeAndUnitName(unitCode, unitName)
                .orElseThrow(() -> new RuntimeException("Unit not found"));

        List<unitAddressEntity> addresses = unit.getAddressDetails();

        if (addresses == null || addresses.isEmpty()) {
            return List.of();
        }
        return addresses.stream()
                .filter(addr -> Boolean.TRUE.equals(addr.getPrimary()))
                .toList();
    }

    private Instant parseToStartInstant(String dateOrDateTime) {
        // try yyyy-MM-dd
        try {
            LocalDate ld = LocalDate.parse(dateOrDateTime, DateTimeFormatter.ISO_DATE);
            ZonedDateTime zdtStart = ld.atStartOfDay(ZONE);
            return zdtStart.toInstant();
        } catch (DateTimeParseException ignored) { }

        // try ISO_DATE_TIME
        OffsetDateTime odt = OffsetDateTime.parse(dateOrDateTime, DateTimeFormatter.ISO_DATE_TIME);
        return odt.toInstant();
    }

    private Instant parseToEndInstant(String dateOrDateTime) {
        // try yyyy-MM-dd
        try {
            LocalDate ld = LocalDate.parse(dateOrDateTime, DateTimeFormatter.ISO_DATE);
            LocalTime endOfDay = LocalTime.of(23, 59, 59, 999_000_000); // 999ms
            ZonedDateTime zdtEnd = ZonedDateTime.of(ld, endOfDay, ZONE);
            return zdtEnd.toInstant();
        } catch (DateTimeParseException ignored) { }

        // try ISO_DATE_TIME
        OffsetDateTime odt = OffsetDateTime.parse(dateOrDateTime, DateTimeFormatter.ISO_DATE_TIME);
        return odt.toInstant();
    }

    // helper method (put in same class as private)
    private String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        if (s.length() == 1) return s.toUpperCase();
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    /**
     * FRD: EWB-005 — Generate MEDCI number (format: MEDCI + YYMM + sequential)
     * e.g., MEDCI26010002
     */
    private String generateDcNumber() {
        LocalDate today = LocalDate.now(KOLKATA);
        String datePart = today.format(MEDCI_FMT); // YYMM format per FRD
        String fullPrefix = PREFIX + datePart;

        long count = deliveryChallanCreationIUMTRepository.countByDCNumberStartingWith(fullPrefix);
        int sequence = (int) count + 1;
        String sequenceStr = String.format("%04d", sequence);

        return fullPrefix + sequenceStr;
    }

    @Override
    public void deleteAll() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║ 🗑️  DELETE ALL DELIVERY CHALLAN IUMT  ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = deliveryChallanCreationIUMTRepository.count();
            System.out.println("📊 Total delivery challans before deletion: " + totalCount);

            deliveryChallanCreationIUMTRepository.deleteAll();

            long afterCount = deliveryChallanCreationIUMTRepository.count();
            System.out.println("✅ All delivery challans deleted successfully!");
            System.out.println("📊 Total delivery challans after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all delivery challans: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all delivery challans: " + e.getMessage());
        }
    }

    // =========================================================================
    // FRD: GRN-001/002 — GRN Status Management
    // =========================================================================

    /**
     * FRD: GRN-002 — Update GRN status to Completed when receiving unit
     * saves and approves the GRN in the GRN Interunit Transfer module.
     */
    public void updateGrnStatusToCompleted(String dcNumber) {
        List<DeliveryChallanCreationIUMTEntity> pendingDcs =
                deliveryChallanCreationIUMTRepository.findPendingByDCNumber(dcNumber);

        for (DeliveryChallanCreationIUMTEntity dc : pendingDcs) {
            dc.setGrnStatus("Completed");
        }
        deliveryChallanCreationIUMTRepository.saveAll(pendingDcs);
    }

    // =========================================================================
    // FRD: DC Summary — Grouped view by DC Number
    // =========================================================================

    /**
     * FRD: ITD-001 to ITD-004 — DC Summary with grouped detail drill-down
     */
    public List<Map<String, Object>> getDcSummaryGrouped() {
        List<DeliveryChallanCreationIUMTEntity> allDcs = deliveryChallanCreationIUMTRepository.findAll();

        // Group by DC Number
        Map<String, List<DeliveryChallanCreationIUMTEntity>> grouped = allDcs.stream()
                .filter(d -> d.getDCNumber() != null)
                .collect(Collectors.groupingBy(DeliveryChallanCreationIUMTEntity::getDCNumber));

        return grouped.entrySet().stream().map(entry -> {
            String dcNum = entry.getKey();
            List<DeliveryChallanCreationIUMTEntity> items = entry.getValue();
            DeliveryChallanCreationIUMTEntity first = items.get(0);

            Map<String, Object> summary = new java.util.LinkedHashMap<>();
            summary.put("dcNumber", dcNum);
            summary.put("timestamp", first.getTimestamp());
            summary.put("packingListNumber", first.getPackingListNumber());
            summary.put("mrNumber", first.getMrNumber());
            summary.put("lineNumber", first.getLineNumber());
            summary.put("unit", first.getUnit());
            summary.put("requestingCode", first.getRequestingCode());
            summary.put("requestingName", first.getRequestingName());
            summary.put("requestingBillingAddress", first.getRequestingBillingAddress());
            summary.put("requestingShippingAddress", first.getRequestingShippingAddress());
            summary.put("subTotalAmount", first.getSubTotalAmount());
            summary.put("igstPercent", first.getIgstPercent());
            summary.put("igstAmount", first.getIgstAmount());
            summary.put("totalAmount", first.getTotalAmount());
            summary.put("vehicleNumber", first.getVehicleNumberPackingAndDispatch());
            summary.put("ewayBillNumber", first.getEwayBillNumber());
            summary.put("grnStatus", first.getGrnStatus());
            summary.put("itemCount", items.size());
            summary.put("items", items);
            return summary;
        }).collect(Collectors.toList());
    }

    // =========================================================================
    // FRD: DCC-004 — Available Packing List Numbers (not yet submitted)
    // =========================================================================

    /**
     * FRD: DCC-002/003/004 — Get RFD List numbers not yet used in DC creation
     */
    public List<Map<String, String>> getAvailablePackingListNumbers(String unit) {
        // Get all packing list numbers from the packing list IUMT module
        List<String> allPlnNumbers = packingListCreationIUMTRepository.findPackingListNo();

        // Get already-submitted packing list numbers
        List<String> submittedNumbers = deliveryChallanCreationIUMTRepository.findAllSubmittedPackingListNumbers();

        // Filter: only those belonging to the unit and not yet submitted
        return allPlnNumbers.stream()
                .filter(pln -> !submittedNumbers.contains(pln))
                .map(pln -> Map.of("label", pln, "value", pln))
                .collect(Collectors.toList());
    }
}

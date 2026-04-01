package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.ProductionEntryDto;
import com.indona.invento.dto.ProductionEntryEndPieceDto;
import com.indona.invento.dto.ProductionIdleTimeEntryDto;
import com.indona.invento.dto.ScrapSummaryDto;
import com.indona.invento.entities.*;
import com.indona.invento.services.ProductionEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionEntryServiceImpl implements ProductionEntryService {

    @Autowired
    private ProductionEntryRepository productionEntryRepository;

    @Autowired
    private ProductionScheduleRepository productionScheduleRepository;

    @Autowired
    private ProductionIdleTimeEntryRepository idleTimeEntryRepository;

    @Autowired
    private MachineMasterRepository machineMasterRepository;

    @Autowired
    private MachineMaintenanceActivityRepository maintenanceRepo;

    @Autowired
    private PackingSchedulerRepository packingSchedulerRepository;

    @Autowired
    private StockSummaryRepository stockSummaryRepository;

    @Autowired
    private RackBinMasterRepository rackBinMasterRepository;

    @Autowired
    private ScrapSummaryRepository scrapSummaryRepository;

    @Autowired
    private ItemMasterRepository itemMasterRepository;

    @Override
    public List<Map<String, Object>> getDetailsBySoAndLineNumber(String soNumber, String lineNumber) {
        return productionScheduleRepository.findDetailsBySoAndLineNumber(soNumber, lineNumber);
    }

    private final ZoneId zone = ZoneId.of("Asia/Kolkata");
    LocalDate today = LocalDate.now(zone);
    DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss");

    @Override
    public List<Map<String, Object>> getDetailsBySoNumLineNumAndDescription(String soNumber, String lineNumber, String itemDescription) {
        return productionScheduleRepository.findDetailsBySoAndLineNumberAndItemDescription(soNumber, lineNumber, itemDescription);
    }

    @Override
    public List<ProductionIdleTimeEntryEntity> getAllProductionIdleEntries(String fromDate, String toDate) {
        Instant fromInstant = null;
        Instant toInstant = null;

        try {
            if (fromDate != null && !fromDate.isBlank()) {
                fromInstant = parseToStartInstant(fromDate);
            }
            if (toDate != null && !toDate.isBlank()) {
                toInstant = parseToEndInstant(toDate);
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd or ISO date-time", ex);
        }

        if (fromInstant != null && toInstant != null) {
            return idleTimeEntryRepository.findByTimestampBetween(fromInstant, toInstant);
        } else if (fromInstant != null) {
            return idleTimeEntryRepository.findByTimestampAfter(fromInstant);
        } else if (toInstant != null) {
            return idleTimeEntryRepository.findByTimestampBefore(toInstant);
        } else {
            return idleTimeEntryRepository.findAll();
        }
    }

//     * Convert an EndPiece entity to a Map matching the JSON example structure.

    private Map<String, Object> mapEndPieceToMap(ProductionEntryEndPieceEntity ep) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ep.getId());
        m.put("endPieceDimension", ep.getEndPieceDimension());
        m.put("calculatedWeightKg", ep.getCalculatedWeightKg());
        m.put("endPieceQuantityKg", ep.getEndPieceQuantityKg());
        m.put("endPieceQuantityNo", ep.getEndPieceQuantityNo());
        m.put("endPieceType", ep.getEndPieceType());
        m.put("qrGenerate", ep.getQrGenerate());
        m.put("qrCode", ep.getQrCode());
        m.put("parentDimension", ep.getParentDimension());
        m.put("batchNumber", ep.getBatchNumber());
        m.put("width", ep.getWidth());
        m.put("thickness", ep.getThickness());
        m.put("length", ep.getLength());
        // If you want to return a simple reference string for productionEntry:
        m.put("productionEntry", ep.getProductionEntry() != null ? ep.getProductionEntry().getId() : null);
        return m;
    }

    //     * Convert an IdleEntry entity to a Map matching the JSON example structure.
    private Map<String, Object> mapIdleEntryToMap(ProductionIdleTimeEntryEntity ie) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", ie.getId());
        m.put("machineName", ie.getMachineName());
        m.put("startTime", ie.getStartTime());
        m.put("endTime", ie.getEndTime());
        // computed combined date+time getters on the entity (if you added them)
        try {
            m.put("startDateTime", ie.getStartTime());
        } catch (Exception ex) {
            m.put("startDateTime", null);
        }
        try {
            m.put("endDateTime", ie.getEndTime());
        } catch (Exception ex) {
            m.put("endDateTime", null);
        }
        m.put("idleMinutes", ie.getIdleMinutes());
        m.put("idleReason", ie.getIdleReason());
        m.put("remarks", ie.getRemarks());
        m.put("productionEntry", ie.getProductionEntry() != null ? ie.getProductionEntry().getId() : null);
        return m;
    }


    @Override
    public List<ProductionEntryEntity> getAllProductionEntries(String fromDate, String toDate) {
        Instant fromInstant = null;
        Instant toInstant = null;

        try {
            if (fromDate != null && !fromDate.isBlank()) {
                fromInstant = parseToStartInstant(fromDate);
            }
            if (toDate != null && !toDate.isBlank()) {
                toInstant = parseToEndInstant(toDate);
            }
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd or ISO date-time", ex);
        }

        if (fromInstant != null && toInstant != null) {
            return productionEntryRepository.findByTimestampBetween(fromInstant, toInstant);
        } else if (fromInstant != null) {
            return productionEntryRepository.findByTimestampAfter(fromInstant);
        } else if (toInstant != null) {
            return productionEntryRepository.findByTimestampBefore(toInstant);
        } else {
            return productionEntryRepository.findAll();
        }
    }

    private Instant parseToStartInstant(String input) {
        if (input.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            LocalDate ld = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
            return ld.atStartOfDay(zone).toInstant();
        } else {
            return Instant.parse(input);
        }
    }

    private Instant parseToEndInstant(String input) {
        if (input.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            LocalDate ld = LocalDate.parse(input, DateTimeFormatter.ISO_LOCAL_DATE);
            LocalDateTime end = ld.atTime(LocalTime.MAX);
            return end.atZone(zone).toInstant();
        } else {
            return Instant.parse(input);
        }
    }


    @Override
    @Transactional
    public ProductionEntryDto createProductionEntry(ProductionEntryDto dto) {

        // formatter and reference date for combining times
        LocalDate today = LocalDate.now();
        DateTimeFormatter outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        java.util.function.Function<String, String> combineWithToday = timeStr -> {
            if (timeStr == null || timeStr.isBlank()) return null;
            String trimmed = timeStr.trim();
            if (trimmed.contains("-") || trimmed.contains("/")) {
                // assume date part exists already — keep as-is
                return trimmed;
            }
            LocalTime lt = parseAndNormalizeLocalTime(trimmed);
            if (lt == null) return null;
            LocalDateTime ldt = LocalDateTime.of(today, lt);
            return ldt.format(outFmt);
        };

        // Map DTO -> Entity
        ProductionEntryEntity entity = dtoToEntity(dto);

        // normalize parent start/end
        entity.setEndTime(combineWithToday.apply(entity.getEndTime()));

        // normalize children times and ensure bidirectional links
        if (entity.getIdleEntries() != null) {
            for (ProductionIdleTimeEntryEntity idle : entity.getIdleEntries()) {
                idle.setStartTime(combineWithToday.apply(idle.getStartTime()));
                idle.setEndTime(combineWithToday.apply(idle.getEndTime()));
                // set parent reference
                idle.setProductionEntry(entity);
            }
        }

        // ensure end pieces have backrefs too
        if (entity.getEndPieces() != null) {
            for (ProductionEntryEndPieceEntity piece : entity.getEndPieces()) {
                piece.setProductionEntry(entity);
            }
        }

        // save
        ProductionEntryEntity saved = productionEntryRepository.save(entity);

        // 🆕 Save End Pieces to Stock Summary if REUSABLE or SCRAP
        if (saved.getEndPieces() != null && !saved.getEndPieces().isEmpty()) {
            for (ProductionEntryEndPieceEntity endPiece : saved.getEndPieces()) {
                if (endPiece.getEndPieceType() != null) {
                    String endPieceType = endPiece.getEndPieceType().toUpperCase().trim();

                    if ("REUSABLE".equals(endPieceType) || "SCRAP".equals(endPieceType)) {
                        try {
                            saveEndPieceToStockSummary(saved, endPiece, endPieceType);
                            System.out.println("✅ End Piece (" + endPieceType + ") saved to Stock Summary - Qty: " +
                                endPiece.getEndPieceQuantityKg() + " KG");
                        } catch (Exception e) {
                            System.out.println("❌ Error saving end piece to stock summary: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (Boolean.TRUE.equals(saved.getPacking())) {
            PackingEntityScheduler pack = PackingEntityScheduler.builder()
                    .soNumber(saved.getSoNumber())
                    .lineNumber(saved.getLineNumber())
                    .unit(saved.getUnit())
                    .customerCode(saved.getCustomerCode())
                    .customerName(saved.getCustomerName())
                    .orderType(saved.getOrderType())
                    .productCategory(saved.getProductCategory())
                    .itemDescription(saved.getItemDescription())
                    .brand(saved.getBrand())
                    .grade(saved.getGrade())
                    .temper(saved.getTemper())
                    .dimension(saved.getDimension())
                    .quantityKg(saved.getProducedQtyKg())
                    .quantityNo(saved.getProducedQtyNo())
                    .uomKg(saved.getUomKg())
                    .uomNo(saved.getUomNo())
                    .targetDateOfDispatch(saved.getTargetDispatchDate())
                    .packingInstructions(null)
                    .packingStatus("PENDING")
                    .build();

            packingSchedulerRepository.save(pack);
        }

        // map saved entity back to dto for response
        ProductionEntryDto savedDto = entityToDto(saved);
        return savedDto;
    }

    @Override
    public List<Map<String, Object>> getSoNumbersAndLineNumbers() {

        // Get all production schedules with PENDING status using optimized query
        List<ProductionScheduleEntity> pendingSchedules = productionScheduleRepository.findPendingSchedules();
        System.out.println("📊 Total PENDING Production Schedules found: " + pendingSchedules.size());

        // Create separate entries for each SO/LineNumber combination from PENDING schedules
        List<Map<String, Object>> result = new ArrayList<>();

        for (ProductionScheduleEntity schedule : pendingSchedules) {
            String so = schedule.getSoNumber();
            String line = schedule.getLineNumber();

            if (so == null || line == null) {
                System.out.println("⚠️ Skipping schedule with null SO or Line: SO=" + so + ", Line=" + line);
                continue;
            }

            System.out.println("✅ Adding: SO=" + so + ", Line=" + line);
            // Add separate entry for each SO/LineNumber combination
            result.add(Map.of(
                    "value", so,
                    "label", line
            ));
        }

        System.out.println("📋 Final result count: " + result.size());
        return result;
    }

    @Override
    public List<Map<String, String>> getLineNumbers(String soNumber) {
        List<String> lineNumbers = productionScheduleRepository.findLineNumbers(soNumber);
        return lineNumbers.stream()
                .map(line -> Map.of("label", line, "value", line))
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, String>> getRmDescriptions() {
        List<String> name = productionScheduleRepository.findRmDescriptions();
        return name.stream()
                .map(res -> Map.of("label", res, "value", res))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScrapSummaryDto> getScrapSummary(LocalDate fromDate, LocalDate toDate) {

        List<ProductionEntryEntity> entries;

        // 🔹 If both dates are provided → apply filtering
        if (fromDate != null && toDate != null) {
            Instant startInstant = fromDate.atStartOfDay(zone).toInstant();
            Instant endInstant = toDate.plusDays(1).atStartOfDay(zone).minusNanos(1).toInstant();

            entries = productionEntryRepository.findAllByTimestampBetween(startInstant, endInstant);

        } else {
            // 🔹 If no date filters → return all entries
            entries = productionEntryRepository.findAll();
        }

        return entries.stream()
                .filter(pe -> pe.getEndPieces() != null && !pe.getEndPieces().isEmpty())
                .flatMap(pe -> pe.getEndPieces().stream()
                        .filter(ep -> ep.getEndPieceType() != null &&
                                "scrap".equalsIgnoreCase(ep.getEndPieceType().trim()))
                        .map(ep -> {
                            ScrapSummaryDto dto = new ScrapSummaryDto();

                            dto.setUnit(pe.getUnit());
                            dto.setDimension(ep.getEndPieceDimension() != null
                                    ? ep.getEndPieceDimension()
                                    : pe.getDimension());

                            dto.setProductCategory(pe.getProductCategory());
                            dto.setItemDescription(pe.getItemDescription());
                            dto.setBrand(pe.getBrand());
                            dto.setGrade(pe.getGrade());
                            dto.setTemper(pe.getTemper());
                            dto.setMachineName(pe.getMachineName());
                            dto.setSoNumber(pe.getSoNumber());
                            dto.setLineNumber(pe.getLineNumber());

                            dto.setProducedQtyKg(
                                    ep.getEndPieceQuantityKg() != null
                                            ? ep.getEndPieceQuantityKg()
                                            : pe.getProducedQtyKg()
                            );

                            BigDecimal producedNo = ep.getEndPieceQuantityNo();
                            if (producedNo == null && pe.getProducedQtyNo() != null) {
                                producedNo = BigDecimal.valueOf(pe.getProducedQtyNo());
                            }
                            dto.setProducedQtyNo(producedNo);

                            dto.setTimestamp(pe.getTimestamp());

                            return dto;
                        })
                )
                .collect(Collectors.toList());
    }


    // Supports dd-MM-yyyy and yyyy-MM-dd
    private LocalDate parseDateLenient(String s) {
        try {
            if (s.matches("\\d{2}-\\d{2}-\\d{4}"))
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            return LocalDate.parse(s); // yyyy-MM-dd
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse a user-provided time string like "9:5", "09:05", "9:05:3", " 09:05 " into LocalTime.
     * Accepts H:mm or H:mm:ss (with single-digit hour/min/sec) and pads values to two digits.
     * Returns null on unrecoverable parse failure.
     */
    private LocalTime parseAndNormalizeLocalTime(String s) {
        if (s == null) return null;
        String cleaned = s.trim().replaceAll("\\s+", ""); // remove internal spaces like "09: 05"
        // quick check: must contain at least one colon
        if (!cleaned.contains(":")) return null;
        String[] parts = cleaned.split(":");
        try {
            String hh = parts.length > 0 ? parts[0] : "0";
            String mm = parts.length > 1 ? parts[1] : "00";
            String ss = parts.length > 2 ? parts[2] : "00";

            // pad to 2 digits
            hh = hh.length() == 1 ? "0" + hh : hh;
            mm = mm.length() == 1 ? "0" + mm : mm;
            ss = ss.length() == 1 ? "0" + ss : ss;

            // Validate numeric ranges to avoid exceptions
            int hour = Integer.parseInt(hh);
            int minute = Integer.parseInt(mm);
            int second = Integer.parseInt(ss);
            if (hour < 0 || hour > 23) return null;
            if (minute < 0 || minute > 59) return null;
            if (second < 0 || second > 59) return null;

            return LocalTime.of(hour, minute, second);
        } catch (NumberFormatException | DateTimeException ex) {
            return null;
        }
    }


    private static ProductionEntryEntity dtoToEntity(ProductionEntryDto dto) {
        if (dto == null) return null;
        ProductionEntryEntity e = new ProductionEntryEntity();

        e.setId(dto.getId());
        e.setSoNumber(dto.getSoNumber());
        e.setLineNumber(dto.getLineNumber());
        e.setMachineName(dto.getMachineName());
        e.setUnit(dto.getUnit());
        e.setCustomerCode(dto.getCustomerCode());
        e.setCustomerName(dto.getCustomerName());
        e.setPacking(dto.getPacking());
        e.setOrderType(dto.getOrderType());
        e.setProductCategory(dto.getProductCategory());
        e.setItemDescription(dto.getItemDescription());
        e.setBrand(dto.getBrand());
        e.setGrade(dto.getGrade());
        e.setTemper(dto.getTemper());
        e.setDimension(dto.getDimension());
        e.setRequiredQuantityKg(dto.getRequiredQuantityKg());
        e.setRequiredQuantityNo(dto.getRequiredQuantityNo());
        e.setCalculatedWeight(dto.getCalculatedWeight());
        e.setProducedQtyKg(dto.getProducedQtyKg());
        e.setProducedQtyNo(dto.getProducedQtyNo());
        e.setGeneratedQr(dto.getGeneratedQr());
        e.setMachineBreakDown(dto.getMachineBreakDown());
        e.setTargetBladeSpeed(dto.getTargetBladeSpeed());
        e.setTargetFeed(dto.getTargetFeed());
        e.setTimestamp(dto.getTimestamp()); // optional: DTO may not send timestamp on create; entity @PrePersist will override if null
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setTotalMetresCut(dto.getTotalMetresCut());
        e.setNextProductionProcess(dto.getNextProductionProcess());

        // map endPieces
        if (dto.getEndPieces() != null) {
            List<ProductionEntryEndPieceEntity> pieces = dto.getEndPieces().stream()
                    .map(ProductionEntryServiceImpl::endPieceDtoToEntity)
                    .collect(Collectors.toList());
            e.getEndPieces().clear();
            e.getEndPieces().addAll(pieces);
            // parent refs set later in caller
        }

        // map idleEntries
        if (dto.getIdleEntries() != null) {
            List<ProductionIdleTimeEntryEntity> idleList = dto.getIdleEntries().stream()
                    .map(ProductionEntryServiceImpl::idleDtoToEntity)
                    .collect(Collectors.toList());
            e.getIdleEntries().clear();
            e.getIdleEntries().addAll(idleList);
            // parent refs set later in caller
        }

        return e;
    }

    private static ProductionEntryEndPieceEntity endPieceDtoToEntity(ProductionEntryEndPieceDto dto) {
        if (dto == null) return null;
        ProductionEntryEndPieceEntity e = new ProductionEntryEndPieceEntity();
        e.setId(dto.getId());
        e.setEndPieceDimension(dto.getEndPieceDimension());
        e.setCalculatedWeightKg(dto.getCalculatedWeightKg());
        e.setEndPieceQuantityKg(dto.getEndPieceQuantityKg());
        e.setEndPieceQuantityNo(dto.getEndPieceQuantityNo());
        e.setEndPieceType(dto.getEndPieceType());
        e.setQrGenerate(dto.getQrGenerate());
        e.setQrCode(dto.getQrCode());
        e.setParentDimension(dto.getParentDimension());
        e.setBatchNumber(dto.getBatchNumber());
        e.setWidth(dto.getWidth());
        e.setThickness(dto.getThickness());
        e.setLength(dto.getLength());
        // do NOT set productionEntry here (set by caller)
        return e;
    }

    private static ProductionIdleTimeEntryEntity idleDtoToEntity(ProductionIdleTimeEntryDto dto) {
        if (dto == null) return null;
        ProductionIdleTimeEntryEntity e = new ProductionIdleTimeEntryEntity();
        e.setId(dto.getId());
        e.setMachineName(dto.getMachineName());
        e.setStartTime(dto.getStartTime());
        e.setEndTime(dto.getEndTime());
        e.setIdleMinutes(dto.getIdleMinutes());
        e.setIdleReason(dto.getIdleReason());
        e.setRemarks(dto.getRemarks());
        e.setTimestamp(dto.getTimestamp()); // if DTO supplied timestamp
        // do NOT set productionEntry here (set by caller)
        return e;
    }

    private static ProductionEntryDto entityToDto(ProductionEntryEntity e) {
        if (e == null) return null;
        ProductionEntryDto dto = new ProductionEntryDto();

        dto.setId(e.getId());
        dto.setSoNumber(e.getSoNumber());
        dto.setLineNumber(e.getLineNumber());
        dto.setMachineName(e.getMachineName());
        dto.setUnit(e.getUnit());
        dto.setCustomerCode(e.getCustomerCode());
        dto.setCustomerName(e.getCustomerName());
        dto.setPacking(e.getPacking());
        dto.setOrderType(e.getOrderType());
        dto.setProductCategory(e.getProductCategory());
        dto.setItemDescription(e.getItemDescription());
        dto.setBrand(e.getBrand());
        dto.setGrade(e.getGrade());
        dto.setTemper(e.getTemper());
        dto.setDimension(e.getDimension());
        dto.setRequiredQuantityKg(e.getRequiredQuantityKg());
        dto.setRequiredQuantityNo(e.getRequiredQuantityNo());
        dto.setCalculatedWeight(e.getCalculatedWeight());
        dto.setProducedQtyKg(e.getProducedQtyKg());
        dto.setProducedQtyNo(e.getProducedQtyNo());
        dto.setGeneratedQr(e.getGeneratedQr());
        dto.setMachineBreakDown(e.getMachineBreakDown());
        dto.setTargetBladeSpeed(e.getTargetBladeSpeed());
        dto.setTargetFeed(e.getTargetFeed());
        dto.setTimestamp(e.getTimestamp());
        dto.setStartTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setTotalMetresCut(e.getTotalMetresCut());
        dto.setNextProductionProcess(e.getNextProductionProcess());

        if (e.getEndPieces() != null) {
            List<ProductionEntryEndPieceDto> pieces = e.getEndPieces().stream()
                    .map(ProductionEntryServiceImpl::endPieceEntityToDto)
                    .collect(Collectors.toList());
            dto.setEndPieces(pieces);
        } else {
            dto.setEndPieces(Collections.emptyList());
        }

        if (e.getIdleEntries() != null) {
            List<ProductionIdleTimeEntryDto> idle = e.getIdleEntries().stream()
                    .map(ProductionEntryServiceImpl::idleEntityToDto)
                    .collect(Collectors.toList());
            dto.setIdleEntries(idle);
        } else {
            dto.setIdleEntries(Collections.emptyList());
        }

        // compute endPiecesQtyNo if you want same behavior as transient getter
        dto.setEndPiecesQtyNo(
                e.getEndPieces() == null ?
                        BigDecimal.ZERO :
                        e.getEndPieces().stream()
                                .map(ProductionEntryEndPieceEntity::getEndPieceQuantityNo)
                                .filter(Objects::nonNull)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
        );

        return dto;
    }

    private static ProductionEntryEndPieceDto endPieceEntityToDto(ProductionEntryEndPieceEntity e) {
        if (e == null) return null;
        ProductionEntryEndPieceDto dto = new ProductionEntryEndPieceDto();
        dto.setId(e.getId());
        dto.setEndPieceDimension(e.getEndPieceDimension());
        dto.setCalculatedWeightKg(e.getCalculatedWeightKg());
        dto.setEndPieceQuantityKg(e.getEndPieceQuantityKg());
        dto.setEndPieceQuantityNo(e.getEndPieceQuantityNo());
        dto.setEndPieceType(e.getEndPieceType());
        dto.setQrGenerate(e.getQrGenerate());
        dto.setQrCode(e.getQrCode());
        dto.setParentDimension(e.getParentDimension());
        dto.setBatchNumber(e.getBatchNumber());
        dto.setWidth(e.getWidth());
        dto.setThickness(e.getThickness());
        dto.setLength(e.getLength());
        // do NOT include productionEntry in DTO
        return dto;
    }

    private static ProductionIdleTimeEntryDto idleEntityToDto(ProductionIdleTimeEntryEntity e) {
        if (e == null) return null;
        ProductionIdleTimeEntryDto dto = new ProductionIdleTimeEntryDto();
        dto.setId(e.getId());
        dto.setMachineName(e.getMachineName());
        dto.setStartTime(e.getStartTime());
        dto.setEndTime(e.getEndTime());
        dto.setIdleMinutes(e.getIdleMinutes());
        dto.setIdleReason(e.getIdleReason());
        dto.setRemarks(e.getRemarks());
        dto.setTimestamp(e.getTimestamp());
        // do NOT include productionEntry in DTO
        return dto;
    }

    @Override
    public List<String> getAvailableMachinesForDropdown() {

        List<String> allMachines = machineMasterRepository.findMachineNames();
        List<String> underBreakdown = maintenanceRepo.getMachinesUnderBreakdown();

        return allMachines.stream()
                .filter(m -> !underBreakdown.contains(m))
                .sorted()
                .toList();
    }

    @Override
    public Map<String, String> getLastEntryEndTime() {
        String endTime = productionEntryRepository.findLastEntryEndTime();
        Map<String, String> response = new HashMap<>();
        response.put("lastEndTime", endTime);
        return response;
    }

    @Override
    @Transactional
    public void deleteAllProductionEntries() {
        List<ProductionEntryEntity> all = productionEntryRepository.findAll();
        if (all.isEmpty()) return;

        List<Long> ids = all.stream()
                .map(ProductionEntryEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (ids.isEmpty()) return;

        productionEntryRepository.deleteEndPiecesByProductionEntryIds(ids);
        productionEntryRepository.deleteIdleEntriesByProductionEntryIds(ids);

        productionEntryRepository.deleteAllByIdInBatch(ids);
    }

    private void saveEndPieceToStockSummary(ProductionEntryEntity production,
                                            ProductionEntryEndPieceEntity endPiece,
                                            String endPieceType) {
        // Determine storage details based on end piece type
        String store = "Warehouse";
        String storageArea = "REUSABLE".equals(endPieceType) ? "End Piece Storage" : "Scrap Storage";
        String rackColumnBin = "";

        // For REUSABLE: Auto-allocate rack based on Product Category
        if ("REUSABLE".equals(endPieceType)) {
            // Get eligible racks from Rack & Bin Master based on Product Category
            List<RackBinMasterEntity> eligibleBins = rackBinMasterRepository.findByItemCategory(production.getProductCategory());

            if (eligibleBins != null && !eligibleBins.isEmpty()) {
                // Filter 1: Check current storage capacity
                BigDecimal endPieceQtyKg = endPiece.getEndPieceQuantityKg() != null ?
                    endPiece.getEndPieceQuantityKg() : BigDecimal.ZERO;

                List<RackBinMasterEntity> availableBins = eligibleBins.stream()
                    .filter(bin -> {
                        Double capacity = Double.valueOf(bin.getBinCapacity() != null ? bin.getBinCapacity() : "0");
                        Double currentStorage = bin.getCurrentStorage() != null ? bin.getCurrentStorage() : 0.0;
                        return (currentStorage + endPieceQtyKg.doubleValue()) <= capacity;
                    })
                    .sorted(java.util.Comparator.comparingDouble(
                        bin -> bin.getDistance() != null ? bin.getDistance() : Double.MAX_VALUE
                    ))
                    .toList();

                if (!availableBins.isEmpty()) {
                    RackBinMasterEntity selectedBin = availableBins.get(0);
                    rackColumnBin = selectedBin.getRackNo() + "-" + selectedBin.getColumnNo() + "-" + selectedBin.getBinNo();

                    System.out.println("   ✅ Auto-allocated bin for REUSABLE: " + rackColumnBin +
                        " (Distance: " + selectedBin.getDistance() + ")");
                } else {
                    rackColumnBin = "AUTO_ALLOCATION_FAILED";
                    System.out.println("   ⚠️ No available bins for REUSABLE end piece");
                }
            }
        } else if ("SCRAP".equals(endPieceType)) {
            // For SCRAP: Use Common bin
            rackColumnBin = "Common";
        }

        // FIX #5: Resolve materialType from ItemMaster (not endPieceType)
        String resolvedMaterialType = "";
        if (production.getItemDescription() != null && !production.getItemDescription().isBlank()) {
            Optional<ItemMasterEntity> itemMasterOpt = itemMasterRepository
                    .findBySkuDescriptionIgnoreCase(production.getItemDescription());
            if (itemMasterOpt.isPresent() && itemMasterOpt.get().getMaterialType() != null) {
                resolvedMaterialType = itemMasterOpt.get().getMaterialType();
            }
        }

        // FIX #3: UPSERT — Check if matching stock entry already exists
        String dimension = production.getDimension();
        Optional<StockSummaryEntity> existingOpt = stockSummaryRepository
                .findByUnitAndItemDescriptionAndItemGroupAndStoreAndStorageAreaAndRackColumnShelfNumberAndDimension(
                        production.getUnit(),
                        production.getItemDescription(),
                        "Raw Materials",
                        store,
                        storageArea,
                        rackColumnBin,
                        dimension);

        StockSummaryEntity stockEntry;
        if (existingOpt.isPresent()) {
            // ♻️ UPDATE existing entry — add quantity
            stockEntry = existingOpt.get();
            BigDecimal oldKg = stockEntry.getQuantityKg() != null ? stockEntry.getQuantityKg() : BigDecimal.ZERO;
            int oldNo = stockEntry.getQuantityNo() != null ? stockEntry.getQuantityNo() : 0;

            BigDecimal addKg = endPiece.getEndPieceQuantityKg() != null ? endPiece.getEndPieceQuantityKg() : BigDecimal.ZERO;
            int addNo = endPiece.getEndPieceQuantityNo() != null ? endPiece.getEndPieceQuantityNo().intValue() : 0;

            stockEntry.setQuantityKg(oldKg.add(addKg));
            stockEntry.setQuantityNo(oldNo + addNo);

            // Update dimensions if provided
            if (endPiece.getLength() != null) stockEntry.setLength(endPiece.getLength());
            if (endPiece.getWidth() != null) stockEntry.setWidth(endPiece.getWidth());
            if (endPiece.getThickness() != null) stockEntry.setThickness(endPiece.getThickness());
            if (endPiece.getBatchNumber() != null) stockEntry.setBatchNumber(endPiece.getBatchNumber());

            System.out.println("   ♻️ UPSERT UPDATE: End Piece → existing ID " + stockEntry.getId() +
                " | Old Qty: " + oldKg + " → New Qty: " + stockEntry.getQuantityKg());
        } else {
            // ✨ CREATE new entry
            stockEntry = StockSummaryEntity.builder()
                .unit(production.getUnit())
                .store(store)
                .storageArea(storageArea)
                .rackColumnShelfNumber(rackColumnBin)
                .itemDescription(production.getItemDescription())
                .productCategory(production.getProductCategory())
                .brand(production.getBrand())
                .grade(production.getGrade())
                .temper(production.getTemper())
                .dimension(dimension)
                .quantityKg(endPiece.getEndPieceQuantityKg())
                .quantityNo(endPiece.getEndPieceQuantityNo() != null ? endPiece.getEndPieceQuantityNo().intValue() : 0)
                .itemGroup("Raw Materials")
                .materialType(resolvedMaterialType)
                .pickListLocked(false)
                .reprintQr(endPiece.getQrGenerate() != null ? endPiece.getQrGenerate() : false)
                .sectionNo(endPieceType)
                .qrCode(endPiece.getQrCode())
                .length(endPiece.getLength())
                .width(endPiece.getWidth())
                .thickness(endPiece.getThickness())
                .batchNumber(endPiece.getBatchNumber())
                .build();

            System.out.println("   ✨ UPSERT CREATE: End Piece → new entry");
        }

        stockSummaryRepository.save(stockEntry);
        System.out.println("   💾 End Piece saved to Stock Summary - Store: " + store +
            " | Area: " + storageArea + " | Rack: " + rackColumnBin +
            " | MaterialType: " + resolvedMaterialType +
            " | Length: " + endPiece.getLength() + " | Width: " + endPiece.getWidth() +
            " | Thickness: " + endPiece.getThickness() + " | Batch: " + endPiece.getBatchNumber());

        // 🆕 If SCRAP: Also save to Scrap Summary
        if ("SCRAP".equals(endPieceType)) {
            ScrapSummaryEntity scrapEntry = ScrapSummaryEntity.builder()
                .unit(production.getUnit())
                .dimension(production.getDimension())
                .productCategory(production.getProductCategory())
                .itemDescription(production.getItemDescription())
                .brand(production.getBrand())
                .grade(production.getGrade())
                .temper(production.getTemper())
                .machineName(production.getMachineName())
                .soNumber(production.getSoNumber())
                .lineNumber(production.getLineNumber())
                .producedQtyKg(endPiece.getEndPieceQuantityKg())
                .producedQtyNo(endPiece.getEndPieceQuantityNo() != null ? endPiece.getEndPieceQuantityNo().intValue() : 0)
                // 🆕 End piece dimensions
                .length(endPiece.getLength())
                .width(endPiece.getWidth())
                .thickness(endPiece.getThickness())
                .batchNumber(endPiece.getBatchNumber())
                .timestamp(java.time.Instant.now())
                .build();

            scrapSummaryRepository.save(scrapEntry);
            System.out.println("   ♻️ SCRAP saved to Scrap Summary - Qty: " + endPiece.getEndPieceQuantityKg() +
                " KG | Length: " + endPiece.getLength() + " | Width: " + endPiece.getWidth() +
                " | Thickness: " + endPiece.getThickness());
        }
    }
}

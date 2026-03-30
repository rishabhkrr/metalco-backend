package com.indona.invento.services.impl;

import com.indona.invento.dao.MachineMasterRepository;
import com.indona.invento.dao.ProductionScheduleRepository;
import com.indona.invento.dao.SalesOrderSchedulerRepository;
import com.indona.invento.dao.WarehouseStockTransferRepository;
import com.indona.invento.dto.ProductionScheduleDto;
import com.indona.invento.entities.*;
import com.indona.invento.services.ProductionScheduleService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.kafka.streams.kstream.EmitStrategy.log;

@Service
public class ProductionScheduleServiceImpl implements ProductionScheduleService {
    private static final ZoneId APP_ZONE = ZoneId.of("Asia/Kolkata");

    @Autowired
    private ProductionScheduleRepository productionScheduleRepository;

    @Autowired
    private SalesOrderSchedulerRepository salesOrderSchedulerRepository;

    @Autowired
    private MachineMasterRepository machineMasterRepository;

    @Autowired
    private WarehouseStockTransferRepository warehouseStockTransferRepository;

    @Override
    public ProductionScheduleEntity createProduction(ProductionScheduleEntity production) {
        return productionScheduleRepository.save(production);
    }

    @Override
    public void deleteProduction(Long id) {
        productionScheduleRepository.deleteById(id);
    }

    @Transactional
    @Override
    public List<ProductionScheduleEntity> getMarkingAndCuttingSchedules() {
        List<SalesOrderSchedulerEntity> markingSales = salesOrderSchedulerRepository.findAllMarkingAndCuttingAndRetrievalStatusCompleted();

        if (markingSales == null || markingSales.isEmpty()) {
            return productionScheduleRepository.findAll();
        }

        List<SalesOrderSchedulerEntity> normalizedSales = markingSales.stream()
                .filter(Objects::nonNull)
                .filter(s -> s.getSoNumber() != null && !s.getSoNumber().trim().isEmpty())
                .filter(s -> s.getLineNumber() != null)
                .toList();

        if (normalizedSales.isEmpty()) {
            return productionScheduleRepository.findAll();
        }

        Set<String> soNumbers = normalizedSales.stream()
                .map(s -> s.getSoNumber().trim())
                .collect(Collectors.toSet());

        List<ProductionScheduleEntity> existingSchedules = productionScheduleRepository.findBySoNumberIn(soNumbers);
        Set<String> existingKeys = existingSchedules.stream()
                .map(e -> compositeKey(e.getSoNumber(), e.getLineNumber()))
                .collect(Collectors.toSet());

        List<ProductionScheduleEntity> toSave = new ArrayList<>(normalizedSales.size());

        for (SalesOrderSchedulerEntity sales : normalizedSales) {
            String soTrimmed = sales.getSoNumber() == null ? "" : sales.getSoNumber().trim();
            String key = compositeKey(soTrimmed, sales.getLineNumber());
            if (existingKeys.contains(key)) {
                continue;
            }

            ProductionScheduleEntity entity = new ProductionScheduleEntity();

            // copy fields (kept same order as original)
            entity.setNextProcess(sales.getNextProcess()); // original NextProcess from sales
            entity.setSoNumber(soTrimmed);
            entity.setLineNumber(sales.getLineNumber());
            entity.setUnit(sales.getUnit());
            entity.setCustomerCode(sales.getCustomerCode());
            entity.setCustomerName(sales.getCustomerName());
            entity.setPacking(sales.getPacking());
            entity.setOrderType(sales.getOrderType());
            entity.setProductCategory(sales.getProductCategory());
            entity.setDimension(sales.getDimension());
            entity.setRequiredQuantityKg(sales.getRequiredQuantityKg());
            entity.setRequiredQuantityNo(sales.getRequiredQuantityNo());
            entity.setTargetDispatchDate(sales.getTargetDateOfDispatch());
            entity.setUomKg(sales.getUomKg());
            entity.setUomNo(sales.getUomNo());

            entity.setNextProcess("Marking and Cutting");

            List<WarehouseStockTransferEntity> wstList =
                    warehouseStockTransferRepository.findBySoNumberAndLineNumber(soTrimmed, sales.getLineNumber());

            if (!wstList.isEmpty()) {

                WarehouseStockTransferEntity firstWst = wstList.get(0);

                entity.setItemDescription(firstWst.getItemDescription());
                entity.setBrand(firstWst.getBrand());
                entity.setGrade(firstWst.getGrade());
                entity.setTemper(firstWst.getTemper());
                entity.setRmQuantityKg(firstWst.getWeighmentQuantityKg());

            } else {

                entity.setItemDescription(sales.getItemDescription());
                entity.setBrand(sales.getBrand());
                entity.setGrade(sales.getGrade());
                entity.setTemper(sales.getTemper());
                entity.setRmQuantityKg(sales.getStockTransfer().getWeighmentQuantityKg());
            }

            entity.setNextProductionProcess("Dispatch");

            toSave.add(entity);
            existingKeys.add(key);
        }

        if (!toSave.isEmpty()) {
            productionScheduleRepository.saveAll(toSave);
        }

        return productionScheduleRepository.findAll(Sort.by(Sort.Direction.ASC, "targetDispatchDate"));
    }



    // Helper to build consistent composite keys (null-safe, trimmed)
    private static String compositeKey(String soNumber, String lineNumber) {
        String so = (soNumber == null) ? "" : soNumber.trim();
        String ln = (lineNumber == null) ? "" : lineNumber.trim();
        return so + "|" + ln;
    }

    @Override
    public List<Map<String, String>> getMachineNames() {
        List<String> name = machineMasterRepository.findApprovedMachineNames();
        return name.stream()
                .map(res -> Map.of("label", res, "value", res))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductionScheduleEntity> getAllProductionSchedule() {
        return productionScheduleRepository.findAll();
    }

    @Override
    public ProductionScheduleEntity updateProductionSchedule(Long id, ProductionScheduleDto dto) {
        ProductionScheduleEntity entity = productionScheduleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductionSchedule id=" + id + " not found"));

        entity.setNextProcess(dto.getNextProcess());
        entity.setMachineName(dto.getMachineName());
        entity.setSoNumber(dto.getSoNumber());
        entity.setLineNumber(dto.getLineNumber());
        entity.setUnit(dto.getUnit());
        entity.setCustomerCode(dto.getCustomerCode());
        entity.setCustomerName(dto.getCustomerName());
        entity.setPacking(dto.getPacking());
        entity.setOrderType(dto.getOrderType());
        entity.setProductCategory(dto.getProductCategory());
        entity.setItemDescription(dto.getItemDescription());
        entity.setRmQuantityKg(dto.getRmQuantityKg());
        entity.setBrand(dto.getBrand());
        entity.setGrade(dto.getGrade());
        entity.setTemper(dto.getTemper());
        entity.setDimension(dto.getDimension());
        entity.setRequiredQuantityKg(dto.getRequiredQuantityKg());
        entity.setRequiredQuantityNo(dto.getRequiredQuantityNo());
        entity.setTargetBladeSpeed(dto.getTargetBladeSpeed());
        entity.setTargetFeed(dto.getTargetFeed());
        entity.setUomNo(dto.getUomNo());
        entity.setUomKg(dto.getUomKg());

        // Parse user times and combine with today's date (Asia/Kolkata)
        LocalDateTime startDt = parseTimeWithToday(dto.getStartTime());
        LocalDateTime endDt   = parseTimeWithToday(dto.getEndTime());

        entity.setStartTime(String.valueOf(startDt));
        entity.setEndTime(String.valueOf(endDt));

        entity.setNextProductionProcess("DISPATCH");
        entity.setTargetDispatchDate(dto.getTargetDispatchDate());

        return productionScheduleRepository.save(entity);
    }

    @Override
    public Map<String, Object> getMachineDetails(String machineName) {

        if (machineName == null || machineName.trim().isEmpty()) {
            return Map.of();
        }
        String machineNameTrimmed = machineName.trim();
        MachineMasterEntity machine = machineMasterRepository.findByMachineName(machineNameTrimmed);
        if (machine == null) {
            return Map.of();
        }
        Map<String, Object> response = new HashMap<>();

        if (machine.getCuttingConfig() != null) {
            CuttingMachineConfig cfg = machine.getCuttingConfig();

            response.put("idealBladeSpeed", cfg.getIdealBladeSpeed());
            response.put("idealCuttingFeed", cfg.getIdealCuttingFeed());
        } else {
            response.put("idealBladeSpeed", null);
            response.put("idealCuttingFeed", null);
        }

        return response;
    }

    private LocalDateTime parseTimeWithToday(String timeStr) {
        if (timeStr == null) return null;
        String trimmed = timeStr.trim();
        if (trimmed.isEmpty()) return null;

        // normalize common spacing issues: "12: 23 AM" -> "12:23 AM"
        trimmed = trimmed.replaceAll("\\s*:\\s*", ":");   // remove spaces around colon
        trimmed = trimmed.replaceAll("\\s+", " ");        // collapse multiple spaces
        trimmed = trimmed.toUpperCase(Locale.ENGLISH);

        DateTimeFormatter twelveHour = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
        DateTimeFormatter twentyFourHour = DateTimeFormatter.ofPattern("H:mm", Locale.ENGLISH);

        LocalDate today = LocalDate.now(APP_ZONE);

        try {
            LocalTime t = LocalTime.parse(trimmed, twelveHour);
            return LocalDateTime.of(today, t);
        } catch (DateTimeParseException e) {
            // fallback: try 24-hour formats like "13:23" or "1:05"
            try {
                LocalTime t = LocalTime.parse(trimmed, twentyFourHour);
                return LocalDateTime.of(today, t);
            } catch (DateTimeParseException ex) {
                // couldn't parse — log and return null
                log.warn("Unable to parse time string '{}' (tried 12h and 24h formats).", timeStr);
                return null;
            }
        }
    }

    @Override
    public List<Map<String, String>> getPendingSoAndLineNumbers() {
        // Find all production schedules with PENDING status
        List<ProductionScheduleEntity> pendingSchedules = productionScheduleRepository.findAll()
                .stream()
                .filter(schedule -> "PENDING".equalsIgnoreCase(schedule.getStatus()))
                .toList();

        // Convert to simple Map format with SO/Line format
        return pendingSchedules.stream()
                .map(schedule -> {
                    String soNumber = schedule.getSoNumber() != null ? schedule.getSoNumber() : "";
                    String lineNumber = schedule.getLineNumber() != null ? schedule.getLineNumber() : "";
                    return Map.of(
                            "soLineNumber", soNumber + "/" + lineNumber
                    );
                })
                .toList();
    }

    @Override
    public ProductionScheduleEntity updateMachineDetails(String soNumber, String lineNumber, String machineName, Double targetBladeSpeed, Double targetFeed) {
        // Find production schedule by SO Number and Line Number
        ProductionScheduleEntity schedule = productionScheduleRepository.findAll()
                .stream()
                .filter(s -> soNumber.equalsIgnoreCase(s.getSoNumber()) && lineNumber.equalsIgnoreCase(s.getLineNumber()))
                .findFirst()
                .orElse(null);

        if (schedule == null) {
            log.warn("Production schedule not found for SO: {}, Line: {}", soNumber, lineNumber);
            return null;
        }

        log.info("📝 Updating machine details for SO: {} | Line: {}", soNumber, lineNumber);
        log.info("   Machine Name: {} -> {}", schedule.getMachineName(), machineName);
        log.info("   Target Blade Speed: {} -> {}", schedule.getTargetBladeSpeed(), targetBladeSpeed);
        log.info("   Target Feed: {} -> {}", schedule.getTargetFeed(), targetFeed);

        // Update only the 3 fields - other fields remain unchanged
        schedule.setMachineName(machineName);
        schedule.setTargetBladeSpeed(targetBladeSpeed);
        schedule.setTargetFeed(targetFeed);

        ProductionScheduleEntity updated = productionScheduleRepository.save(schedule);
        log.info("✅ Machine details updated successfully - ID: {}", updated.getId());

        return updated;
    }
}
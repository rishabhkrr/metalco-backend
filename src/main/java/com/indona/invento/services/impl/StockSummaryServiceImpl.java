package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.*;
import com.indona.invento.entities.*;
import com.indona.invento.services.StockSummaryService;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class StockSummaryServiceImpl implements StockSummaryService {

    @Autowired
    private StockSummaryRepository repository;

    @Autowired
    private ItemMasterRepository itemMasterRepository;

    @Autowired
    private SoSummaryRepository soSummaryRepository;

    @Autowired
    private LowStockAlertRepository lowStockAlertRepository;

    @Autowired
    private PORequestRepository poRequestRepository;

    @Autowired
    private RackBinMasterRepository rackBinMasterRepository;

    @Autowired
    private GRNRepository grnRepository;

    @Autowired
    private StockSummaryBundleRepository stockSummaryBundleRepository;

    @Override
    public StockSummaryEntity create(StockSummaryDto dto) {
        try {
            log.info("📦 [StockSummary] Creating StockSummaryEntity...");
            log.info("   - Unit: {}", dto.getUnit());
            log.info("   - Store: {}", dto.getStore());
            log.info("   - Storage Area: {}", dto.getStorageArea());
            log.info("   - Product Category: {}", dto.getProductCategory());
            log.info("   - Item Description: {}", dto.getItemDescription());
            log.info("   - Brand: {}", dto.getBrand());
            log.info("   - Section No: {}", dto.getSectionNo());
            log.info("   - Quantity Kg: {}", dto.getQuantityKg());

            StockSummaryEntity entity = StockSummaryEntity.builder()
                    .unit(dto.getUnit())
                    .store(dto.getStore())
                    .storageArea(dto.getStorageArea())
                    .rackColumnShelfNumber(dto.getRackColumnShelfNumber())
                    .productCategory(dto.getProductCategory())
                    .itemDescription(dto.getItemDescription())
                    .brand(dto.getBrand())
                    .grade(dto.getGrade())
                    .temper(dto.getTemper())
                    .dimension(dto.getDimension())
                    .quantityKg(dto.getQuantityKg())
                    .materialType(dto.getMaterialType())
                    .quantityNo(dto.getQuantityNo())
                    .itemPrice(dto.getItemPrice())
                    .reprintQr(dto.getReprintQr())
                    .sectionNo(dto.getSectionNo())
                    .build();

            log.info("   ✅ Entity built. Before saving - Section No: {}", entity.getSectionNo());
            StockSummaryEntity saved = repository.save(entity);
            log.info("   💾 StockSummary saved to DB - ID: {}, Section No: {}", saved.getId(), saved.getSectionNo());

            return saved;
        } catch (Exception e) {
            log.error("   ❌ Error creating stock entry: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create stock entry: " + e.getMessage());
        }
    }

    @Override
    public StockSummaryEntity update(Long id, StockSummaryDto dto) {
        try {
            StockSummaryEntity entity = repository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Stock with ID " + id + " not found"));

            entity.setUnit(dto.getUnit());
            entity.setStore(dto.getStore());
            entity.setStorageArea(dto.getStorageArea());
            entity.setRackColumnShelfNumber(dto.getRackColumnShelfNumber());
            entity.setProductCategory(dto.getProductCategory());
            entity.setItemDescription(dto.getItemDescription());
            entity.setBrand(dto.getBrand());
            entity.setGrade(dto.getGrade());
            entity.setTemper(dto.getTemper());
            entity.setDimension(dto.getDimension());
            entity.setQuantityKg(dto.getQuantityKg());
            entity.setQuantityNo(dto.getQuantityNo());
            entity.setMaterialType(dto.getMaterialType());
            entity.setItemPrice(dto.getItemPrice());
            entity.setReprintQr(dto.getReprintQr());

            return repository.save(entity);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update stock entry: " + e.getMessage());
        }
    }

    @Override
    public StockSummaryEntity getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock with ID " + id + " not found"));
    }

    @Override
    public List<StockSummaryEntity> getAll() {
        return repository.findAll();
    }

    @Override
    public StockSummaryEntity delete(Long id) {
        StockSummaryEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock with ID " + id + " not found"));

        repository.delete(entity);
        return entity;
    }

    @Override
    public void deleteAll() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     🗑️  DELETE ALL STOCK SUMMARY       ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total stock entries before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All stock summary entries deleted successfully!");
            System.out.println("📊 Total stock entries after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all stock entries: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all stock entries: " + e.getMessage());
        }
    }

    private String generatePRNumber() {
        LocalDate now = LocalDate.now();
        String yy = String.format("%02d", now.getYear() % 100);
        String mm = String.format("%02d", now.getMonthValue());

        // Example: MEINYYMM#### → MEIN25120001
        String prefix = "MEIN" + yy + mm;

        // Fetch last sequence from DB for this month
        String lastPr = poRequestRepository.findLastPrNumberForMonth(prefix);
        int seq = 1;
        if (lastPr != null && lastPr.startsWith(prefix)) {
            String numPart = lastPr.substring(prefix.length());
            seq = Integer.parseInt(numPart) + 1;
        }

        return prefix + String.format("%04d", seq);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockSummaryWithItemDetailsDTO> getFilteredSummary(
            String unit,
            List<String> brands,
            List<String> productCategories,
            List<String> materialTypes) {
        log.info("=== Starting getFilteredSummary ===");
        log.info("Input params → unit={}, brands={}, productCategories={}, materialTypes={}",
                unit, brands, productCategories, materialTypes);

        // Normalize filters
        unit = (unit != null && !unit.isBlank()) ? unit.trim() : null;
        if (brands != null && brands.contains("ALL")) {
            log.info("ALL brand detected → ignoring brand filter");
            brands = null;
        }
        productCategories = (productCategories != null && !productCategories.isEmpty()) ? productCategories : null;
        materialTypes = (materialTypes != null && !materialTypes.isEmpty()) ? materialTypes : null;

        // 1️⃣ Fetch stock summary
        List<StockSummaryEntity> stockList = repository.filterStockSummary(unit, brands, productCategories,
                materialTypes);
        log.info("Fetched {} stock records from repository", stockList.size());
        if (stockList.isEmpty()) {
            log.warn("No stock records found → returning empty list");
            return Collections.emptyList();
        }

        // 2️⃣ Aggregate quantities
        Map<String, StockSummaryEntity> aggregated = new HashMap<>();
        for (StockSummaryEntity stock : stockList) {
            String key = stock.getUnit() + "|" + stock.getProductCategory() + "|" +
                    stock.getMaterialType() + "|" + stock.getItemDescription();
            aggregated.compute(key, (k, v) -> {
                if (v == null) {
                    log.debug("Creating new aggregated entry for key={}", key);
                    StockSummaryEntity consolidated = new StockSummaryEntity();
                    consolidated.setUnit(stock.getUnit());
                    consolidated.setProductCategory(stock.getProductCategory());
                    consolidated.setMaterialType(stock.getMaterialType());
                    consolidated.setItemDescription(stock.getItemDescription());
                    consolidated.setBrand(stock.getBrand());
                    consolidated.setGrade(stock.getGrade());
                    consolidated.setTemper(stock.getTemper());
                    consolidated.setQuantityKg(stock.getQuantityKg());
                    return consolidated;
                } else {
                    log.debug("Updating aggregated entry for key={} → adding {}", key, stock.getQuantityKg());
                    v.setQuantityKg(v.getQuantityKg().add(stock.getQuantityKg()));
                    return v;
                }
            });
        }
        List<StockSummaryEntity> consolidatedList = new ArrayList<>(aggregated.values());
        log.info("Aggregated into {} consolidated records", consolidatedList.size());

        // 3️⃣ Pre‑fetch sales in one go
        List<String> itemDescs = consolidatedList.stream().map(StockSummaryEntity::getItemDescription).toList();
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(12);
        List<Object[]> raw = soSummaryRepository.getMonthlySaleForItems(unit, itemDescs, startDate);
        log.info("Fetched {} monthly sale records for last 12 months", raw.size());

        Map<String, Map<String, BigDecimal>> saleMap = new HashMap<>();
        for (Object[] row : raw) {
            saleMap.computeIfAbsent((String) row[0], k -> new HashMap<>())
                    .put((String) row[1], (BigDecimal) row[2]);
        }

        // 4️⃣ Pre‑fetch all alerts in one go
        Set<String> existingAlerts = lowStockAlertRepository
                .findAllByUnitAndItemDescriptions(unit, itemDescs)
                .stream()
                .map(a -> a.getUnit() + "|" + a.getMaterialType() + "|" + a.getBrand() + "|" + a.getItemDescription())
                .collect(Collectors.toSet());
        log.info("Fetched {} existing alerts from LowStockAlert table", existingAlerts.size());

        // 5️⃣ Build DTOs
        List<StockSummaryWithItemDetailsDTO> result = new ArrayList<>();
        for (StockSummaryEntity stock : consolidatedList) {
            String itemDesc = stock.getItemDescription();
            log.debug("Processing item={}", itemDesc);

            // ItemMaster lookup
            ItemMasterEntity master = itemMasterRepository.findBySkuDescriptionIgnoreCase(itemDesc)
                    .orElseGet(() -> {
                        List<ItemMasterEntity> similar = itemMasterRepository.findSimilarSku(itemDesc);
                        return similar.isEmpty() ? null : similar.get(0);
                    });

            BigDecimal moq = (master != null && master.getMoq() != null) ? master.getMoq() : BigDecimal.ZERO;
            Integer leadTimeDays = (master != null && master.getLeadTimeDays() != null) ? master.getLeadTimeDays() : 0;

            List<MonthlySaleDTO> monthlySales = fillMonths(saleMap.getOrDefault(itemDesc, Collections.emptyMap()));

            StockSummaryWithItemDetailsDTO dto = new StockSummaryWithItemDetailsDTO();
            dto.setItemDescription(itemDesc);
            dto.setBrand(stock.getBrand());
            dto.setGrade(stock.getGrade());
            dto.setTemper(stock.getTemper());
            dto.setQuantityKg(stock.getQuantityKg());
            dto.setMoq(moq);
            dto.setLeadTimeDays(leadTimeDays);
            dto.setProductCategory(stock.getProductCategory());
            dto.setMaterialType(stock.getMaterialType());
            dto.setLast12MonthsSales(monthlySales);
            dto.setUnit(stock.getUnit());

            calculateConsumptionMetrics(dto, monthlySales);

            String alertKey = dto.getUnit() + "|" + dto.getMaterialType() + "|" + dto.getBrand() + "|"
                    + dto.getItemDescription();
            boolean existsInAlert = existingAlerts.contains(alertKey);

            dto.setStatus(existsInAlert ? "PO Generated Already" : "");
            log.debug("Item={} → existsInAlert={}, status={}", itemDesc, existsInAlert, dto.getStatus());

            // Condition check
            if (!existsInAlert && dto.getReorderLevel() != null &&
                    dto.getQuantityKg().compareTo(dto.getReorderLevel()) < 0) {
                log.info("Low stock condition met for item={} → currentStock={} < reorderLevel={}",
                        dto.getItemDescription(), dto.getQuantityKg(), dto.getReorderLevel());

                String newPrNumber = generatePRNumber();
                log.info("Generated new PRNumber={} for item={}", newPrNumber, dto.getItemDescription());

                // Save alert
                LowStockAlertEntity alert = new LowStockAlertEntity();
                alert.setItemDescription(dto.getItemDescription());
                alert.setBrand(dto.getBrand());
                alert.setGrade(dto.getGrade());
                alert.setTemper(dto.getTemper());
                alert.setQuantityKg(dto.getQuantityKg());
                alert.setReorderLevel(dto.getReorderLevel());
                alert.setReorderQuantity(dto.getReorderQuantity());
                alert.setUnit(dto.getUnit());
                alert.setProductCategory(dto.getProductCategory());
                alert.setMaterialType(dto.getMaterialType());
                alert.setCreatedAt(new Date());
                alert.setPrNumber(newPrNumber);
                lowStockAlertRepository.save(alert);
                log.info("Saved LowStockAlertEntity for item={} with PRNumber={}", dto.getItemDescription(),
                        newPrNumber);

                // Save PORequest
                PORequestEntity poRequest = new PORequestEntity();
                poRequest.setTimeStamp(new Date());
                poRequest.setStatus("PENDING");
                poRequest.setPrNumber(newPrNumber);
                poRequest.setOrderType("INVENTORY ANALYSIS");
                poRequest.setSupplierCode(master != null ? master.getSupplierCode() : "AUTO");
                poRequest.setSupplierName(master != null ? master.getSupplierName() : "AUTO");
                poRequest.setUnit(dto.getUnit());
                poRequest.setUnitCode(dto.getUnit());
                poRequest.setPrCreatedBy("SYSTEM");
                poRequest.setReasonForRequest("Auto generated due to low stock");

                POProductEntity product = POProductEntity.builder()
                        .sectionNo(master != null ? master.getSectionNumber() : null)
                        .itemDescription(dto.getItemDescription())
                        .productCategory(dto.getProductCategory())
                        .brand(dto.getBrand())
                        .grade(dto.getGrade())
                        .temper(dto.getTemper())
                        .requiredQuantity(
                                dto.getReorderQuantity() != null ? dto.getReorderQuantity().intValue() : 0.000)
                        .uom("Kgs")
                        .selected("Y")
                        .poRequest(poRequest)
                        .build();

                poRequest.setProducts(List.of(product));
                poRequestRepository.save(poRequest);
                log.info("Saved PORequestEntity with PRNumber={} for item={}", newPrNumber, dto.getItemDescription());

                dto.setStatus("PO Generated Already");
            }

            result.add(dto);
        }

        log.info("=== Completed getFilteredSummary → returning {} DTOs ===", result.size());
        return result;
    }

    private void calculateConsumptionMetrics(
            StockSummaryWithItemDetailsDTO dto,
            List<MonthlySaleDTO> monthlySales) {
        // 1. Total consumption (exact)
        BigDecimal totalConsumption = monthlySales.stream()
                .map(MonthlySaleDTO::getTotalKg)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.DOWN);

        dto.setTotalConsumption(totalConsumption);

        // 2. Monthly average = total / 12 (exact 4 decimals)
        BigDecimal monthlyAvg = totalConsumption
                .divide(BigDecimal.valueOf(12), 4, RoundingMode.DOWN);
        dto.setMonthlyAverageConsumption(monthlyAvg);

        // 3. Daily consumption = monthlyAvg / 27
        BigDecimal dailyConsumption = monthlyAvg
                .divide(BigDecimal.valueOf(27), 4, RoundingMode.DOWN);
        dto.setDailyConsumption(dailyConsumption);

        // 4. Safety stock = daily × 15
        BigDecimal safetyStock = dailyConsumption
                .multiply(BigDecimal.valueOf(15))
                .setScale(4, RoundingMode.DOWN);
        dto.setSafetyStock(safetyStock);

        // 5. Reorder quantity = daily × MOQ
        BigDecimal moq = dto.getMoq() != null ? dto.getMoq() : BigDecimal.ZERO;
        BigDecimal reorderQty = dailyConsumption
                .multiply(moq)
                .setScale(4, RoundingMode.DOWN);
        dto.setReorderQuantity(reorderQty);

        // 6. Reorder level = safety + reorderQty
        BigDecimal reorderLevel = safetyStock
                .add(reorderQty)
                .setScale(4, RoundingMode.DOWN);
        dto.setReorderLevel(reorderLevel);
    }

    private List<MonthlySaleDTO> fillMonths(Map<String, BigDecimal> monthMap) {

        List<MonthlySaleDTO> list = new ArrayList<>();

        LocalDate now = LocalDate.now().withDayOfMonth(1);
        LocalDate end = now.minusMonths(1);
        LocalDate start = now.minusMonths(12);

        for (int i = 0; i < 12; i++) {
            LocalDate month = end.minusMonths(i);
            String key = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            list.add(new MonthlySaleDTO(
                    key,
                    monthMap.getOrDefault(key, BigDecimal.ZERO)));
        }

        return list; // already newest → oldest
    }

    @Override
    public StockSummaryItemDetailsDTO getDetailsByItemDescription(String itemDescription) {
        StockSummaryEntity entity = repository.findTopByItemDescription(itemDescription)
                .orElseThrow(() -> new RuntimeException("Item not found for description: " + itemDescription));

        return new StockSummaryItemDetailsDTO(
                entity.getItemDescription(),
                entity.getMaterialType(),
                entity.getProductCategory(),
                entity.getBrand(),
                entity.getGrade(),
                entity.getTemper(),
                "Kg", // ✅ UOM
                entity.getSectionNo() // ✅ New field added
        );
    }

    @Override
    public List<StockSummaryFormattedDTO> getFormattedSummary() {
        List<StockSummaryEntity> entities = repository.findAll();

        // Group by itemDescription + unit
        record GroupKey(String itemDescription, String unit) {
        }

        Map<GroupKey, List<StockSummaryEntity>> grouped = entities.stream()
                .collect(Collectors.groupingBy(e -> new GroupKey(e.getItemDescription(), e.getUnit())));

        return grouped.entrySet().stream()
                .map(entry -> {
                    List<StockSummaryEntity> group = entry.getValue();

                    // Select sample: prioritize entry with itemGroup, then GRN numbers
                    StockSummaryEntity sample = group.stream()
                            .filter(e -> e.getItemGroup() != null && !e.getItemGroup().isEmpty())
                            .findFirst()
                            .orElseGet(() -> group.stream()
                                    .filter(e -> e.getGrnNumbers() != null && !e.getGrnNumbers().isEmpty())
                                    .findFirst()
                                    .orElse(group.get(0))); // fallback to first if none have itemGroup or GRN numbers

                    // Get itemGroup from any entry in group (might be in different rack entry)
                    String itemGroupValue = group.stream()
                            .map(StockSummaryEntity::getItemGroup)
                            .filter(ig -> ig != null && !ig.isEmpty())
                            .findFirst()
                            .orElse(null);

                    log.info("🔍 Selected sample for item group: id={}, itemGroup={}, hasGrnNumbers={}",
                            sample.getId(), itemGroupValue,
                            sample.getGrnNumbers() != null && !sample.getGrnNumbers().isEmpty());

                    // Total quantityKg
                    BigDecimal totalKg = group.stream()
                            .map(e -> Optional.ofNullable(e.getQuantityKg()).orElse(BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    // Total quantityNo
                    Integer totalNo = group.stream()
                            .map(e -> Optional.ofNullable(e.getQuantityNo()).orElse(0))
                            .reduce(0, Integer::sum);

                    // Average itemPrice
                    BigDecimal avgPrice = group.stream()
                            .map(e -> Optional.ofNullable(e.getItemPrice()).orElse(BigDecimal.ZERO))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(group.size()), 2, RoundingMode.HALF_UP);

                    // Collect all rack-wise entries with GRN numbers
                    List<RackWiseDTO> rackWise = group.stream()
                            .map(e -> {
                                // Parse GRN numbers for this specific rack entry
                                Object grnNumbersObj = new java.util.ArrayList<>();
                                if (e.getGrnNumbers() != null && !e.getGrnNumbers().trim().isEmpty()) {
                                    try {
                                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                        String grnStr = e.getGrnNumbers().trim();
                                        if (grnStr.startsWith("[") && grnStr.endsWith("]")) {
                                            grnNumbersObj = mapper.readValue(grnStr, Object.class);
                                        } else {
                                            java.util.List<String> grnList = new java.util.ArrayList<>();
                                            grnList.add(grnStr);
                                            grnNumbersObj = grnList;
                                        }
                                    } catch (Exception ex) {
                                        log.warn("Error parsing GRN numbers for rack {}: {}",
                                                e.getRackColumnShelfNumber(), ex.getMessage());
                                    }
                                }

                                return RackWiseDTO.builder()
                                        .rackColumnShelfNumber(e.getRackColumnShelfNumber())
                                        .store(e.getStore())
                                        .quantityKg(e.getQuantityKg())
                                        .quantityNo(e.getQuantityNo())
                                        .itemPrice(e.getItemPrice())
                                        .storageArea(e.getStorageArea())
                                        .grnNumbers(grnNumbersObj) // ✅ Add GRN numbers
                                        .build();
                            })
                            .toList();

                    // Convert grnNumbers JSON string to proper Object (array)
                    Object grnNumbersObj = new java.util.ArrayList<>();
                    if (sample.getGrnNumbers() != null && !sample.getGrnNumbers().trim().isEmpty()) {
                        try {
                            // Parse JSON string to array/list
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            String grnStr = sample.getGrnNumbers().trim();

                            // Check if it's already a JSON array
                            if (grnStr.startsWith("[") && grnStr.endsWith("]")) {
                                // It's a JSON array - parse it
                                grnNumbersObj = mapper.readValue(grnStr, Object.class);
                                log.info("📋 GRN Numbers from DB (JSON array): {}", grnNumbersObj);
                            } else {
                                // It's a plain string - wrap it in a list
                                java.util.List<String> grnList = new java.util.ArrayList<>();
                                grnList.add(grnStr);
                                grnNumbersObj = grnList;
                                log.info("📋 GRN Numbers from DB (plain string): {}", grnNumbersObj);
                            }
                        } catch (Exception e) {
                            log.error("Error processing GRN numbers: {} - Value: {}", e.getMessage(),
                                    sample.getGrnNumbers());
                            grnNumbersObj = new java.util.ArrayList<>();
                        }
                    } else {
                        log.warn("⚠️ GRN Numbers is empty or null for item: {}", sample.getItemDescription());
                    }

                    return StockSummaryFormattedDTO.builder()
                            .itemDescription(entry.getKey().itemDescription())
                            .unit(entry.getKey().unit())
                            .store(sample.getStore())
                            .storageArea(sample.getStorageArea())
                            .productCategory(sample.getProductCategory())
                            .brand(sample.getBrand())
                            .grade(sample.getGrade())
                            .materialType(sample.getMaterialType())
                            .itemGroup(itemGroupValue)
                            .temper(sample.getTemper())
                            .dimension(sample.getDimension())
                            .reprintQr(sample.getReprintQr())
                            .totalQuantityKg(totalKg)
                            .totalQuantityNo(totalNo)
                            .averageItemPrice(avgPrice)
                            .rackWise(rackWise)
                            .build();
                })
                .sorted(Comparator
                        .comparing(StockSummaryFormattedDTO::getItemDescription,
                                Comparator.nullsLast(String::compareToIgnoreCase))
                        .thenComparing(StockSummaryFormattedDTO::getUnit,
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

    @Override
    public List<RackOnlyDTO> getRackOnlySummary(
            String itemDescription,
            String unit,
            List<String> stores,
            List<String> storageAreas) {
        return repository.findAll().stream()
                .filter(e -> itemDescription.equalsIgnoreCase(e.getItemDescription()))
                .filter(e -> unit.equalsIgnoreCase(e.getUnit()))
                .filter(e -> stores == null || stores.isEmpty()
                        || stores.stream().anyMatch(s -> s.equalsIgnoreCase(e.getStore())))
                .filter(e -> storageAreas == null || storageAreas.isEmpty()
                        || storageAreas.stream().anyMatch(sa -> sa.equalsIgnoreCase(e.getStorageArea())))
                .map(e -> RackOnlyDTO.builder()
                        .rackColumnShelfNumber(e.getRackColumnShelfNumber())
                        .store(e.getStore())
                        .storageArea(e.getStorageArea())
                        .quantityKg(e.getQuantityKg())
                        .quantityNo(e.getQuantityNo())
                        .itemPrice(e.getItemPrice())
                        .build())
                .toList();
    }

    @Override
    public List<StockSummaryEntity> bulkCreate(List<StockSummaryDto> dtoList) {

        if (dtoList == null || dtoList.isEmpty()) {
            throw new RuntimeException("Input list cannot be empty");
        }

        List<StockSummaryEntity> entities = new ArrayList<>();

        for (StockSummaryDto dto : dtoList) {
            StockSummaryEntity entity = StockSummaryEntity.builder()
                    .unit(dto.getUnit())
                    .store(dto.getStore())
                    .storageArea(dto.getStorageArea())
                    .rackColumnShelfNumber(dto.getRackColumnShelfNumber())
                    .productCategory(dto.getProductCategory())
                    .itemDescription(dto.getItemDescription())
                    .brand(dto.getBrand())
                    .grade(dto.getGrade())
                    .temper(dto.getTemper())
                    .dimension(dto.getDimension())
                    .quantityKg(dto.getQuantityKg())
                    .quantityNo(dto.getQuantityNo())
                    .itemPrice(dto.getItemPrice())
                    .materialType(dto.getMaterialType())
                    .reprintQr(dto.getReprintQr())
                    .sectionNo(dto.getSectionNo())
                    .build();

            entities.add(entity);
        }

        return repository.saveAll(entities);
    }

    @Override
    public StockAnalysisDto getStockAnalysisByItemAndUnit(String itemDescription, String unit, String productCategory) {
        log.info("=== Starting getStockAnalysisByItemAndUnit for item={}, unit={}, category={} ===", itemDescription,
                unit, productCategory);

        // 1. Fetch current stock for this item and unit (optionally filtered by
        // productCategory)
        List<StockSummaryEntity> stockList = repository.findAll().stream()
                .filter(s -> s.getItemDescription() != null && s.getItemDescription().equalsIgnoreCase(itemDescription))
                .filter(s -> s.getUnit() != null && s.getUnit().equalsIgnoreCase(unit))
                .filter(s -> productCategory == null || productCategory.isEmpty() ||
                        (s.getProductCategory() != null && s.getProductCategory().equalsIgnoreCase(productCategory)))
                .toList();

        if (stockList.isEmpty()) {
            throw new RuntimeException("No stock found for item: " + itemDescription + " and unit: " + unit +
                    (productCategory != null && !productCategory.isEmpty() ? " and category: " + productCategory : ""));
        }

        // 2. Calculate total current stock (sum of all quantities in different
        // bins/racks)
        BigDecimal currentStock = stockList.stream()
                .map(s -> s.getQuantityKg() != null ? s.getQuantityKg() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total current stock for item={} = {}", itemDescription, currentStock);

        // 3. Fetch last 6 months sales data
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.minusMonths(6);

        List<Object[]> raw = soSummaryRepository.getMonthlySaleForItems(unit, List.of(itemDescription), startDate);
        log.info("Fetched {} monthly sale records for last 6 months", raw.size());

        Map<String, BigDecimal> saleMap = new HashMap<>();
        for (Object[] row : raw) {
            saleMap.put((String) row[1], (BigDecimal) row[2]);
        }

        // 4. Fill 6 months data
        List<MonthlySaleDTO> last6MonthsSales = fillLast6Months(saleMap);

        // 5. Calculate consumption metrics
        BigDecimal totalConsumption = last6MonthsSales.stream()
                .map(MonthlySaleDTO::getTotalKg)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.DOWN);

        // For 6 months data, we calculate differently
        BigDecimal monthlyAverage = totalConsumption
                .divide(BigDecimal.valueOf(6), 4, RoundingMode.DOWN);

        BigDecimal dailyConsumption = monthlyAverage
                .divide(BigDecimal.valueOf(27), 4, RoundingMode.DOWN);

        // 6. Get MOQ from ItemMaster
        ItemMasterEntity master = itemMasterRepository.findBySkuDescriptionIgnoreCase(itemDescription)
                .orElseGet(() -> {
                    List<ItemMasterEntity> similar = itemMasterRepository.findSimilarSku(itemDescription);
                    return similar.isEmpty() ? null : similar.get(0);
                });

        BigDecimal moq = (master != null && master.getMoq() != null) ? master.getMoq() : BigDecimal.ZERO;

        // 7. Calculate safety stock and reorder levels
        BigDecimal safetyStock = dailyConsumption
                .multiply(BigDecimal.valueOf(15))
                .setScale(4, RoundingMode.DOWN);

        BigDecimal reorderQuantity = dailyConsumption
                .multiply(moq)
                .setScale(4, RoundingMode.DOWN);

        BigDecimal reorderLevel = safetyStock
                .add(reorderQuantity)
                .setScale(4, RoundingMode.DOWN);

        log.info(
                "Calculated metrics → totalConsumption={}, monthlyAvg={}, dailyConsumption={}, safetyStock={}, reorderLevel={}",
                totalConsumption, monthlyAverage, dailyConsumption, safetyStock, reorderLevel);

        // 8. Build and return DTO
        StockAnalysisDto dto = StockAnalysisDto.builder()
                .itemDescription(itemDescription)
                .unit(unit)
                .currentStock(currentStock)
                .last6MonthsSales(last6MonthsSales)
                .totalConsumption(totalConsumption)
                .monthlyAverageConsumption(monthlyAverage)
                .dailyConsumption(dailyConsumption)
                .safetyStock(safetyStock)
                .reorderQuantity(reorderQuantity)
                .reorderLevel(reorderLevel)
                .build();

        log.info("=== Completed getStockAnalysisByItemAndUnit ===");
        return dto;
    }

    private List<MonthlySaleDTO> fillLast6Months(Map<String, BigDecimal> monthMap) {
        List<MonthlySaleDTO> list = new ArrayList<>();

        LocalDate now = LocalDate.now().withDayOfMonth(1);
        LocalDate end = now.minusMonths(1);

        for (int i = 0; i < 6; i++) {
            LocalDate month = end.minusMonths(i);
            String key = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            list.add(new MonthlySaleDTO(
                    key,
                    monthMap.getOrDefault(key, BigDecimal.ZERO)));
        }

        return list; // newest → oldest
    }

    @Override
    public List<Map<String, Object>> searchByUnitAndItemDescription(String unit, String itemDescription) {
        log.info("🔍 Searching Stock Summary by Unit and ItemDescription...");
        log.info("   - Unit: '{}'", unit);
        log.info("   - ItemDescription: '{}'", itemDescription);

        List<StockSummaryEntity> allEntries = repository.findAll();

        List<Map<String, Object>> result = allEntries.stream()
                .filter(e -> e.getUnit() != null && e.getUnit().equalsIgnoreCase(unit) &&
                        e.getItemDescription() != null && e.getItemDescription().equalsIgnoreCase(itemDescription))
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("id", entry.getId());
                    map.put("store", entry.getStore());
                    map.put("storageArea", entry.getStorageArea());
                    map.put("rackColumnShelfNumber", entry.getRackColumnShelfNumber());
                    map.put("quantityKg", entry.getQuantityKg());
                    map.put("quantityNo", entry.getQuantityNo());
                    map.put("itemPrice", entry.getItemPrice());

                    // Parse GRN numbers
                    List<String> grnList = new ArrayList<>();
                    if (entry.getGrnNumbers() != null && !entry.getGrnNumbers().isEmpty()) {
                        try {
                            String grnStr = entry.getGrnNumbers().trim();
                            // Check if JSON array format
                            if (grnStr.startsWith("[")) {
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                grnList = mapper.readValue(grnStr,
                                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));
                            } else {
                                // Plain string format (comma-separated)
                                String[] parts = grnStr.split(",");
                                for (String part : parts) {
                                    String trimmed = part.trim();
                                    if (!trimmed.isEmpty()) {
                                        grnList.add(trimmed);
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("⚠️ Error parsing GRN numbers: {}", e.getMessage());
                            // Add as is if parsing fails
                            grnList.add(entry.getGrnNumbers());
                        }
                    }
                    map.put("grnNumbers", grnList);
                    map.put("brand", entry.getBrand());
                    map.put("grade", entry.getGrade());
                    map.put("temper", entry.getTemper());
                    map.put("productCategory", entry.getProductCategory());

                    return map;
                })
                .collect(Collectors.toList());

        log.info("   ✅ Found {} location(s)", result.size());
        for (Map<String, Object> item : result) {
            log.info("      📍 Store: {}, Area: {}, Rack: {}, GRNs: {}",
                    item.get("store"), item.get("storageArea"),
                    item.get("rackColumnShelfNumber"), item.get("grnNumbers"));
        }

        return result;
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> saveReturnStock(ReturnStockDTO dto) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            📦 RETURN STOCK SAVE - NEW IMPLEMENTATION                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ INPUT PARAMETERS                                                            │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ Unit           : " + dto.getUnit());
        System.out.println("│ Item Desc      : " + dto.getItemDescription());
        System.out
                .println("│ Return Entries : " + (dto.getReturnEntries() != null ? dto.getReturnEntries().size() : 0));
        System.out.println("│ Target Store   : Loose Piece (fixed)");
        System.out.println("│ Item Group     : RAW MATERIAL (fixed)");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        List<StockSummaryEntity> savedEntities = new java.util.ArrayList<>();
        List<StockSummaryBundleEntity> savedBundles = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();

        String targetStore = "Loose Piece";
        String targetItemGroup = "RAW MATERIAL";

        if (dto.getReturnEntries() == null || dto.getReturnEntries().isEmpty()) {
            System.out.println("\n⚠️ No return entries provided - ABORTING");
            return java.util.Map.of(
                    "success", false,
                    "message", "No return entries provided",
                    "savedCount", 0);
        }

        for (int i = 0; i < dto.getReturnEntries().size(); i++) {
            ReturnStockDTO.ReturnEntryDTO entry = dto.getReturnEntries().get(i);

            System.out.println("\n┌─ ENTRY [" + (i + 1) + "/" + dto.getReturnEntries().size()
                    + "] ────────────────────────────────────────────────────────┐");
            System.out.println("│ 📋 ENTRY DETAILS:");
            System.out.println("│    Batch Number (GRN): " + entry.getBatchNumber());
            System.out.println("│    Date of Inward    : " + entry.getDateOfInward());
            System.out.println("│    Return Qty        : " + entry.getReturnQuantityKg() + " KG | "
                    + entry.getReturnQuantityNo() + " NO");
            System.out.println("│    Storage Area      : " + entry.getStorageArea());
            System.out.println("│    Rack/Bin          : " + entry.getRackColumnBin());
            System.out.println("│    Dimension         : " + entry.getDimension());
            System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

            try {
                String storageArea = entry.getStorageArea() != null ? entry.getStorageArea() : "Common";
                String rackColumnBin = entry.getRackColumnBin() != null ? entry.getRackColumnBin() : "Common";
                String dimension = entry.getDimension();
                String grnNumber = entry.getBatchNumber();

                // ═══════════════════════════════════════════════════════════════════
                // STEP 1: CHECK IF STOCK SUMMARY ENTRY EXISTS (unit + itemDesc + itemGroup +
                // store + storageArea + rack + dimension)
                // ═══════════════════════════════════════════════════════════════════
                System.out.println("│ 🔍 STEP 1: Checking for existing Stock Summary entry...");
                System.out.println("│    Search Criteria:");
                System.out.println("│       - Unit        : '" + dto.getUnit() + "'");
                System.out.println("│       - ItemDesc    : '" + dto.getItemDescription() + "'");
                System.out.println("│       - ItemGroup   : '" + targetItemGroup + "'");
                System.out.println("│       - Store       : '" + targetStore + "'");
                System.out.println("│       - StorageArea : '" + storageArea + "'");
                System.out.println("│       - Rack        : '" + rackColumnBin + "'");
                System.out.println("│       - Dimension   : '" + dimension + "'");

                Optional<StockSummaryEntity> existingOpt = repository
                        .findByUnitAndItemDescriptionAndItemGroupAndStoreAndStorageAreaAndRackColumnShelfNumberAndDimension(
                                dto.getUnit(),
                                dto.getItemDescription(),
                                targetItemGroup,
                                targetStore,
                                storageArea,
                                rackColumnBin,
                                dimension);

                StockSummaryEntity stockSummary;

                if (existingOpt.isPresent()) {
                    // ═══════════════════════════════════════════════════════════════════
                    // EXISTING ENTRY FOUND - UPDATE QUANTITY & GRN
                    // ═══════════════════════════════════════════════════════════════════
                    stockSummary = existingOpt.get();
                    System.out.println("│");
                    System.out.println("│ ✅ FOUND EXISTING ENTRY - Stock ID: " + stockSummary.getId());

                    // Update quantity
                    BigDecimal existingQtyKg = stockSummary.getQuantityKg() != null ? stockSummary.getQuantityKg()
                            : BigDecimal.ZERO;
                    Integer existingQtyNo = stockSummary.getQuantityNo() != null ? stockSummary.getQuantityNo() : 0;

                    BigDecimal newQtyKg = existingQtyKg
                            .add(entry.getReturnQuantityKg() != null ? entry.getReturnQuantityKg() : BigDecimal.ZERO);
                    Integer newQtyNo = existingQtyNo
                            + (entry.getReturnQuantityNo() != null ? entry.getReturnQuantityNo() : 0);

                    System.out.println("│    📊 QUANTITY UPDATE:");
                    System.out.println("│       Qty BEFORE : " + existingQtyKg + " KG | " + existingQtyNo + " NO");
                    System.out.println("│       Adding     : " + entry.getReturnQuantityKg() + " KG | "
                            + entry.getReturnQuantityNo() + " NO");
                    System.out.println("│       Qty AFTER  : " + newQtyKg + " KG | " + newQtyNo + " NO");

                    stockSummary.setQuantityKg(newQtyKg);
                    stockSummary.setQuantityNo(newQtyNo);

                    // Update GRN Numbers
                    if (grnNumber != null && !grnNumber.isEmpty()) {
                        String existingGrns = stockSummary.getGrnNumbers();
                        if (existingGrns == null || existingGrns.isEmpty() || existingGrns.equals("[]")) {
                            existingGrns = "[\"" + grnNumber + "\"]";
                        } else {
                            if (!existingGrns.contains("\"" + grnNumber + "\"")) {
                                existingGrns = existingGrns.replace("]", ",\"" + grnNumber + "\"]");
                            }
                        }
                        stockSummary.setGrnNumbers(existingGrns);
                        System.out.println("│    📝 GRN Numbers: " + existingGrns);
                    }

                    // Update QR Code if provided
                    if (entry.getQrCode() != null && !entry.getQrCode().isEmpty()) {
                        stockSummary.setQrCode(entry.getQrCode());
                    }

                    stockSummary = repository.save(stockSummary);
                    System.out.println("│");
                    System.out.println("│ 💾 STOCK SUMMARY UPDATED - ID: " + stockSummary.getId());

                } else {
                    // ═══════════════════════════════════════════════════════════════════
                    // NO EXISTING ENTRY - CREATE NEW STOCK SUMMARY
                    // ═══════════════════════════════════════════════════════════════════
                    System.out.println("│");
                    System.out.println("│ 🆕 NO EXISTING ENTRY - CREATING NEW STOCK SUMMARY");

                    stockSummary = StockSummaryEntity.builder()
                            .unit(dto.getUnit())
                            .store(targetStore)
                            .itemGroup(targetItemGroup)
                            .storageArea(storageArea)
                            .rackColumnShelfNumber(rackColumnBin)
                            .itemDescription(dto.getItemDescription())
                            .dimension(dimension)
                            .quantityKg(entry.getReturnQuantityKg())
                            .quantityNo(entry.getReturnQuantityNo())
                            .grnNumbers(grnNumber != null ? "[\"" + grnNumber + "\"]" : null)
                            .qrCode(entry.getQrCode())
                            .pickListLocked(false)
                            .reprintQr(false)
                            .build();

                    stockSummary = repository.save(stockSummary);
                    System.out.println("│    ✅ NEW STOCK SUMMARY CREATED - ID: " + stockSummary.getId());
                }

                savedEntities.add(stockSummary);

                // ═══════════════════════════════════════════════════════════════════
                // STEP 2: SAVE BUNDLE TO StockSummaryBundleEntity
                // ═══════════════════════════════════════════════════════════════════
                System.out.println("│");
                System.out.println("│ 📦 STEP 2: Saving Bundle to StockSummaryBundleEntity...");

                // Get GRN details for bundle
                GRNEntity grnEntity = null;
                if (grnNumber != null && !grnNumber.isEmpty()) {
                    Optional<GRNEntity> grnOpt = grnRepository.findByGrnRefNumber(grnNumber);
                    if (grnOpt.isPresent()) {
                        grnEntity = grnOpt.get();
                        System.out.println("│    Found GRN Entity: " + grnEntity.getGrnRefNumber());
                    }
                }

                StockSummaryBundleEntity bundle = StockSummaryBundleEntity.builder()
                        .stockSummary(stockSummary)
                        .grnNumber(grnNumber)
                        .grnId(grnEntity != null ? grnEntity.getId() : null)
                        .itemDescription(dto.getItemDescription())
                        .dimension(dimension)
                        .weightmentQuantityKg(entry.getReturnQuantityKg())
                        .weightmentQuantityNo(entry.getReturnQuantityNo())
                        .currentStore(targetStore)
                        .storageArea(storageArea)
                        .rackColumnBinNumber(rackColumnBin)
                        .poNumber(grnEntity != null ? grnEntity.getPoNumber() : null)
                        .qrCodeUrl(entry.getQrCode())
                        .status("RETURNED")
                        .transferType("RETURN_STOCK")
                        .build();

                StockSummaryBundleEntity savedBundle = stockSummaryBundleRepository.save(bundle);
                savedBundles.add(savedBundle);

                System.out.println("│    ✅ BUNDLE SAVED - ID: " + savedBundle.getId());
                System.out.println("│       - Linked to Stock Summary ID: " + stockSummary.getId());
                System.out.println("│       - GRN Number: " + grnNumber);
                System.out.println("│       - Return Qty: " + entry.getReturnQuantityKg() + " KG | "
                        + entry.getReturnQuantityNo() + " NO");
                System.out.println("│       - Dimension: " + dimension);
                System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

            } catch (Exception e) {
                String error = "Error processing entry " + (i + 1) + ": " + e.getMessage();
                System.out.println("│ ❌ ERROR: " + error);
                System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
                e.printStackTrace();
                errors.add(error);
            }
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            📊 RETURN STOCK SAVE - SUMMARY                                   ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Entries Processed     : " + dto.getReturnEntries().size());
        System.out.println("║ Stock Summaries Saved : " + savedEntities.size());
        System.out.println("║ Bundles Saved         : " + savedBundles.size());
        System.out.println("║ Errors                : " + errors.size());
        System.out.println("║ Status                : " + (errors.isEmpty() ? "✅ SUCCESS" : "⚠️ PARTIAL SUCCESS"));
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝\n");

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("success", errors.isEmpty());
        response.put("message",
                errors.isEmpty() ? "Return stock saved successfully" : "Return stock saved with some errors");
        response.put("savedStockSummaryCount", savedEntities.size());
        response.put("savedBundleCount", savedBundles.size());
        response.put("errorCount", errors.size());
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }
        response.put("data", savedEntities);

        return response;
    }

    /*
     * //
     * ═════════════════════════════════════════════════════════════════════════════
     * ══
     * // OLD saveReturnStock METHOD - COMMENTED OUT
     * //
     * ═════════════════════════════════════════════════════════════════════════════
     * ══
     * 
     * @Override
     * 
     * @Transactional
     * public java.util.Map<String, Object> saveReturnStock_OLD(ReturnStockDTO dto)
     * {
     * System.out.println(
     * "\n╔══════════════════════════════════════════════════════════════════════════════╗"
     * );
     * System.out.
     * println("║            📦 RETURN STOCK SAVE - STOCK SUMMARY                             ║"
     * );
     * System.out.println(
     * "╚══════════════════════════════════════════════════════════════════════════════╝"
     * );
     * System.out.println(
     * "┌──────────────────────────────────────────────────────────────────────────────┐"
     * );
     * System.out.
     * println("│ INPUT PARAMETERS                                                            │"
     * );
     * System.out.println(
     * "├──────────────────────────────────────────────────────────────────────────────┤"
     * );
     * System.out.println("│ Unit           : " + dto.getUnit());
     * System.out.println("│ Item Desc      : " + dto.getItemDescription());
     * System.out.println("│ Return Entries : " + (dto.getReturnEntries() != null ?
     * dto.getReturnEntries().size() : 0));
     * System.out.println("│ Target Store   : Loose Piece (fixed)");
     * System.out.println(
     * "└──────────────────────────────────────────────────────────────────────────────┘"
     * );
     * 
     * List<StockSummaryEntity> savedEntities = new java.util.ArrayList<>();
     * List<String> errors = new java.util.ArrayList<>();
     * 
     * String targetStore = "Loose Piece"; // Store is always "Loose Piece"
     * 
     * if (dto.getReturnEntries() == null || dto.getReturnEntries().isEmpty()) {
     * System.out.println("\n⚠️ No return entries provided - ABORTING");
     * return java.util.Map.of(
     * "success", false,
     * "message", "No return entries provided",
     * "savedCount", 0
     * );
     * }
     * 
     * for (int i = 0; i < dto.getReturnEntries().size(); i++) {
     * ReturnStockDTO.ReturnEntryDTO entry = dto.getReturnEntries().get(i);
     * 
     * System.out.println("\n┌─ ENTRY [" + (i + 1) + "/" +
     * dto.getReturnEntries().size() +
     * "] ────────────────────────────────────────────────────────┐");
     * System.out.println("│ 📋 ENTRY DETAILS:");
     * System.out.println("│    Batch Number     : " + entry.getBatchNumber());
     * System.out.println("│    Date of Inward   : " + entry.getDateOfInward());
     * System.out.println("│    Return Qty       : " + entry.getReturnQuantityKg() +
     * " KG | " + entry.getReturnQuantityNo() + " NO");
     * System.out.println("│    Storage Area     : " + entry.getStorageArea());
     * System.out.println("│    Rack/Bin         : " + entry.getRackColumnBin());
     * System.out.println("│    Dimension        : " + entry.getDimension());
     * System.out.println("│    Allocation Status: " + entry.getAllocationStatus());
     * System.out.println("│    QR Code          : " + (entry.getQrCode() != null ?
     * entry.getQrCode().substring(0, Math.min(50, entry.getQrCode().length())) +
     * "..." : "null"));
     * System.out.println(
     * "├──────────────────────────────────────────────────────────────────────────────┤"
     * );
     * 
     * try {
     * String storageArea = entry.getStorageArea() != null ? entry.getStorageArea()
     * : "Common";
     * String rackColumnBin = entry.getRackColumnBin() != null ?
     * entry.getRackColumnBin() : "Common";
     * 
     * // Check if same unit + item description + Loose Piece store + storage area +
     * rack exists
     * System.out.
     * println("│ 🔍 STEP 1: Checking for existing Stock Summary entry...");
     * System.out.println("│    Search Criteria:");
     * System.out.println("│       - Unit        : '" + dto.getUnit() + "'");
     * System.out.println("│       - ItemDesc    : '" + dto.getItemDescription() +
     * "'");
     * System.out.println("│       - ItemGroup   : 'RAW MATERIAL'");
     * System.out.println("│       - Store       : '" + targetStore + "'");
     * System.out.println("│       - StorageArea : '" + storageArea + "'");
     * System.out.println("│       - Rack        : '" + rackColumnBin + "'");
     * System.out.println("│       - Dimension   : '" + entry.getDimension() + "'");
     * 
     * // Search by unit + itemDesc + itemGroup(RAW MATERIAL) + store + storageArea
     * + rack + dimension
     * Optional<StockSummaryEntity> existingOpt = repository.
     * findByUnitAndItemDescriptionAndItemGroupAndStoreAndStorageAreaAndRackColumnShelfNumberAndDimension(
     * dto.getUnit(),
     * dto.getItemDescription(),
     * "RAW MATERIAL",
     * targetStore,
     * storageArea,
     * rackColumnBin,
     * entry.getDimension()
     * );
     * 
     * if (existingOpt.isPresent()) {
     * // Update existing entry
     * StockSummaryEntity existing = existingOpt.get();
     * System.out.println("│");
     * System.out.println("│ ✅ FOUND EXISTING ENTRY - UPDATING");
     * System.out.println("│    Stock ID: " + existing.getId());
     * System.out.println("│    Current Dimension: " + existing.getDimension());
     * 
     * java.math.BigDecimal existingQtyKg = existing.getQuantityKg() != null ?
     * existing.getQuantityKg() : java.math.BigDecimal.ZERO;
     * Integer existingQtyNo = existing.getQuantityNo() != null ?
     * existing.getQuantityNo() : 0;
     * 
     * java.math.BigDecimal newQtyKg = existingQtyKg.add(entry.getReturnQuantityKg()
     * != null ? entry.getReturnQuantityKg() : java.math.BigDecimal.ZERO);
     * Integer newQtyNo = existingQtyNo + (entry.getReturnQuantityNo() != null ?
     * entry.getReturnQuantityNo() : 0);
     * 
     * System.out.println("│");
     * System.out.println("│    📊 QUANTITY UPDATE:");
     * System.out.println("│       Qty BEFORE : " + existingQtyKg + " KG | " +
     * existingQtyNo + " NO");
     * System.out.println("│       Adding     : " + entry.getReturnQuantityKg() +
     * " KG | " + entry.getReturnQuantityNo() + " NO");
     * System.out.println("│       Qty AFTER  : " + newQtyKg + " KG | " + newQtyNo +
     * " NO");
     * 
     * existing.setQuantityKg(newQtyKg);
     * existing.setQuantityNo(newQtyNo);
     * 
     * // Update dimension if provided
     * if (entry.getDimension() != null && !entry.getDimension().isEmpty()) {
     * String oldDim = existing.getDimension();
     * existing.setDimension(entry.getDimension());
     * System.out.println("│");
     * System.out.println("│    📐 DIMENSION UPDATE:");
     * System.out.println("│       Old Dimension: " + oldDim);
     * System.out.println("│       New Dimension: " + entry.getDimension());
     * }
     * 
     * // Append GRN to grnNumbers if batch number provided
     * if (entry.getBatchNumber() != null && !entry.getBatchNumber().isEmpty()) {
     * String existingGrns = existing.getGrnNumbers();
     * String grnsBefore = existingGrns;
     * if (existingGrns == null || existingGrns.isEmpty() ||
     * existingGrns.equals("[]")) {
     * existingGrns = "[\"" + entry.getBatchNumber() + "\"]";
     * } else {
     * if (!existingGrns.contains("\"" + entry.getBatchNumber() + "\"")) {
     * existingGrns = existingGrns.replace("]", ",\"" + entry.getBatchNumber() +
     * "\"]");
     * }
     * }
     * existing.setGrnNumbers(existingGrns);
     * System.out.println("│");
     * System.out.println("│    📝 GRN UPDATE:");
     * System.out.println("│       GRNs BEFORE: " + grnsBefore);
     * System.out.println("│       GRNs AFTER : " + existingGrns);
     * }
     * 
     * // Store QR Code if provided
     * if (entry.getQrCode() != null && !entry.getQrCode().isEmpty()) {
     * existing.setQrCode(entry.getQrCode());
     * System.out.println("│    QR Code Set   : ✅");
     * }
     * 
     * StockSummaryEntity saved = repository.save(existing);
     * savedEntities.add(saved);
     * System.out.println("│");
     * System.out.println("│ 💾 STOCK SUMMARY SAVED - Stock ID: " + saved.getId());
     * 
     * // ═══════════════════════════════════════════════════════════════════
     * // STEP 2: SAVE BUNDLE TO StockSummaryBundleEntity
     * // ═══════════════════════════════════════════════════════════════════
     * System.out.println("│");
     * System.out.
     * println("│ 📦 STEP 2: Saving Bundle to StockSummaryBundleEntity...");
     * 
     * // Get GRN details for bundle
     * GRNEntity grnEntity = null;
     * if (entry.getBatchNumber() != null && !entry.getBatchNumber().isEmpty()) {
     * Optional<GRNEntity> grnOpt =
     * grnRepository.findByGrnRefNumber(entry.getBatchNumber());
     * if (grnOpt.isPresent()) {
     * grnEntity = grnOpt.get();
     * System.out.println("│    Found GRN: " + grnEntity.getGrnRefNumber());
     * }
     * }
     * 
     * StockSummaryBundleEntity returnBundle = StockSummaryBundleEntity.builder()
     * .stockSummary(saved)
     * .grnNumber(entry.getBatchNumber())
     * .grnId(grnEntity != null ? grnEntity.getId() : null)
     * .itemDescription(dto.getItemDescription())
     * .dimension(entry.getDimension())
     * .weightmentQuantityKg(entry.getReturnQuantityKg())
     * .weightmentQuantityNo(entry.getReturnQuantityNo())
     * .currentStore(targetStore)
     * .storageArea(storageArea)
     * .rackColumnBinNumber(rackColumnBin)
     * .poNumber(grnEntity != null ? grnEntity.getPoNumber() : null)
     * .status("RETURNED")
     * .transferType("RETURN_STOCK")
     * .qrCodeUrl(entry.getQrCode())
     * .build();
     * 
     * StockSummaryBundleEntity savedBundle =
     * stockSummaryBundleRepository.save(returnBundle);
     * System.out.println("│    ✅ Bundle SAVED - ID: " + savedBundle.getId());
     * System.out.println("│       - Linked to Stock Summary ID: " + saved.getId());
     * System.out.println("│       - Return Qty: " + entry.getReturnQuantityKg() +
     * " KG | " + entry.getReturnQuantityNo() + " NO");
     * System.out.println("│       - Dimension: " + entry.getDimension());
     * 
     * } else {
     * // Create new entry
     * System.out.println("│");
     * System.out.println("│ 🆕 NO EXISTING ENTRY FOUND - CREATING NEW");
     * System.out.println("│    New Entry Details:");
     * System.out.println("│       - Unit        : " + dto.getUnit());
     * System.out.println("│       - Store       : " + targetStore);
     * System.out.println("│       - StorageArea : " + storageArea);
     * System.out.println("│       - Rack        : " + rackColumnBin);
     * System.out.println("│       - ItemDesc    : " + dto.getItemDescription());
     * System.out.println("│       - Dimension   : " + entry.getDimension());
     * System.out.println("│       - Qty KG      : " + entry.getReturnQuantityKg());
     * System.out.println("│       - Qty NO      : " + entry.getReturnQuantityNo());
     * System.out.println("│       - GRN         : " + entry.getBatchNumber());
     * 
     * StockSummaryEntity newStock = StockSummaryEntity.builder()
     * .unit(dto.getUnit())
     * .store(targetStore)
     * .itemGroup("RAW MATERIAL")
     * .storageArea(storageArea)
     * .rackColumnShelfNumber(rackColumnBin)
     * .itemDescription(dto.getItemDescription())
     * .dimension(entry.getDimension())
     * .quantityKg(entry.getReturnQuantityKg())
     * .quantityNo(entry.getReturnQuantityNo())
     * .grnNumbers(entry.getBatchNumber() != null ? "[\"" + entry.getBatchNumber() +
     * "\"]" : null)
     * .pickListLocked(false)
     * .reprintQr(false)
     * .build();
     * 
     * // Set QR Code after build
     * if (entry.getQrCode() != null && !entry.getQrCode().isEmpty()) {
     * newStock.setQrCode(entry.getQrCode());
     * System.out.println("│       - QR Code    : Set ✅");
     * }
     * 
     * StockSummaryEntity saved = repository.save(newStock);
     * savedEntities.add(saved);
     * System.out.println("│");
     * System.out.println("│ 💾 STOCK SUMMARY CREATED - Stock ID: " +
     * saved.getId());
     * 
     * // ═══════════════════════════════════════════════════════════════════
     * // STEP 2: SAVE BUNDLE TO StockSummaryBundleEntity (for new entry)
     * // ═══════════════════════════════════════════════════════════════════
     * System.out.println("│");
     * System.out.
     * println("│ 📦 STEP 2: Saving Bundle to StockSummaryBundleEntity...");
     * 
     * // Get GRN details for bundle
     * GRNEntity grnEntityNew = null;
     * if (entry.getBatchNumber() != null && !entry.getBatchNumber().isEmpty()) {
     * Optional<GRNEntity> grnOptNew =
     * grnRepository.findByGrnRefNumber(entry.getBatchNumber());
     * if (grnOptNew.isPresent()) {
     * grnEntityNew = grnOptNew.get();
     * System.out.println("│    Found GRN: " + grnEntityNew.getGrnRefNumber());
     * }
     * }
     * 
     * StockSummaryBundleEntity newBundle = StockSummaryBundleEntity.builder()
     * .stockSummary(saved)
     * .grnNumber(entry.getBatchNumber())
     * .grnId(grnEntityNew != null ? grnEntityNew.getId() : null)
     * .itemDescription(dto.getItemDescription())
     * .dimension(entry.getDimension())
     * .weightmentQuantityKg(entry.getReturnQuantityKg())
     * .weightmentQuantityNo(entry.getReturnQuantityNo())
     * .currentStore(targetStore)
     * .storageArea(storageArea)
     * .rackColumnBinNumber(rackColumnBin)
     * .poNumber(grnEntityNew != null ? grnEntityNew.getPoNumber() : null)
     * .status("RETURNED")
     * .transferType("RETURN_STOCK")
     * .qrCodeUrl(entry.getQrCode())
     * .build();
     * 
     * StockSummaryBundleEntity savedNewBundle =
     * stockSummaryBundleRepository.save(newBundle);
     * System.out.println("│    ✅ Bundle SAVED - ID: " + savedNewBundle.getId());
     * System.out.println("│       - Linked to Stock Summary ID: " + saved.getId());
     * System.out.println("│       - Return Qty: " + entry.getReturnQuantityKg() +
     * " KG | " + entry.getReturnQuantityNo() + " NO");
     * System.out.println("│       - Dimension: " + entry.getDimension());
     * }
     * System.out.println(
     * "└──────────────────────────────────────────────────────────────────────────────┘"
     * );
     * 
     * } catch (Exception e) {
     * String error = "Error processing entry " + (i + 1) + ": " + e.getMessage();
     * System.out.println("│ ❌ ERROR: " + error);
     * System.out.println(
     * "└──────────────────────────────────────────────────────────────────────────────┘"
     * );
     * errors.add(error);
     * }
     * }
     * 
     * System.out.println(
     * "\n╔══════════════════════════════════════════════════════════════════════════════╗"
     * );
     * System.out.
     * println("║            📊 RETURN STOCK SAVE - SUMMARY                                   ║"
     * );
     * System.out.println(
     * "╠══════════════════════════════════════════════════════════════════════════════╣"
     * );
     * System.out.println("║ Entries Processed : " + dto.getReturnEntries().size());
     * System.out.println("║ Successfully Saved: " + savedEntities.size());
     * System.out.println("║ Errors            : " + errors.size());
     * System.out.println("║ Status            : " + (errors.isEmpty() ? "✅ SUCCESS"
     * : "⚠️ PARTIAL SUCCESS"));
     * System.out.println(
     * "╚══════════════════════════════════════════════════════════════════════════════╝\n"
     * );
     * 
     * java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
     * response.put("success", errors.isEmpty());
     * response.put("message", errors.isEmpty() ? "Return stock saved successfully"
     * : "Return stock saved with some errors");
     * response.put("savedCount", savedEntities.size());
     * response.put("errorCount", errors.size());
     * if (!errors.isEmpty()) {
     * response.put("errors", errors);
     * }
     * response.put("data", savedEntities);
     * 
     * return response;
     * }
     */
    // ═══════════════════════════════════════════════════════════════════════════════
    // END OF OLD saveReturnStock METHOD
    // ═══════════════════════════════════════════════════════════════════════════════

    @Override
    public AllocateReturnRackDTO.SuggestedRackDTO allocateReturnRack(AllocateReturnRackDTO dto) {
        log.info("\n══════════════════════════════════════════════════════════════════════════");
        log.info("🏗️ [AllocateReturnRack] ALLOCATING RACK FOR RETURN STOCK");
        log.info("══════════════════════════════════════════════════════════════════════════");
        log.info("   Unit: {}", dto.getUnit());
        log.info("   Item Description: {}", dto.getItemDescription());
        log.info("   Product Category: {}", dto.getProductCategory());
        log.info("   Return Quantity (Kg): {}", dto.getReturnQuantityKg());

        String targetStore = "LOOSE PIECE STORAGE"; // Always Loose Piece

        try {
            // STEP 1: Find all racks matching Product Category
            log.info("\n   📍 STEP 1: Finding racks by Product Category...");
            log.info("      Product Category: {}", dto.getProductCategory());

            List<RackBinMasterEntity> matchingRacks = rackBinMasterRepository
                    .findByItemCategory(dto.getProductCategory());
            log.info("      Found {} racks with category: {}", matchingRacks.size(), dto.getProductCategory());

            if (matchingRacks.isEmpty()) {
                log.warn("      ⚠️ No racks found for product category: {}", dto.getProductCategory());
                return null;
            }

            // STEP 2: Filter racks with available capacity
            log.info("\n   📍 STEP 2: Filtering racks by available capacity...");
            log.info("      Required Capacity: {} KG", dto.getReturnQuantityKg());

            double requiredQty = dto.getReturnQuantityKg().doubleValue();

            List<RackBinMasterEntity> availableRacks = matchingRacks.stream()
                    .filter(rack -> {
                        double binCapacity = 0;
                        try {
                            binCapacity = Double
                                    .parseDouble(rack.getBinCapacity() != null ? rack.getBinCapacity() : "0");
                        } catch (NumberFormatException e) {
                            log.warn("      ⚠️ Invalid bin capacity: {}", rack.getBinCapacity());
                        }

                        double currentStorage = rack.getCurrentStorage() != null ? rack.getCurrentStorage() : 0;
                        double availableCapacity = binCapacity - currentStorage;

                        boolean hasCapacity = availableCapacity >= requiredQty;

                        if (hasCapacity) {
                            log.info("      ✅ Rack: {}/{}/{} - Capacity: {} KG, Current: {} KG, Available: {} KG",
                                    rack.getStorageArea(), rack.getRackNo(), rack.getBinNo(),
                                    binCapacity, currentStorage, availableCapacity);
                        }

                        return hasCapacity;
                    })
                    .toList();

            log.info("      Found {} racks with available capacity", availableRacks.size());

            if (availableRacks.isEmpty()) {
                log.warn("      ⚠️ No racks with sufficient capacity");
                return null;
            }

            // STEP 3: Sort by Storage Area Order (ascending) and Distance (ascending)
            log.info("\n   📍 STEP 3: Sorting by Storage Area Order & Distance...");

            RackBinMasterEntity bestRack = availableRacks.stream()
                    .sorted((r1, r2) -> {
                        // First sort by Storage Area Order (ascending)
                        Integer order1 = r1.getStorageAreaOrder() != null ? r1.getStorageAreaOrder()
                                : Integer.MAX_VALUE;
                        Integer order2 = r2.getStorageAreaOrder() != null ? r2.getStorageAreaOrder()
                                : Integer.MAX_VALUE;

                        int orderComparison = order1.compareTo(order2);
                        if (orderComparison != 0) {
                            return orderComparison;
                        }

                        // Then sort by Distance (ascending)
                        Double distance1 = r1.getDistance() != null ? r1.getDistance() : Double.MAX_VALUE;
                        Double distance2 = r2.getDistance() != null ? r2.getDistance() : Double.MAX_VALUE;

                        return distance1.compareTo(distance2);
                    })
                    .findFirst()
                    .orElse(null);

            if (bestRack == null) {
                log.warn("      ⚠️ No suitable rack found after sorting");
                return null;
            }

            // STEP 4: Build and return suggested rack
            log.info("\n   📍 STEP 4: Building suggested rack response...");

            String rackColumnBin = bestRack.getRackNo() + "-" + bestRack.getColumnNo() + "-" + bestRack.getBinNo();

            double binCapacity = 0;
            try {
                binCapacity = Double.parseDouble(bestRack.getBinCapacity() != null ? bestRack.getBinCapacity() : "0");
            } catch (NumberFormatException e) {
                log.warn("      ⚠️ Invalid bin capacity: {}", bestRack.getBinCapacity());
            }
            double currentStorage = bestRack.getCurrentStorage() != null ? bestRack.getCurrentStorage() : 0;
            double availableCapacity = binCapacity - currentStorage;

            AllocateReturnRackDTO.SuggestedRackDTO suggested = AllocateReturnRackDTO.SuggestedRackDTO.builder()
                    .store(targetStore)
                    .storageArea(bestRack.getStorageArea())
                    .rackColumnBin(rackColumnBin)
                    .availableCapacity(availableCapacity)
                    .distance(bestRack.getDistance())
                    .storageAreaOrder(bestRack.getStorageAreaOrder())
                    .itemCategory(bestRack.getItemCategory())
                    .isAllocated(true)
                    .build();

            log.info("      ✅ ALLOCATED RACK:");
            log.info("         Store: {}", suggested.getStore());
            log.info("         Storage Area: {}", suggested.getStorageArea());
            log.info("         Rack/Bin: {}", suggested.getRackColumnBin());
            log.info("         Available Capacity: {} KG", suggested.getAvailableCapacity());
            log.info("         Distance: {}", suggested.getDistance());
            log.info("         Storage Area Order: {}", suggested.getStorageAreaOrder());

            log.info("\n══════════════════════════════════════════════════════════════════════════");
            log.info("✅ [AllocateReturnRack] COMPLETE");
            log.info("══════════════════════════════════════════════════════════════════════════\n");

            return suggested;

        } catch (Exception e) {
            log.error("   ❌ Error allocating rack: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockSummaryWithBundlesDTO> getAllWithBundles() {
        log.info("\n══════════════════════════════════════════════════════════════════════════");
        log.info("📦 [StockSummary] GET ALL WITH BUNDLES - Fetching all stock summaries with GRN data");
        log.info("══════════════════════════════════════════════════════════════════════════\n");

        try {
            List<StockSummaryEntity> allStockSummaries = repository.findAll();
            log.info("   📊 Total stock summaries found: {}", allStockSummaries.size());

            List<StockSummaryWithBundlesDTO> result = allStockSummaries.stream()
                    .map(entity -> {
                        // Convert bundles to DTOs
                        List<StockSummaryWithBundlesDTO.BundleDTO> bundleDTOs = entity.getBundles() != null
                                ? entity.getBundles().stream()
                                        .map(bundle -> StockSummaryWithBundlesDTO.BundleDTO.builder()
                                                .id(bundle.getId())
                                                .grnNumber(bundle.getGrnNumber())
                                                .grnId(bundle.getGrnId())
                                                .stockTransferId(bundle.getStockTransferId())
                                                .transferNumber(bundle.getTransferNumber())
                                                .transferType(bundle.getTransferType())
                                                .slNo(bundle.getSlNo())
                                                .itemDescription(bundle.getItemDescription())
                                                .productCategory(bundle.getProductCategory())
                                                .sectionNumber(bundle.getSectionNumber())
                                                .brand(bundle.getBrand())
                                                .grade(bundle.getGrade())
                                                .temper(bundle.getTemper())
                                                .weighment(bundle.getWeighment())
                                                .weightmentQuantityKg(bundle.getWeightmentQuantityKg())
                                                .uomNetWeight(bundle.getUomNetWeight())
                                                .weightmentQuantityNo(bundle.getWeightmentQuantityNo())
                                                .uomNo(bundle.getUomNo())
                                                .materialAcceptance(bundle.getMaterialAcceptance())
                                                .currentStore(bundle.getCurrentStore())
                                                .recipientStore(bundle.getRecipientStore())
                                                .storageArea(bundle.getStorageArea())
                                                .rackColumnBinNumber(bundle.getRackColumnBinNumber())
                                                .rackStatus(bundle.getRackStatus())
                                                .qrCodeUrl(bundle.getQrCodeUrl())
                                                .poNumber(bundle.getPoNumber())
                                                .heatNo(bundle.getHeatNo())
                                                .lotNo(bundle.getLotNo())
                                                .testCertificate(bundle.getTestCertificate())
                                                .userId(bundle.getUserId())
                                                .unitId(bundle.getUnitId())
                                                .status(bundle.getStatus())
                                                .createdBy(bundle.getCreatedBy())
                                                .createdDate(bundle.getCreatedDate() != null
                                                        ? bundle.getCreatedDate().toString()
                                                        : null)
                                                .build())
                                        .collect(Collectors.toList())
                                : new ArrayList<>();

                        // Build the main DTO
                        return StockSummaryWithBundlesDTO.builder()
                                .id(entity.getId())
                                .unit(entity.getUnit())
                                .store(entity.getStore())
                                .storageArea(entity.getStorageArea())
                                .rackColumnShelfNumber(entity.getRackColumnShelfNumber())
                                .productCategory(entity.getProductCategory())
                                .itemDescription(entity.getItemDescription())
                                .brand(entity.getBrand())
                                .grade(entity.getGrade())
                                .temper(entity.getTemper())
                                .dimension(entity.getDimension())
                                .quantityKg(entity.getQuantityKg())
                                .quantityNo(entity.getQuantityNo())
                                .itemPrice(entity.getItemPrice())
                                .materialType(entity.getMaterialType())
                                .length(entity.getLength())
                                .width(entity.getWidth())
                                .thickness(entity.getThickness())
                                .batchNumber(entity.getBatchNumber())
                                .itemGroup(entity.getItemGroup())
                                .reprintQr(entity.getReprintQr())
                                .sectionNo(entity.getSectionNo())
                                .qrCode(entity.getQrCode())
                                .pickListLocked(entity.getPickListLocked())
                                .grnNumbers(entity.getGrnNumbers())
                                .bundles(bundleDTOs)
                                .build();
                    })
                    .collect(Collectors.toList());

            log.info("   ✅ Successfully mapped {} stock summaries with bundles", result.size());
            log.info("\n══════════════════════════════════════════════════════════════════════════");
            log.info("✅ [StockSummary] GET ALL WITH BUNDLES - COMPLETE");
            log.info("══════════════════════════════════════════════════════════════════════════\n");

            return result;

        } catch (Exception e) {
            log.error("   ❌ Error fetching stock summaries with bundles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch stock summaries with bundles: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMergedStockWithGrnDetails(String unit, String itemDescription, String itemGroup,
            String dimension) {
        log.info("\n══════════════════════════════════════════════════════════════════════════");
        log.info("📦 [StockSummary] GET MERGED STOCK WITH GRN DETAILS");
        log.info("══════════════════════════════════════════════════════════════════════════");
        log.info("   Filter Criteria:");
        log.info("      - Unit: '{}'", unit);
        log.info("      - Item Description: '{}'", itemDescription);
        log.info("      - Item Group: '{}'", itemGroup);
        log.info("      - Dimension: '{}'", dimension);

        Map<String, Object> response = new LinkedHashMap<>();

        try {
            // Normalize dimension for comparison
            String normalizedDimension = dimension != null ? dimension.trim() : "";

            // Step 1: Filter stock summaries based on criteria
            List<StockSummaryEntity> matchingStocks = repository.findAll().stream()
                    .filter(stock -> {
                        // Unit must match
                        if (unit != null && !unit.isEmpty()) {
                            if (stock.getUnit() == null || !stock.getUnit().equalsIgnoreCase(unit)) {
                                return false;
                            }
                        }
                        // Item Description must match
                        if (itemDescription != null && !itemDescription.isEmpty()) {
                            if (stock.getItemDescription() == null
                                    || !stock.getItemDescription().equalsIgnoreCase(itemDescription)) {
                                return false;
                            }
                        }
                        // Item Group must match
                        if (itemGroup != null && !itemGroup.isEmpty()) {
                            if (stock.getItemGroup() == null || !stock.getItemGroup().equalsIgnoreCase(itemGroup)) {
                                return false;
                            }
                        }
                        // Dimension must match (both empty or equal)
                        String stockDimension = stock.getDimension() != null ? stock.getDimension().trim() : "";
                        if (!normalizedDimension.isEmpty()) {
                            if (!stockDimension.equalsIgnoreCase(normalizedDimension)) {
                                return false;
                            }
                        } else {
                            // If dimension filter is empty, match only empty dimensions
                            if (!stockDimension.isEmpty()) {
                                return false;
                            }
                        }
                        return true;
                    })
                    .toList();

            log.info("   Found {} matching stock entries", matchingStocks.size());

            if (matchingStocks.isEmpty()) {
                response.put("success", false);
                response.put("message", "No matching stock entries found");
                response.put("totalQuantityKg", BigDecimal.ZERO);
                response.put("totalQuantityNo", 0);
                response.put("rackDetails", new ArrayList<>());
                return response;
            }

            // Step 2: Merge quantities
            BigDecimal totalQuantityKg = BigDecimal.ZERO;
            int totalQuantityNo = 0;

            // Collect unique GRN numbers and rack details
            Set<String> allGrnNumbers = new HashSet<>();
            List<Map<String, Object>> rackDetailsList = new ArrayList<>();

            for (StockSummaryEntity stock : matchingStocks) {
                // Add quantities
                if (stock.getQuantityKg() != null) {
                    totalQuantityKg = totalQuantityKg.add(stock.getQuantityKg());
                }
                if (stock.getQuantityNo() != null) {
                    totalQuantityNo += stock.getQuantityNo();
                }

                // Parse GRN numbers from this stock entry
                List<String> grnNumbersList = parseGrnNumbers(stock.getGrnNumbers());

                // Also collect GRN numbers from bundles
                Set<String> stockGrnNumbersSet = new LinkedHashSet<>(grnNumbersList);
                List<StockSummaryBundleEntity> bundles = stock.getBundles();
                if (bundles != null && !bundles.isEmpty()) {
                    for (StockSummaryBundleEntity bundle : bundles) {
                        if (bundle.getGrnNumber() != null && !bundle.getGrnNumber().isEmpty()) {
                            stockGrnNumbersSet.add(bundle.getGrnNumber());
                        }
                    }
                }
                List<String> finalGrnNumbersList = new ArrayList<>(stockGrnNumbersSet);
                allGrnNumbers.addAll(finalGrnNumbersList);

                // Collect rack details
                Map<String, Object> rackDetail = new LinkedHashMap<>();
                rackDetail.put("stockSummaryId", stock.getId());
                rackDetail.put("store", stock.getStore());
                rackDetail.put("storageArea", stock.getStorageArea());
                rackDetail.put("rackColumnShelfNumber", stock.getRackColumnShelfNumber());
                rackDetail.put("quantityKg", stock.getQuantityKg());
                rackDetail.put("quantityNo", stock.getQuantityNo());
                rackDetail.put("grnNumbers", finalGrnNumbersList);

                // Fetch GRN details for each GRN number
                List<Map<String, Object>> grnDetailsList = new ArrayList<>();
                for (String grnNo : finalGrnNumbersList) {
                    Optional<GRNEntity> grnOpt = grnRepository.findByGrnRefNumber(grnNo);
                    if (grnOpt.isPresent()) {
                        GRNEntity grn = grnOpt.get();
                        Map<String, Object> grnDetail = new LinkedHashMap<>();
                        grnDetail.put("grnRefNumber", grn.getGrnRefNumber());
                        grnDetail.put("invoiceNumber", grn.getInvoiceNumber());
                        grnDetail.put("poNumber", grn.getPoNumber());
                        grnDetail.put("supplierName", grn.getSupplierName());
                        grnDetail.put("supplierCode", grn.getSupplierCode());
                        grnDetail.put("vehicleNumber", grn.getVehicleNumber());
                        grnDetail.put("gateEntryRefNo", grn.getGateEntryRefNo());
                        grnDetail.put("ewayBillNumber", grn.getEwayBillNumber());
                        grnDetail.put("timeStamp", grn.getTimeStamp());
                        grnDetail.put("status", grn.getStatus());
                        grnDetail.put("weighmentQuantity", grn.getWeighmentQuantity());
                        grnDetailsList.add(grnDetail);
                    } else {
                        // GRN not found, add minimal info
                        Map<String, Object> grnDetail = new LinkedHashMap<>();
                        grnDetail.put("grnRefNumber", grnNo);
                        grnDetail.put("message", "GRN details not found");
                        grnDetailsList.add(grnDetail);
                    }
                }
                rackDetail.put("grnDetails", grnDetailsList);
                rackDetailsList.add(rackDetail);
            }

            log.info("   Total Quantity KG: {}", totalQuantityKg);
            log.info("   Total Quantity No: {}", totalQuantityNo);
            log.info("   Total Unique GRN Numbers: {}", allGrnNumbers.size());

            // Build response
            response.put("success", true);
            response.put("unit", unit);
            response.put("itemDescription", itemDescription);
            response.put("itemGroup", itemGroup);
            response.put("dimension", dimension);
            response.put("totalQuantityKg", totalQuantityKg);
            response.put("totalQuantityNo", totalQuantityNo);
            response.put("totalMatchingEntries", matchingStocks.size());
            response.put("totalUniqueGrnNumbers", allGrnNumbers.size());
            response.put("allGrnNumbers", new ArrayList<>(allGrnNumbers));
            response.put("rackDetails", rackDetailsList);

            log.info("══════════════════════════════════════════════════════════════════════════");
            log.info("✅ [StockSummary] GET MERGED STOCK WITH GRN DETAILS - COMPLETE");
            log.info("══════════════════════════════════════════════════════════════════════════\n");

            return response;

        } catch (Exception e) {
            log.error("   ❌ Error in getMergedStockWithGrnDetails: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", "Failed to fetch merged stock with GRN details: " + e.getMessage());
            return response;
        }
    }

    /**
     * Parse GRN numbers from string (can be JSON array or comma-separated)
     */
    private List<String> parseGrnNumbers(String grnNumbersStr) {
        List<String> result = new ArrayList<>();
        if (grnNumbersStr == null || grnNumbersStr.isEmpty()) {
            return result;
        }

        try {
            // Check if it's JSON array format
            if (grnNumbersStr.trim().startsWith("[")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                result = mapper.readValue(grnNumbersStr,
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } else {
                // Plain string format (comma-separated or single value)
                String[] parts = grnNumbersStr.split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty() && !trimmed.equals("[]")) {
                        result.add(trimmed);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("      ⚠️ Error parsing GRN numbers: {}", e.getMessage());
            // Try simple split as fallback
            String[] parts = grnNumbersStr.split(",");
            for (String part : parts) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty() && !trimmed.equals("[]")) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAllMergedWithGrnDetails() {
        log.info("\n══════════════════════════════════════════════════════════════════════════");
        log.info("📦 [StockSummary] GET ALL MERGED WITH GRN DETAILS");
        log.info("══════════════════════════════════════════════════════════════════════════");

        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            // Step 1: Get all stock summaries
            List<StockSummaryEntity> allStocks = repository.findAll();
            log.info("   Total stock entries: {}", allStocks.size());

            // Step 2: Group by unit + itemDescription + itemGroup + dimension
            Map<String, List<StockSummaryEntity>> groupedStocks = allStocks.stream()
                    .collect(Collectors.groupingBy(stock -> {
                        String unit = stock.getUnit() != null ? stock.getUnit().trim() : "";
                        String itemDesc = stock.getItemDescription() != null ? stock.getItemDescription().trim() : "";
                        String itemGroup = stock.getItemGroup() != null ? stock.getItemGroup().trim() : "";
                        String dimension = stock.getDimension() != null ? stock.getDimension().trim() : "";
                        return unit + "||" + itemDesc + "||" + itemGroup + "||" + dimension;
                    }));

            log.info("   Total unique groups (unit+itemDesc+itemGroup+dimension): {}", groupedStocks.size());

            // Step 3: Process each group
            for (Map.Entry<String, List<StockSummaryEntity>> entry : groupedStocks.entrySet()) {
                List<StockSummaryEntity> groupStocks = entry.getValue();

                if (groupStocks.isEmpty())
                    continue;

                // Get first entry for common fields
                StockSummaryEntity sample = groupStocks.get(0);

                // Build rackWise array
                List<Map<String, Object>> rackWiseList = new ArrayList<>();

                for (StockSummaryEntity stock : groupStocks) {

                    // Parse GRN numbers from stock entity
                    List<String> grnNumbersList = parseGrnNumbers(stock.getGrnNumbers());

                    // Use a Set to collect unique GRN numbers (including from bundles)
                    Set<String> grnNumbersSet = new LinkedHashSet<>(grnNumbersList);

                    // Fetch bundle details from StockSummaryBundleEntity
                    List<Map<String, Object>> bundleDetailsList = new ArrayList<>();
                    List<StockSummaryBundleEntity> bundles = stock.getBundles();

                    // Calculate quantities from bundles if available
                    BigDecimal rackQuantityKg = BigDecimal.ZERO;
                    int rackQuantityNo = 0;

                    // ✅ Rack price calculation (sum of itemValue and sum of kg for weighted
                    // average)
                    BigDecimal rackItemValueTotal = BigDecimal.ZERO;
                    BigDecimal rackItemKgTotal = BigDecimal.ZERO;

                    if (bundles != null && !bundles.isEmpty()) {

                        for (StockSummaryBundleEntity bundle : bundles) {

                            if (bundle.getWeightmentQuantityKg() != null) {
                                rackQuantityKg = rackQuantityKg.add(bundle.getWeightmentQuantityKg());
                            }
                            if (bundle.getWeightmentQuantityNo() != null) {
                                rackQuantityNo += bundle.getWeightmentQuantityNo();
                            }

                            // Collect GRN number from bundle if not already in list
                            if (bundle.getGrnNumber() != null && !bundle.getGrnNumber().isEmpty()) {
                                grnNumbersSet.add(bundle.getGrnNumber());
                            }

                            // ✅ NULL → ZERO handling
                            BigDecimal price = bundle.getItemPrice() != null
                                    ? bundle.getItemPrice()
                                    : BigDecimal.ZERO;

                            BigDecimal bundleKg = bundle.getWeightmentQuantityKg() != null
                                    ? bundle.getWeightmentQuantityKg()
                                    : BigDecimal.ZERO;

                            // ✅ itemValue = itemPrice * weightmentQuantityKg
                            BigDecimal itemValue = price.multiply(bundleKg);

                            rackItemValueTotal = rackItemValueTotal.add(itemValue);
                            rackItemKgTotal = rackItemKgTotal.add(bundleKg);

                            Map<String, Object> bundleDetail = new LinkedHashMap<>();
                            bundleDetail.put("bundleId", bundle.getId());
                            bundleDetail.put("slNo", bundle.getSlNo());
                            bundleDetail.put("grnNumber", bundle.getGrnNumber());
                            bundleDetail.put("grnId", bundle.getGrnId());
                            bundleDetail.put("itemDescription", bundle.getItemDescription());
                            bundleDetail.put("productCategory", bundle.getProductCategory());
                            bundleDetail.put("sectionNumber", bundle.getSectionNumber());
                            bundleDetail.put("brand", bundle.getBrand());
                            bundleDetail.put("grade", bundle.getGrade());
                            bundleDetail.put("temper", bundle.getTemper());
                            bundleDetail.put("dimension", bundle.getDimension());
                            bundleDetail.put("weighment", bundle.getWeighment());
                            bundleDetail.put("weightmentQuantityKg", bundle.getWeightmentQuantityKg());
                            bundleDetail.put("uomNetWeight", bundle.getUomNetWeight());
                            bundleDetail.put("weightmentQuantityNo", bundle.getWeightmentQuantityNo());
                            bundleDetail.put("uomNo", bundle.getUomNo());
                            bundleDetail.put("itemPrice", price);
                            bundleDetail.put("itemValue", itemValue);
                            bundleDetail.put("materialAcceptance", bundle.getMaterialAcceptance());
                            bundleDetail.put("currentStore", bundle.getCurrentStore());
                            bundleDetail.put("recipientStore", bundle.getRecipientStore());
                            bundleDetail.put("storageArea", bundle.getStorageArea());
                            bundleDetail.put("rackColumnBinNumber", bundle.getRackColumnBinNumber());
                            bundleDetail.put("rackStatus", bundle.getRackStatus());
                            bundleDetail.put("qrCodeUrl", bundle.getQrCodeUrl());
                            bundleDetail.put("poNumber", bundle.getPoNumber());
                            bundleDetail.put("heatNo", bundle.getHeatNo());
                            bundleDetail.put("lotNo", bundle.getLotNo());
                            bundleDetail.put("testCertificate", bundle.getTestCertificate());
                            bundleDetail.put("status", bundle.getStatus());
                            bundleDetail.put("createdBy", bundle.getCreatedBy());
                            bundleDetail.put("createdDate", bundle.getCreatedDate());

                            bundleDetailsList.add(bundleDetail);
                        }
                    } else {
                        rackQuantityKg = stock.getQuantityKg() != null ? stock.getQuantityKg() : BigDecimal.ZERO;
                        rackQuantityNo = stock.getQuantityNo() != null ? stock.getQuantityNo() : 0;
                    }

                    // ✅ Rack average = sum of itemValue / sum of weightmentQuantityKg
                    BigDecimal rackAverageItemPrice = rackItemKgTotal.compareTo(BigDecimal.ZERO) > 0
                            ? rackItemValueTotal.divide(rackItemKgTotal, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    // Use bundle quantities if bundles exist, otherwise use stock quantity
                    BigDecimal finalRackQtyKg = (bundles != null && !bundles.isEmpty()) ? rackQuantityKg
                            : (stock.getQuantityKg() != null ? stock.getQuantityKg() : BigDecimal.ZERO);
                    int finalRackQtyNo = (bundles != null && !bundles.isEmpty()) ? rackQuantityNo
                            : (stock.getQuantityNo() != null ? stock.getQuantityNo() : 0);

                    Map<String, Object> rackEntry = new LinkedHashMap<>();
                    rackEntry.put("rackColumnShelfNumber", stock.getRackColumnShelfNumber());
                    rackEntry.put("store", stock.getStore());
                    rackEntry.put("storageArea", stock.getStorageArea());
                    rackEntry.put("quantityKg", finalRackQtyKg);
                    rackEntry.put("quantityNo", finalRackQtyNo);
                    rackEntry.put("averageItemPrice", rackAverageItemPrice);

                    // Convert Set to List for response (includes GRN numbers from both stock and
                    // bundles)
                    List<String> finalGrnNumbersList = new ArrayList<>(grnNumbersSet);
                    rackEntry.put("grnNumbers", finalGrnNumbersList);
                    rackEntry.put("bundles", bundleDetailsList);

                    // Fetch GRN details for each GRN number
                    List<Map<String, Object>> grnDetailsList = new ArrayList<>();
                    for (String grnNo : finalGrnNumbersList) {
                        Optional<GRNEntity> grnOpt = grnRepository.findByGrnRefNumber(grnNo);
                        if (grnOpt.isPresent()) {
                            GRNEntity grn = grnOpt.get();
                            Map<String, Object> grnDetail = new LinkedHashMap<>();
                            grnDetail.put("grnRefNumber", grn.getGrnRefNumber());
                            grnDetail.put("invoiceNumber", grn.getInvoiceNumber());
                            grnDetail.put("poNumber", grn.getPoNumber());
                            grnDetail.put("supplierName", grn.getSupplierName());
                            grnDetail.put("supplierCode", grn.getSupplierCode());
                            grnDetail.put("vehicleNumber", grn.getVehicleNumber());
                            grnDetail.put("gateEntryRefNo", grn.getGateEntryRefNo());
                            grnDetail.put("ewayBillNumber", grn.getEwayBillNumber());
                            grnDetail.put("timeStamp", grn.getTimeStamp());
                            grnDetail.put("status", grn.getStatus());
                            grnDetail.put("weighmentQuantity", grn.getWeighmentQuantity());
                            grnDetail.put("unit", grn.getUnit());
                            grnDetail.put("testCertificateNumbers", grn.getTestCertificateNumbers());
                            grnDetailsList.add(grnDetail);
                        }
                    }
                    rackEntry.put("grnDetails", grnDetailsList);

                    rackWiseList.add(rackEntry);
                }

                // ✅ MAIN LEVEL CALCULATION
                BigDecimal totalWeightedPrice = BigDecimal.ZERO;
                BigDecimal totalRackKg = BigDecimal.ZERO;

                BigDecimal totalQuantityKg = BigDecimal.ZERO;
                int totalQuantityNo = 0;

                for (Map<String, Object> rack : rackWiseList) {

                    BigDecimal rackAvg = (BigDecimal) rack.get("averageItemPrice");
                    BigDecimal rackQtyKg = (BigDecimal) rack.get("quantityKg");

                    // ✅ Weighted price: averageItemPrice * quantityKg for each rack
                    if (rackAvg != null && rackQtyKg != null && rackAvg.compareTo(BigDecimal.ZERO) > 0
                            && rackQtyKg.compareTo(BigDecimal.ZERO) > 0) {
                        totalWeightedPrice = totalWeightedPrice.add(rackAvg.multiply(rackQtyKg));
                        totalRackKg = totalRackKg.add(rackQtyKg);
                    }

                    List<Map<String, Object>> bundles = (List<Map<String, Object>>) rack.get("bundles");
                    if (bundles != null) {
                        for (Map<String, Object> b : bundles) {
                            BigDecimal kg = (BigDecimal) b.get("weightmentQuantityKg");
                            Integer no = (Integer) b.get("weightmentQuantityNo");

                            if (kg != null)
                                totalQuantityKg = totalQuantityKg.add(kg);
                            if (no != null)
                                totalQuantityNo += no;
                        }
                    }
                }

                // ✅ Main averageItemPrice = sum(averageItemPrice * quantityKg) /
                // sum(quantityKg)
                BigDecimal finalAverageItemPrice = totalRackKg.compareTo(BigDecimal.ZERO) > 0
                        ? totalWeightedPrice.divide(totalRackKg, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

                StockSummaryEntity primaryEntry = groupStocks.stream()
                        .filter(s -> s.getQuantityKg() != null && s.getQuantityKg().compareTo(BigDecimal.ZERO) > 0)
                        .max(Comparator.comparing(StockSummaryEntity::getQuantityKg))
                        .orElse(sample);

                // Build final entry
                Map<String, Object> mergedEntry = new LinkedHashMap<>();
                mergedEntry.put("itemDescription", sample.getItemDescription());
                mergedEntry.put("unit", sample.getUnit());
                mergedEntry.put("store", primaryEntry.getStore() != null ? primaryEntry.getStore() : "");
                mergedEntry.put("storageArea",
                        primaryEntry.getStorageArea() != null ? primaryEntry.getStorageArea() : "");
                mergedEntry.put("rackColumnShelfNumber", primaryEntry.getRackColumnShelfNumber());
                mergedEntry.put("productCategory", sample.getProductCategory());
                mergedEntry.put("brand", sample.getBrand());
                mergedEntry.put("grade", sample.getGrade());
                mergedEntry.put("temper", sample.getTemper());
                mergedEntry.put("dimension", sample.getDimension());
                mergedEntry.put("reprintQr", sample.getReprintQr() != null ? sample.getReprintQr() : false);
                mergedEntry.put("totalQuantityKg", totalQuantityKg);
                mergedEntry.put("totalQuantityNo", totalQuantityNo);
                mergedEntry.put("averageItemPrice", finalAverageItemPrice);

                String materialType = sample.getMaterialType();
                if (materialType == null || materialType.isEmpty()) {
                    String itemDescription = sample.getItemDescription();
                    if (itemDescription != null && !itemDescription.isEmpty()) {
                        Optional<ItemMasterEntity> itemMasterOpt = itemMasterRepository
                                .findBySkuDescriptionIgnoreCase(itemDescription);
                        if (itemMasterOpt.isPresent() && itemMasterOpt.get().getMaterialType() != null) {
                            materialType = itemMasterOpt.get().getMaterialType();
                        } else {
                            materialType = "";
                        }
                    } else {
                        materialType = "";
                    }
                }
                mergedEntry.put("materialType", materialType);
                mergedEntry.put("itemGroup", sample.getItemGroup());
                mergedEntry.put("rackWise", rackWiseList);

                resultList.add(mergedEntry);
            }

            log.info("   Total merged entries: {}", resultList.size());
            log.info("══════════════════════════════════════════════════════════════════════════");
            log.info("✅ [StockSummary] GET ALL MERGED WITH GRN DETAILS - COMPLETE");
            log.info("══════════════════════════════════════════════════════════════════════════\n");

            return resultList;

        } catch (Exception e) {
            log.error("   ❌ Error in getAllMergedWithGrnDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch all merged stock with GRN details: " + e.getMessage());
        }
    }
}

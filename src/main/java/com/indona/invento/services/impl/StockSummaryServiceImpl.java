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

    @Autowired
    private BlockedQuantityRepository blockedQuantityRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ItemEnquiryRepository itemEnquiryRepository;

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // FIX #1: Item Group Short Code Normalization
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    private String normalizeItemGroup(String itemGroup) {
        if (itemGroup == null || itemGroup.isBlank()) return "";
        String upper = itemGroup.trim().toUpperCase();
        if (upper.equals("RAW MATERIAL") || upper.equals("RAW MATERIALS") || upper.equals("RM")) return "RM";
        if (upper.equals("SEMI FINISHED GOODS") || upper.equals("SEMI-FINISHED GOODS") || upper.equals("SFG")) return "SFG";
        if (upper.equals("FINISHED GOODS") || upper.equals("FG")) return "FG";
        return itemGroup; // Return original if no known mapping
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // FIX #2: Material Type Code Mapping
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    private String getMaterialTypeCode(String materialTypeName) {
        if (materialTypeName == null || materialTypeName.isBlank()) return null;
        String upper = materialTypeName.trim().toUpperCase();
        return switch (upper) {
            case "ALUMINIUM", "ALUMINUM" -> "AL";
            case "COPPER" -> "CU";
            case "STAINLESS STEEL" -> "SS";
            case "BRASS" -> "BR";
            case "MILD STEEL", "MS" -> "MS";
            case "GALVANIZED IRON", "GI" -> "GI";
            case "ZINC" -> "ZN";
            case "LEAD" -> "PB";
            case "TIN" -> "SN";
            case "NICKEL" -> "NI";
            default -> null;
        };
    }

    private String formatMaterialType(String materialTypeName) {
        if (materialTypeName == null || materialTypeName.isBlank()) return "";
        String code = getMaterialTypeCode(materialTypeName);
        if (code != null) {
            return materialTypeName.trim() + " - " + code;
        }
        return materialTypeName.trim();
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // FIX #5: Centralized Material Type Resolution
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    public String resolveMaterialType(String materialType, String itemDescription) {
        if (materialType != null && !materialType.isBlank()) return materialType;
        if (itemDescription != null && !itemDescription.isBlank()) {
            Optional<ItemMasterEntity> itemOpt = itemMasterRepository.findBySkuDescriptionIgnoreCase(itemDescription);
            if (itemOpt.isPresent() && itemOpt.get().getMaterialType() != null) {
                return itemOpt.get().getMaterialType();
            }
        }
        return "";
    }

    @Override
    public StockSummaryEntity create(StockSummaryDto dto) {
        try {
            log.info("ðŸ“¦ [StockSummary] Creating StockSummaryEntity...");
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

            log.info("   âœ… Entity built. Before saving - Section No: {}", entity.getSectionNo());
            StockSummaryEntity saved = repository.save(entity);
            log.info("   ðŸ’¾ StockSummary saved to DB - ID: {}, Section No: {}", saved.getId(), saved.getSectionNo());

            return saved;
        } catch (Exception e) {
            log.error("   âŒ Error creating stock entry: {}", e.getMessage(), e);
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
        System.out.println("\nâ•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—");
        System.out.println("â•‘     ðŸ—‘ï¸  DELETE ALL STOCK SUMMARY       â•‘");
        System.out.println("â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");

        try {
            long totalCount = repository.count();
            System.out.println("ðŸ“Š Total stock entries before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("âœ… All stock summary entries deleted successfully!");
            System.out.println("ðŸ“Š Total stock entries after deletion: " + afterCount);
            System.out.println("\nâ•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—");
            System.out.println("â•‘     âœ… DELETION COMPLETE               â•‘");
            System.out.println("â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");
        } catch (Exception e) {
            System.err.println("âŒ Error deleting all stock entries: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all stock entries: " + e.getMessage());
        }
    }

    private String generatePRNumber() {
        LocalDate now = LocalDate.now();
        String yy = String.format("%02d", now.getYear() % 100);
        String mm = String.format("%02d", now.getMonthValue());

        // Example: MEINYYMM#### â†’ MEIN25120001
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
        log.info("Input params â†’ unit={}, brands={}, productCategories={}, materialTypes={}",
                unit, brands, productCategories, materialTypes);

        // Normalize filters
        unit = (unit != null && !unit.isBlank()) ? unit.trim() : null;
        if (brands != null && brands.contains("ALL")) {
            log.info("ALL brand detected â†’ ignoring brand filter");
            brands = null;
        }
        productCategories = (productCategories != null && !productCategories.isEmpty()) ? productCategories : null;
        materialTypes = (materialTypes != null && !materialTypes.isEmpty()) ? materialTypes : null;

        // 1ï¸âƒ£ Fetch stock summary
        List<StockSummaryEntity> stockList = repository.filterStockSummary(unit, brands, productCategories,
                materialTypes);
        log.info("Fetched {} stock records from repository", stockList.size());
        if (stockList.isEmpty()) {
            log.warn("No stock records found â†’ returning empty list");
            return Collections.emptyList();
        }

        // 2ï¸âƒ£ Aggregate quantities
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
                    log.debug("Updating aggregated entry for key={} â†’ adding {}", key, stock.getQuantityKg());
                    v.setQuantityKg(v.getQuantityKg().add(stock.getQuantityKg()));
                    return v;
                }
            });
        }
        List<StockSummaryEntity> consolidatedList = new ArrayList<>(aggregated.values());
        log.info("Aggregated into {} consolidated records", consolidatedList.size());

        // 3ï¸âƒ£ Preâ€‘fetch sales in one go
        List<String> itemDescs = consolidatedList.stream().map(StockSummaryEntity::getItemDescription).toList();
        LocalDate startDate = LocalDate.now().withDayOfMonth(1).minusMonths(12);
        List<Object[]> raw = soSummaryRepository.getMonthlySaleForItems(unit, itemDescs, startDate);
        log.info("Fetched {} monthly sale records for last 12 months", raw.size());

        Map<String, Map<String, BigDecimal>> saleMap = new HashMap<>();
        for (Object[] row : raw) {
            saleMap.computeIfAbsent((String) row[0], k -> new HashMap<>())
                    .put((String) row[1], (BigDecimal) row[2]);
        }

        // 4ï¸âƒ£ Preâ€‘fetch all alerts in one go
        Set<String> existingAlerts = lowStockAlertRepository
                .findAllByUnitAndItemDescriptions(unit, itemDescs)
                .stream()
                .map(a -> a.getUnit() + "|" + a.getMaterialType() + "|" + a.getBrand() + "|" + a.getItemDescription())
                .collect(Collectors.toSet());
        log.info("Fetched {} existing alerts from LowStockAlert table", existingAlerts.size());

        // 5ï¸âƒ£ Build DTOs
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
            log.debug("Item={} â†’ existsInAlert={}, status={}", itemDesc, existsInAlert, dto.getStatus());

            // Condition check
            if (!existsInAlert && dto.getReorderLevel() != null &&
                    dto.getQuantityKg().compareTo(dto.getReorderLevel()) < 0) {
                log.info("Low stock condition met for item={} â†’ currentStock={} < reorderLevel={}",
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

        log.info("=== Completed getFilteredSummary â†’ returning {} DTOs ===", result.size());
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

        // 4. Safety stock = daily Ã— 15
        BigDecimal safetyStock = dailyConsumption
                .multiply(BigDecimal.valueOf(15))
                .setScale(4, RoundingMode.DOWN);
        dto.setSafetyStock(safetyStock);

        // 5. Reorder quantity = daily Ã— MOQ
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

        return list; // already newest â†’ oldest
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
                "Kg", // âœ… UOM
                entity.getSectionNo() // âœ… New field added
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

                    log.info("ðŸ” Selected sample for item group: id={}, itemGroup={}, hasGrnNumbers={}",
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
                                        .grnNumbers(grnNumbersObj) // âœ… Add GRN numbers
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
                                log.info("ðŸ“‹ GRN Numbers from DB (JSON array): {}", grnNumbersObj);
                            } else {
                                // It's a plain string - wrap it in a list
                                java.util.List<String> grnList = new java.util.ArrayList<>();
                                grnList.add(grnStr);
                                grnNumbersObj = grnList;
                                log.info("ðŸ“‹ GRN Numbers from DB (plain string): {}", grnNumbersObj);
                            }
                        } catch (Exception e) {
                            log.error("Error processing GRN numbers: {} - Value: {}", e.getMessage(),
                                    sample.getGrnNumbers());
                            grnNumbersObj = new java.util.ArrayList<>();
                        }
                    } else {
                        log.warn("âš ï¸ GRN Numbers is empty or null for item: {}", sample.getItemDescription());
                    }

                    return StockSummaryFormattedDTO.builder()
                            .itemDescription(entry.getKey().itemDescription())
                            .unit(entry.getKey().unit())
                            .store(sample.getStore())
                            .storageArea(sample.getStorageArea())
                            .productCategory(sample.getProductCategory())
                            .brand(sample.getBrand())
                            .grade(sample.getGrade())
                            .materialType(formatMaterialType(resolveMaterialType(sample.getMaterialType(), sample.getItemDescription())))
                            .itemGroup(normalizeItemGroup(itemGroupValue))
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
                "Calculated metrics â†’ totalConsumption={}, monthlyAvg={}, dailyConsumption={}, safetyStock={}, reorderLevel={}",
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

        return list; // newest â†’ oldest
    }

    @Override
    public List<Map<String, Object>> searchByUnitAndItemDescription(String unit, String itemDescription) {
        log.info("ðŸ” Searching Stock Summary by Unit and ItemDescription...");
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
                            log.warn("âš ï¸ Error parsing GRN numbers: {}", e.getMessage());
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

        log.info("   âœ… Found {} location(s)", result.size());
        for (Map<String, Object> item : result) {
            log.info("      ðŸ“ Store: {}, Area: {}, Rack: {}, GRNs: {}",
                    item.get("store"), item.get("storageArea"),
                    item.get("rackColumnShelfNumber"), item.get("grnNumbers"));
        }

        return result;
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> saveReturnStock(ReturnStockDTO dto) {
        System.out.println("\nâ•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—");
        System.out.println("â•‘            ðŸ“¦ RETURN STOCK SAVE - NEW IMPLEMENTATION                        â•‘");
        System.out.println("â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        System.out.println("â”Œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”");
        System.out.println("â”‚ INPUT PARAMETERS                                                            â”‚");
        System.out.println("â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤");
        System.out.println("â”‚ Unit           : " + dto.getUnit());
        System.out.println("â”‚ Item Desc      : " + dto.getItemDescription());
        System.out
                .println("â”‚ Return Entries : " + (dto.getReturnEntries() != null ? dto.getReturnEntries().size() : 0));
        System.out.println("â”‚ Item Group     : RAW MATERIAL (fixed)");
        System.out.println("â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜");

        List<StockSummaryEntity> savedEntities = new java.util.ArrayList<>();
        List<StockSummaryBundleEntity> savedBundles = new java.util.ArrayList<>();
        List<String> errors = new java.util.ArrayList<>();

        String defaultStore = "Loose Piece Storage";
        String targetItemGroup = "RAW MATERIAL";

        if (dto.getReturnEntries() == null || dto.getReturnEntries().isEmpty()) {
            System.out.println("\nâš ï¸ No return entries provided - ABORTING");
            return java.util.Map.of(
                    "success", false,
                    "message", "No return entries provided",
                    "savedCount", 0);
        }

        for (int i = 0; i < dto.getReturnEntries().size(); i++) {
            ReturnStockDTO.ReturnEntryDTO entry = dto.getReturnEntries().get(i);

            System.out.println("\nâ”Œâ”€ ENTRY [" + (i + 1) + "/" + dto.getReturnEntries().size()
                    + "] â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”");
            System.out.println("â”‚ ðŸ“‹ ENTRY DETAILS:");
            System.out.println("â”‚    Batch Number (GRN): " + entry.getBatchNumber());
            System.out.println("â”‚    Date of Inward    : " + entry.getDateOfInward());
            System.out.println("â”‚    Return Qty        : " + entry.getReturnQuantityKg() + " KG | "
                    + entry.getReturnQuantityNo() + " NO");
            System.out.println("â”‚    Return Store      : " + entry.getReturnStore());
            System.out.println("â”‚    Storage Area      : " + entry.getStorageArea());
            System.out.println("â”‚    Rack/Bin          : " + entry.getRackColumnBin());
            System.out.println("â”‚    Dimension         : " + entry.getDimension());
            System.out.println("â”œâ”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”¤");

            try {
                String storageArea = entry.getStorageArea() != null ? entry.getStorageArea() : "Common";
                String rackColumnBin = entry.getRackColumnBin() != null ? entry.getRackColumnBin() : "Common";
                String dimension = entry.getDimension();
                String grnNumber = entry.getBatchNumber();

                // Use the actual return store from the entry (set by allocateReturnRack)
                String entryStore = entry.getReturnStore() != null && !entry.getReturnStore().isEmpty()
                        ? entry.getReturnStore() : defaultStore;
                System.out.println("â”‚    Resolved Store    : " + entryStore);

                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // PRE-ENRICHMENT: Lookup GRN Item for product details
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                String preProductCategory = null;
                String preBrand = null;
                String preGrade = null;
                String preTemper = null;
                String preMaterialType = "ALUMINIUM - AL";

                if (grnNumber != null && !grnNumber.isEmpty()) {
                    Optional<GRNEntity> preGrnOpt = grnRepository.findByGrnRefNumber(grnNumber);
                    if (preGrnOpt.isPresent()) {
                        GRNEntity preGrn = preGrnOpt.get();
                        List<GRNItemEntity> preGrnItems = preGrn.getGrnItems();
                        if (preGrnItems != null && !preGrnItems.isEmpty()) {
                            GRNItemEntity preItem = preGrnItems.stream()
                                    .filter(item -> dto.getItemDescription() != null &&
                                            dto.getItemDescription().equalsIgnoreCase(item.getItemDescription()))
                                    .findFirst()
                                    .orElse(preGrnItems.get(0));
                            preProductCategory = preItem.getProductCategory();
                            preBrand = preItem.getBrand();
                            preGrade = preItem.getGrade();
                            preTemper = preItem.getTemper();
                            System.out.println("â”‚    Pre-enriched: category=" + preProductCategory + " brand=" + preBrand);
                        }
                    }
                }

                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // STEP 1: CHECK IF STOCK SUMMARY ENTRY EXISTS
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                System.out.println("â”‚ ðŸ” STEP 1: Checking for existing Stock Summary entry...");
                System.out.println("â”‚    Search: unit='" + dto.getUnit() + "' store='" + entryStore
                        + "' area='" + storageArea + "' rack='" + rackColumnBin + "'");

                Optional<StockSummaryEntity> existingOpt = repository
                        .findByUnitAndItemDescriptionAndItemGroupAndStoreAndStorageAreaAndRackColumnShelfNumberAndDimension(
                                dto.getUnit(),
                                dto.getItemDescription(),
                                targetItemGroup,
                                entryStore,
                                storageArea,
                                rackColumnBin,
                                dimension);

                StockSummaryEntity stockSummary;

                if (existingOpt.isPresent()) {
                    // EXISTING ENTRY FOUND - UPDATE QUANTITY
                    stockSummary = existingOpt.get();
                    System.out.println("â”‚ âœ… FOUND EXISTING ENTRY - Stock ID: " + stockSummary.getId());

                    BigDecimal existingQtyKg = stockSummary.getQuantityKg() != null ? stockSummary.getQuantityKg()
                            : BigDecimal.ZERO;
                    Integer existingQtyNo = stockSummary.getQuantityNo() != null ? stockSummary.getQuantityNo() : 0;

                    BigDecimal newQtyKg = existingQtyKg
                            .add(entry.getReturnQuantityKg() != null ? entry.getReturnQuantityKg() : BigDecimal.ZERO);
                    Integer newQtyNo = existingQtyNo
                            + (entry.getReturnQuantityNo() != null ? entry.getReturnQuantityNo() : 0);

                    System.out.println("â”‚    Qty: " + existingQtyKg + " â†’ " + newQtyKg + " KG");

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
                    }

                    if (entry.getQrCode() != null && !entry.getQrCode().isEmpty()) {
                        stockSummary.setQrCode(entry.getQrCode());
                    }

                    // Enrich with product details if missing
                    if (stockSummary.getProductCategory() == null && preProductCategory != null)
                        stockSummary.setProductCategory(preProductCategory);
                    if (stockSummary.getBrand() == null && preBrand != null)
                        stockSummary.setBrand(preBrand);
                    if (stockSummary.getGrade() == null && preGrade != null)
                        stockSummary.setGrade(preGrade);
                    if (stockSummary.getTemper() == null && preTemper != null)
                        stockSummary.setTemper(preTemper);
                    if (stockSummary.getMaterialType() == null && preMaterialType != null)
                        stockSummary.setMaterialType(preMaterialType);

                    stockSummary = repository.save(stockSummary);
                    System.out.println("â”‚ ðŸ’¾ STOCK SUMMARY UPDATED - ID: " + stockSummary.getId());

                } else {
                    // NO EXISTING ENTRY - CREATE NEW
                    System.out.println("â”‚ ðŸ†• NO EXISTING ENTRY - CREATING NEW STOCK SUMMARY");

                    stockSummary = StockSummaryEntity.builder()
                            .unit(dto.getUnit())
                            .store(entryStore)
                            .itemGroup(targetItemGroup)
                            .storageArea(storageArea)
                            .rackColumnShelfNumber(rackColumnBin)
                            .itemDescription(dto.getItemDescription())
                            .dimension(dimension)
                            .quantityKg(entry.getReturnQuantityKg())
                            .quantityNo(entry.getReturnQuantityNo())
                            .grnNumbers(grnNumber != null ? "[\"" + grnNumber + "\"]" : null)
                            .qrCode(entry.getQrCode())
                            .productCategory(preProductCategory)
                            .brand(preBrand)
                            .grade(preGrade)
                            .temper(preTemper)
                            .materialType(preMaterialType)
                            .pickListLocked(false)
                            .reprintQr(false)
                            .build();

                    stockSummary = repository.save(stockSummary);
                    System.out.println("â”‚    âœ… NEW STOCK SUMMARY CREATED - ID: " + stockSummary.getId());
                }

                savedEntities.add(stockSummary);

                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // STEP 2: SAVE BUNDLE TO StockSummaryBundleEntity (with GRN Item enrichment)
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                System.out.println("â”‚");
                System.out.println("â”‚ ðŸ“¦ STEP 2: Saving Bundle to StockSummaryBundleEntity...");

                // Lookup GRN entity for enrichment
                GRNEntity grnEntity = null;
                if (grnNumber != null && !grnNumber.isEmpty()) {
                    Optional<GRNEntity> grnOpt = grnRepository.findByGrnRefNumber(grnNumber);
                    if (grnOpt.isPresent()) {
                        grnEntity = grnOpt.get();
                        System.out.println("â”‚    Found GRN: " + grnEntity.getGrnRefNumber());
                    }
                }

                // Enrichment fields from GRN Item
                String enrichedHeatNo = null;
                String enrichedLotNo = null;
                String enrichedTestCertificate = null;
                BigDecimal enrichedItemPrice = null;
                String enrichedProductCategory = preProductCategory;
                String enrichedBrand = preBrand;
                String enrichedGrade = preGrade;
                String enrichedTemper = preTemper;

                if (grnEntity != null) {
                    List<GRNItemEntity> grnItems = grnEntity.getGrnItems();
                    if (grnItems != null && !grnItems.isEmpty()) {
                        GRNItemEntity matchedItem = grnItems.stream()
                                .filter(item -> dto.getItemDescription() != null &&
                                        dto.getItemDescription().equalsIgnoreCase(item.getItemDescription()))
                                .findFirst()
                                .orElse(grnItems.get(0));

                        enrichedHeatNo = matchedItem.getHeatNumber();
                        enrichedLotNo = matchedItem.getLotNumber();
                        enrichedTestCertificate = matchedItem.getTestCertificateNumber();
                        enrichedItemPrice = matchedItem.getRate() != null ? BigDecimal.valueOf(matchedItem.getRate()) : null;
                        enrichedProductCategory = matchedItem.getProductCategory();
                        enrichedBrand = matchedItem.getBrand();
                        enrichedGrade = matchedItem.getGrade();
                        enrichedTemper = matchedItem.getTemper();

                        System.out.println("â”‚    Enriched from GRN Item: heat=" + enrichedHeatNo
                                + " lot=" + enrichedLotNo + " price=" + enrichedItemPrice);
                    }
                }

                StockSummaryBundleEntity bundle = StockSummaryBundleEntity.builder()
                        .stockSummary(stockSummary)
                        .grnNumber(grnNumber)
                        .grnId(grnEntity != null ? grnEntity.getId() : null)
                        .itemDescription(dto.getItemDescription())
                        .productCategory(enrichedProductCategory)
                        .brand(enrichedBrand)
                        .grade(enrichedGrade)
                        .temper(enrichedTemper)
                        .dimension(dimension)
                        .weightmentQuantityKg(entry.getReturnQuantityKg())
                        .weightmentQuantityNo(entry.getReturnQuantityNo())
                        .currentStore(entryStore)
                        .storageArea(storageArea)
                        .rackColumnBinNumber(rackColumnBin)
                        .poNumber(grnEntity != null ? grnEntity.getPoNumber() : null)
                        .heatNo(enrichedHeatNo)
                        .lotNo(enrichedLotNo)
                        .testCertificate(enrichedTestCertificate)
                        .itemPrice(enrichedItemPrice)
                        .qrCodeUrl(entry.getQrCode())
                        .status("RETURNED")
                        .transferType("RETURN_STOCK")
                        .build();

                StockSummaryBundleEntity savedBundle = stockSummaryBundleRepository.save(bundle);
                savedBundles.add(savedBundle);

                System.out.println("â”‚    âœ… BUNDLE SAVED - ID: " + savedBundle.getId());
                System.out.println("â”‚       - Linked to Stock Summary ID: " + stockSummary.getId());
                System.out.println("â”‚       - Return Qty: " + entry.getReturnQuantityKg() + " KG | "
                        + entry.getReturnQuantityNo() + " NO");

                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // STEP 3: UPDATE RACK & BIN MASTER currentStorage
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                System.out.println("â”‚");
                System.out.println("â”‚ ðŸ—ï¸ STEP 3: Updating Rack & Bin Master currentStorage...");

                if (rackColumnBin == null || rackColumnBin.isBlank() || "Common".equalsIgnoreCase(rackColumnBin)) {
                    System.out.println("â”‚    â­ï¸ SKIP â€” rackColumnBin is null or 'Common'");
                } else {
                    try {
                        String[] rackParts = rackColumnBin.split("-");
                        if (rackParts.length >= 3) {
                            String rackNo = rackParts[0];
                            String columnNo = rackParts[1];
                            String binNo = rackParts[2];

                            System.out.println("â”‚    Searching: store='" + entryStore + "' area='" + storageArea +
                                    "' rack='" + rackNo + "' col='" + columnNo + "' bin='" + binNo + "' unit='" + dto.getUnit() + "'");

                            Optional<RackBinMasterEntity> rackOpt = rackBinMasterRepository
                                    .findByStorageTypeAndStorageAreaAndRackNoAndColumnNoAndBinNoAndUnitName(
                                            entryStore, storageArea, rackNo, columnNo, binNo, dto.getUnit());

                            if (rackOpt.isPresent()) {
                                RackBinMasterEntity rack = rackOpt.get();
                                double existingStorage = rack.getCurrentStorage() != null ? rack.getCurrentStorage() : 0;
                                double returnQty = entry.getReturnQuantityKg() != null ? entry.getReturnQuantityKg().doubleValue() : 0;
                                double newStorage = existingStorage + returnQty;
                                rack.setCurrentStorage(newStorage);
                                rackBinMasterRepository.save(rack);
                                System.out.println("â”‚    âœ… Rack Storage: " + existingStorage + " â†’ " + newStorage + " KG");
                            } else {
                                System.out.println("â”‚    âš ï¸ Rack not found â€” skipping storage update");
                            }
                        } else {
                            System.out.println("â”‚    âš ï¸ Cannot parse rack: '" + rackColumnBin + "' (expected R-C-B format)");
                        }
                    } catch (Exception rackEx) {
                        System.out.println("â”‚    âš ï¸ Error updating rack storage: " + rackEx.getMessage());
                    }
                }

                System.out.println("â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜");

            } catch (Exception e) {
                String error = "Error processing entry " + (i + 1) + ": " + e.getMessage();
                System.out.println("â”‚ âŒ ERROR: " + error);
                System.out.println("â””â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”˜");
                e.printStackTrace();
                errors.add(error);
            }
        }

        System.out.println("\nâ•”â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•—");
        System.out.println("â•‘            ðŸ“Š RETURN STOCK SAVE - SUMMARY                                   â•‘");
        System.out.println("â• â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•£");
        System.out.println("â•‘ Entries Processed     : " + dto.getReturnEntries().size());
        System.out.println("â•‘ Stock Summaries Saved : " + savedEntities.size());
        System.out.println("â•‘ Bundles Saved         : " + savedBundles.size());
        System.out.println("â•‘ Errors                : " + errors.size());
        System.out.println("â•‘ Status                : " + (errors.isEmpty() ? "âœ… SUCCESS" : "âš ï¸ PARTIAL SUCCESS"));
        System.out.println("â•šâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");

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

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // END OF saveReturnStock METHOD
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•


    @Override
    public AllocateReturnRackDTO.SuggestedRackDTO allocateReturnRack(AllocateReturnRackDTO dto) {
        log.info("\nâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        log.info("ðŸ—ï¸ [AllocateReturnRack] ALLOCATING RACK FOR RETURN STOCK");
        log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        log.info("   Unit: {}", dto.getUnit());
        log.info("   Item Description: {}", dto.getItemDescription());
        log.info("   Product Category: {}", dto.getProductCategory());
        log.info("   Return Quantity (Kg): {}", dto.getReturnQuantityKg());

        String targetStore = "LOOSE PIECE STORAGE"; // Always Loose Piece

        try {
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            // STEP 1: Filter by Store â€” find all racks where storageType matches
            //         "Loose Piece Storage" (case-insensitive, contains-based)
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            log.info("\n   ðŸ“ STEP 1: Finding racks by Store (Loose Piece Storage)...");

            // Get ALL racks for this unit, then filter by store containing "LOOSE"
            List<RackBinMasterEntity> allRacks = rackBinMasterRepository.findAll();
            log.info("      Total racks in database: {}", allRacks.size());

            // Filter by unit name AND loose piece store
            List<RackBinMasterEntity> storeFilteredRacks = allRacks.stream()
                    .filter(rack -> {
                        // Unit match
                        String rackUnit = rack.getUnitName() != null ? rack.getUnitName().trim() : "";
                        boolean unitMatch = dto.getUnit() != null && dto.getUnit().trim().equalsIgnoreCase(rackUnit);
                        if (!unitMatch) return false;

                        // Store match: storageType contains "LOOSE"
                        String storageType = rack.getStorageType() != null ? rack.getStorageType().toUpperCase() : "";
                        return storageType.contains("LOOSE");
                    })
                    .toList();

            log.info("      Found {} racks for store 'Loose Piece Storage' and unit '{}'", storeFilteredRacks.size(), dto.getUnit());

            if (storeFilteredRacks.isEmpty()) {
                log.warn("      âš ï¸ No loose piece racks found for unit â€” returning store without rack details");
                return AllocateReturnRackDTO.SuggestedRackDTO.builder()
                        .store(targetStore)
                        .storageArea("")
                        .rackColumnBin("")
                        .availableCapacity(0.0)
                        .distance(0.0)
                        .storageAreaOrder(0)
                        .itemCategory(dto.getProductCategory())
                        .isAllocated(true)
                        .build();
            }

            // Log all found racks for debugging
            for (RackBinMasterEntity rack : storeFilteredRacks) {
                log.info("      â†’ Rack: {}/{}/{} | Category: {} | Capacity: {} | Current: {} | Area: {}",
                        rack.getRackNo(), rack.getColumnNo(), rack.getBinNo(),
                        rack.getItemCategory(), rack.getBinCapacity(),
                        rack.getCurrentStorage(), rack.getStorageArea());
            }

            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            // STEP 2: Filter by Category + Fallback to "ALL"
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            log.info("\n   ðŸ“ STEP 2: Filtering by Product Category: '{}'...", dto.getProductCategory());

            String requestedCategory = dto.getProductCategory() != null ? dto.getProductCategory().trim() : "";
            boolean usedFallback = false;

            List<RackBinMasterEntity> categoryFilteredRacks = storeFilteredRacks.stream()
                    .filter(rack -> {
                        String rackCategory = rack.getItemCategory() != null ? rack.getItemCategory().trim() : "";
                        return rackCategory.equalsIgnoreCase(requestedCategory);
                    })
                    .toList();

            log.info("      Found {} racks matching category '{}'", categoryFilteredRacks.size(), requestedCategory);

            // Fallback to "ALL" category
            if (categoryFilteredRacks.isEmpty()) {
                log.info("      âš ï¸ No racks for category '{}', falling back to 'ALL'...", requestedCategory);
                categoryFilteredRacks = storeFilteredRacks.stream()
                        .filter(rack -> {
                            String rackCategory = rack.getItemCategory() != null ? rack.getItemCategory().trim() : "";
                            return rackCategory.equalsIgnoreCase("ALL");
                        })
                        .toList();
                usedFallback = true;
                log.info("      Found {} racks with category 'ALL'", categoryFilteredRacks.size());
            }

            // If still empty, use ALL loose piece racks regardless of category
            if (categoryFilteredRacks.isEmpty()) {
                log.info("      âš ï¸ No 'ALL' category racks either â€” using ALL loose piece racks");
                categoryFilteredRacks = storeFilteredRacks;
                usedFallback = true;
            }

            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            // STEP 3: Filter by Capacity (availableSpace >= returnQuantityKg)
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            log.info("\n   ðŸ“ STEP 3: Filtering by available capacity...");
            log.info("      Required Capacity: {} KG", dto.getReturnQuantityKg());

            double requiredQty = dto.getReturnQuantityKg().doubleValue();

            List<RackBinMasterEntity> capacityFilteredRacks = categoryFilteredRacks.stream()
                    .filter(rack -> {
                        double binCapacity = 0;
                        try {
                            binCapacity = Double.parseDouble(rack.getBinCapacity() != null ? rack.getBinCapacity() : "0");
                        } catch (NumberFormatException e) {
                            log.warn("      âš ï¸ Invalid bin capacity: {}", rack.getBinCapacity());
                        }
                        double currentStorage = rack.getCurrentStorage() != null ? rack.getCurrentStorage() : 0;
                        double availableSpace = binCapacity - currentStorage;
                        boolean hasCapacity = availableSpace >= requiredQty;

                        log.info("      {} Rack: {}/{}/{} â€” Capacity: {} KG, Current: {} KG, Available: {} KG",
                                hasCapacity ? "âœ…" : "âŒ",
                                rack.getRackNo(), rack.getColumnNo(), rack.getBinNo(),
                                binCapacity, currentStorage, availableSpace);

                        return hasCapacity;
                    })
                    .toList();

            log.info("      Found {} racks with sufficient capacity", capacityFilteredRacks.size());

            // If no capacity after category filter, backtrack to ALL categories
            if (capacityFilteredRacks.isEmpty() && !usedFallback) {
                log.info("      âš ï¸ No capacity for category '{}', backtracking to ALL loose piece racks...", requestedCategory);
                capacityFilteredRacks = storeFilteredRacks.stream()
                        .filter(rack -> {
                            double binCapacity = 0;
                            try {
                                binCapacity = Double.parseDouble(rack.getBinCapacity() != null ? rack.getBinCapacity() : "0");
                            } catch (NumberFormatException e) { /* ignore */ }
                            double currentStorage = rack.getCurrentStorage() != null ? rack.getCurrentStorage() : 0;
                            return (binCapacity - currentStorage) >= requiredQty;
                        })
                        .toList();
                log.info("      Found {} racks after backtrack", capacityFilteredRacks.size());
            }

            if (capacityFilteredRacks.isEmpty()) {
                log.warn("      âš ï¸ No racks with sufficient capacity â€” returning store without rack details");
                return AllocateReturnRackDTO.SuggestedRackDTO.builder()
                        .store(targetStore)
                        .storageArea("")
                        .rackColumnBin("")
                        .availableCapacity(0.0)
                        .distance(0.0)
                        .storageAreaOrder(0)
                        .itemCategory(dto.getProductCategory())
                        .isAllocated(true)
                        .build();
            }

            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            // STEP 4: Min storage_area_order (keep only bins with LOWEST order)
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            log.info("\n   ðŸ“ STEP 4: Selecting bins with minimum storage_area_order...");

            Integer minOrder = capacityFilteredRacks.stream()
                    .map(r -> r.getStorageAreaOrder() != null ? r.getStorageAreaOrder() : Integer.MAX_VALUE)
                    .min(Integer::compareTo)
                    .orElse(Integer.MAX_VALUE);

            log.info("      Minimum storage_area_order: {}", minOrder == Integer.MAX_VALUE ? "N/A" : minOrder);

            List<RackBinMasterEntity> minOrderRacks = capacityFilteredRacks.stream()
                    .filter(r -> {
                        Integer order = r.getStorageAreaOrder() != null ? r.getStorageAreaOrder() : Integer.MAX_VALUE;
                        return order.equals(minOrder);
                    })
                    .toList();

            log.info("      Found {} racks with minimum order", minOrderRacks.size());

            // If only 1, return it
            if (minOrderRacks.size() == 1) {
                RackBinMasterEntity bestRack = minOrderRacks.get(0);
                log.info("      âœ… SINGLE BEST RACK â€” returning immediately");
                return buildSuggestedRack(bestRack, targetStore);
            }

            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            // STEP 5: Min Distance (select bin with SMALLEST distance)
            // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
            log.info("\n   ðŸ“ STEP 5: Selecting bin with minimum distance...");

            RackBinMasterEntity bestRack = minOrderRacks.stream()
                    .min(Comparator.comparing(r -> r.getDistance() != null ? r.getDistance() : Double.MAX_VALUE))
                    .orElse(null);

            if (bestRack == null) {
                log.warn("      âš ï¸ No suitable rack found after sorting");
                return null;
            }

            AllocateReturnRackDTO.SuggestedRackDTO suggested = buildSuggestedRack(bestRack, targetStore);

            log.info("      âœ… ALLOCATED RACK:");
            log.info("         Store: {}", suggested.getStore());
            log.info("         Storage Area: {}", suggested.getStorageArea());
            log.info("         Rack/Bin: {}", suggested.getRackColumnBin());
            log.info("         Available Capacity: {} KG", suggested.getAvailableCapacity());
            log.info("         Distance: {}", suggested.getDistance());
            log.info("         Storage Area Order: {}", suggested.getStorageAreaOrder());

            log.info("\nâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
            log.info("âœ… [AllocateReturnRack] COMPLETE");
            log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");

            return suggested;

        } catch (Exception e) {
            log.error("   âŒ Error allocating rack: {}", e.getMessage(), e);
            return null;
        }
    }

    private AllocateReturnRackDTO.SuggestedRackDTO buildSuggestedRack(RackBinMasterEntity rack, String store) {
        String rackColumnBin = rack.getRackNo() + "-" + rack.getColumnNo() + "-" + rack.getBinNo();

        double binCapacity = 0;
        try {
            binCapacity = Double.parseDouble(rack.getBinCapacity() != null ? rack.getBinCapacity() : "0");
        } catch (NumberFormatException e) { /* ignore */ }
        double currentStorage = rack.getCurrentStorage() != null ? rack.getCurrentStorage() : 0;
        double availableCapacity = binCapacity - currentStorage;

        return AllocateReturnRackDTO.SuggestedRackDTO.builder()
                .store(rack.getStorageType() != null ? rack.getStorageType() : store)
                .storageArea(rack.getStorageArea())
                .rackColumnBin(rackColumnBin)
                .availableCapacity(availableCapacity)
                .distance(rack.getDistance())
                .storageAreaOrder(rack.getStorageAreaOrder())
                .itemCategory(rack.getItemCategory())
                .isAllocated(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockSummaryWithBundlesDTO> getAllWithBundles() {
        log.info("\nâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        log.info("ðŸ“¦ [StockSummary] GET ALL WITH BUNDLES - Fetching all stock summaries with GRN data");
        log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");

        try {
            List<StockSummaryEntity> allStockSummaries = repository.findAll();
            log.info("   ðŸ“Š Total stock summaries found: {}", allStockSummaries.size());

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

            log.info("   âœ… Successfully mapped {} stock summaries with bundles", result.size());
            log.info("\nâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
            log.info("âœ… [StockSummary] GET ALL WITH BUNDLES - COMPLETE");
            log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");

            return result;

        } catch (Exception e) {
            log.error("   âŒ Error fetching stock summaries with bundles: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch stock summaries with bundles: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getMergedStockWithGrnDetails(String unit, String itemDescription, String itemGroup,
            String dimension) {
        log.info("\nâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        log.info("ðŸ“¦ [StockSummary] GET MERGED STOCK WITH GRN DETAILS");
        log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
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

            log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
            log.info("âœ… [StockSummary] GET MERGED STOCK WITH GRN DETAILS - COMPLETE");
            log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");

            return response;

        } catch (Exception e) {
            log.error("   âŒ Error in getMergedStockWithGrnDetails: {}", e.getMessage(), e);
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
            log.warn("      âš ï¸ Error parsing GRN numbers: {}", e.getMessage());
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
        log.info("\nâ•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
        log.info("ðŸ“¦ [StockSummary] GET ALL MERGED WITH GRN DETAILS");
        log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");

        List<Map<String, Object>> resultList = new ArrayList<>();

        try {
            // Step 1: Get all stock summaries
            List<StockSummaryEntity> allStocks = repository.findAll();
            log.info("   Total stock entries (before approval filter): {}", allStocks.size());

            // Step 1.5: Filter out stocks whose Item Master is NOT approved
            //   Only show items in stock summary if the Item Master record has status = "APPROVED"
            allStocks = allStocks.stream().filter(stock -> {
                String itemDesc = stock.getItemDescription();
                if (itemDesc == null || itemDesc.isBlank()) return true; // Keep if no item desc
                Optional<ItemMasterEntity> itemMasterOpt = itemMasterRepository
                        .findBySkuDescriptionIgnoreCase(itemDesc.trim());
                if (itemMasterOpt.isPresent()) {
                    String status = itemMasterOpt.get().getStatus();
                    return "APPROVED".equalsIgnoreCase(status);
                }
                return true; // Keep if no matching Item Master found (legacy data)
            }).collect(Collectors.toList());
            log.info("   Total stock entries (after approval filter): {}", allStocks.size());

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

                    // âœ… Rack price calculation (sum of itemValue and sum of kg for weighted
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

                            // âœ… NULL â†’ ZERO handling
                            BigDecimal price = bundle.getItemPrice() != null
                                    ? bundle.getItemPrice()
                                    : BigDecimal.ZERO;

                            BigDecimal bundleKg = bundle.getWeightmentQuantityKg() != null
                                    ? bundle.getWeightmentQuantityKg()
                                    : BigDecimal.ZERO;

                            // âœ… itemValue = itemPrice * weightmentQuantityKg
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
                            // Include parent stock entity's unit for QR popup display
                            bundleDetail.put("unit", stock.getUnit());

                            bundleDetailsList.add(bundleDetail);
                        }
                    } else {
                        rackQuantityKg = stock.getQuantityKg() != null ? stock.getQuantityKg() : BigDecimal.ZERO;
                        rackQuantityNo = stock.getQuantityNo() != null ? stock.getQuantityNo() : 0;
                    }

                    // âœ… Rack average = sum of itemValue / sum of weightmentQuantityKg
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

                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // FIX #4: Filter out zero-quantity rack rows
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                rackWiseList = rackWiseList.stream()
                    .filter(rack -> {
                        BigDecimal qtyKg = (BigDecimal) rack.get("quantityKg");
                        Object qtyNoObj = rack.get("quantityNo");
                        int qtyNo = (qtyNoObj instanceof Integer) ? (Integer) qtyNoObj : 0;
                        boolean hasKg = qtyKg != null && qtyKg.compareTo(BigDecimal.ZERO) > 0;
                        boolean hasNo = qtyNo > 0;
                        return hasKg || hasNo;
                    })
                    .collect(Collectors.toList());

                // Skip entire group if all racks are zero-quantity
                if (rackWiseList.isEmpty()) continue;

                // âœ… MAIN LEVEL CALCULATION
                BigDecimal totalWeightedPrice = BigDecimal.ZERO;
                BigDecimal totalRackKg = BigDecimal.ZERO;

                BigDecimal totalQuantityKg = BigDecimal.ZERO;
                int totalQuantityNo = 0;

                for (Map<String, Object> rack : rackWiseList) {

                    BigDecimal rackAvg = (BigDecimal) rack.get("averageItemPrice");
                    BigDecimal rackQtyKg = (BigDecimal) rack.get("quantityKg");

                    // âœ… Weighted price: averageItemPrice * quantityKg for each rack
                    if (rackAvg != null && rackQtyKg != null && rackAvg.compareTo(BigDecimal.ZERO) > 0
                            && rackQtyKg.compareTo(BigDecimal.ZERO) > 0) {
                        totalWeightedPrice = totalWeightedPrice.add(rackAvg.multiply(rackQtyKg));
                        totalRackKg = totalRackKg.add(rackQtyKg);
                    }

                    // Use rack-level quantities (which already fallback to stock entity values
                    // when no bundles exist, e.g. opening stock entries)
                    if (rackQtyKg != null) {
                        totalQuantityKg = totalQuantityKg.add(rackQtyKg);
                    }
                    Object rackQtyNoObj = rack.get("quantityNo");
                    if (rackQtyNoObj instanceof Integer) {
                        totalQuantityNo += (Integer) rackQtyNoObj;
                    }
                }

                // âœ… Main averageItemPrice = sum(averageItemPrice * quantityKg) /
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
                mergedEntry.put("materialType", formatMaterialType(materialType));
                mergedEntry.put("itemGroup", normalizeItemGroup(sample.getItemGroup()));
                mergedEntry.put("rackWise", rackWiseList);

                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                // BLOCKED QTY + SO ALLOTTED QTY + NET AVAILABLE QTY
                // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
                String itemDesc = sample.getItemDescription();
                BigDecimal blockedQuantityKg = BigDecimal.ZERO;
                BigDecimal soAllottedQuantityKg = BigDecimal.ZERO;

                // 1) Blocked Quantity â€” from blocked_product joined via blocked_quantity
                //    Only include records whose parent Quotation is NOT cancelled/expired/rejected
                try {
                    List<BlockedQuantityEntity> blockedEntities =
                            blockedQuantityRepository.findByItemDescription(itemDesc);
                    for (BlockedQuantityEntity bq : blockedEntities) {
                        // Check parent quotation status
                        String quotationNo = bq.getQuotationNo();
                        boolean isActive = true;
                        if (quotationNo != null) {
                            Optional<ItemEnquiry> enquiryOpt = itemEnquiryRepository.findByQuotationNo(quotationNo);
                            if (enquiryOpt.isPresent()) {
                                String qStatus = enquiryOpt.get().getStatus();
                                if (qStatus != null) {
                                    String upper = qStatus.toUpperCase();
                                    if (upper.equals("CANCELLED") || upper.equals("EXPIRED") || upper.equals("REJECTED")) {
                                        isActive = false;
                                    }
                                }
                            }
                        }
                        if (isActive && bq.getProducts() != null) {
                            for (BlockedProductEntity bp : bq.getProducts()) {
                                if (bp.getItemDescription() != null
                                        && bp.getItemDescription().equalsIgnoreCase(itemDesc)
                                        && bp.getAvailableQuantityKg() != null) {
                                    blockedQuantityKg = blockedQuantityKg.add(bp.getAvailableQuantityKg());
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("   âš ï¸ Error computing blocked qty for '{}': {}", itemDesc, ex.getMessage());
                }

                // 2) SO Allotted Quantity â€” from SalesOrderLineItem where parent SO is ACTIVE
                try {
                    List<SalesOrder> activeSOs = salesOrderRepository.findAll().stream()
                            .filter(so -> {
                                String st = so.getStatus();
                                return st != null && st.equalsIgnoreCase("ACTIVE");
                            })
                            .toList();
                    for (SalesOrder so : activeSOs) {
                        if (so.getItems() != null) {
                            for (SalesOrderLineItem li : so.getItems()) {
                                if (li.getItemDescription() != null
                                        && li.getItemDescription().equalsIgnoreCase(itemDesc)) {
                                    soAllottedQuantityKg = soAllottedQuantityKg.add(
                                            BigDecimal.valueOf(li.getQuantityKg()));
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("   âš ï¸ Error computing SO allotted qty for '{}': {}", itemDesc, ex.getMessage());
                }

                // 3) Net Available = Total - Blocked - SO Allotted
                BigDecimal netAvailableQuantityKg = totalQuantityKg
                        .subtract(blockedQuantityKg)
                        .subtract(soAllottedQuantityKg);

                mergedEntry.put("blockedQuantityKg", blockedQuantityKg);
                mergedEntry.put("soAllottedQuantityKg", soAllottedQuantityKg);
                mergedEntry.put("netAvailableQuantityKg", netAvailableQuantityKg);

                resultList.add(mergedEntry);
            }

            log.info("   Total merged entries: {}", resultList.size());
            log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•");
            log.info("âœ… [StockSummary] GET ALL MERGED WITH GRN DETAILS - COMPLETE");
            log.info("â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•\n");

            return resultList;

        } catch (Exception e) {
            log.error("   âŒ Error in getAllMergedWithGrnDetails: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch all merged stock with GRN details: " + e.getMessage());
        }
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // BLOCKED DETAILS DRILL-DOWN
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    @Override
    public List<Map<String, Object>> getBlockedDetails(String itemDescription) {
        log.info("ðŸ“¦ [StockSummary] GET BLOCKED DETAILS for: {}", itemDescription);
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<BlockedQuantityEntity> blockedEntities =
                    blockedQuantityRepository.findByItemDescription(itemDescription);

            for (BlockedQuantityEntity bq : blockedEntities) {
                // Check parent quotation status â€” skip cancelled/expired/rejected
                String quotationNo = bq.getQuotationNo();
                String quotationStatus = "UNKNOWN";
                if (quotationNo != null) {
                    Optional<ItemEnquiry> enquiryOpt = itemEnquiryRepository.findByQuotationNo(quotationNo);
                    if (enquiryOpt.isPresent()) {
                        quotationStatus = enquiryOpt.get().getStatus() != null ? enquiryOpt.get().getStatus() : "UNKNOWN";
                        String upper = quotationStatus.toUpperCase();
                        if (upper.equals("CANCELLED") || upper.equals("EXPIRED") || upper.equals("REJECTED")) {
                            continue; // Skip inactive quotations
                        }
                    }
                }

                // Sum blocked qty for this item from this quotation
                BigDecimal totalBlockedKg = BigDecimal.ZERO;
                if (bq.getProducts() != null) {
                    for (BlockedProductEntity bp : bq.getProducts()) {
                        if (bp.getItemDescription() != null
                                && bp.getItemDescription().equalsIgnoreCase(itemDescription)
                                && bp.getAvailableQuantityKg() != null) {
                            totalBlockedKg = totalBlockedKg.add(bp.getAvailableQuantityKg());
                        }
                    }
                }

                if (totalBlockedKg.compareTo(BigDecimal.ZERO) > 0) {
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("quotationNo", bq.getQuotationNo());
                    detail.put("customerName", bq.getCustomerName());
                    detail.put("marketingExecutiveName", bq.getMarketingExecutiveName());
                    detail.put("blockedQuantityKg", totalBlockedKg);
                    detail.put("quotationStatus", quotationStatus);
                    detail.put("createdAt", bq.getCreatedAt());
                    detail.put("pdfLink", bq.getPdfLink());
                    result.add(detail);
                }
            }
            log.info("   âœ… Found {} blocked detail entries", result.size());
        } catch (Exception e) {
            log.error("   âŒ Error fetching blocked details: {}", e.getMessage(), e);
        }
        return result;
    }

    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    // SO ALLOTTED DETAILS DRILL-DOWN
    // â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•â•
    @Override
    public List<Map<String, Object>> getSoAllottedDetails(String itemDescription) {
        log.info("ðŸ“¦ [StockSummary] GET SO ALLOTTED DETAILS for: {}", itemDescription);
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<SalesOrder> allSOs = salesOrderRepository.findAll().stream()
                    .filter(so -> {
                        String st = so.getStatus();
                        return st != null && st.equalsIgnoreCase("ACTIVE");
                    })
                    .toList();

            for (SalesOrder so : allSOs) {
                if (so.getItems() == null) continue;
                for (SalesOrderLineItem li : so.getItems()) {
                    if (li.getItemDescription() != null
                            && li.getItemDescription().equalsIgnoreCase(itemDescription)
                            && li.getQuantityKg() > 0) {
                        Map<String, Object> detail = new LinkedHashMap<>();
                        detail.put("soNumber", so.getSoNumber());
                        detail.put("customerName", so.getCustomerName());
                        detail.put("unit", so.getUnit());
                        detail.put("quantityKg", li.getQuantityKg());
                        detail.put("orderMode", li.getOrderMode());
                        detail.put("status", so.getStatus());
                        detail.put("targetDispatchDate", so.getTargetDispatchDate());
                        detail.put("dimension", li.getDimension());
                        detail.put("productCategory", li.getProductCategory());
                        result.add(detail);
                    }
                }
            }
            log.info("   âœ… Found {} SO allotted detail entries", result.size());
        } catch (Exception e) {
            log.error("   âŒ Error fetching SO allotted details: {}", e.getMessage(), e);
        }
        return result;
    }
}

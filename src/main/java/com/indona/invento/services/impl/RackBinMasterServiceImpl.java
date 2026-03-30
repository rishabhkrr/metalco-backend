package com.indona.invento.services.impl;

import com.indona.invento.dao.RackBinMasterRepository;
import com.indona.invento.dao.StockSummaryRepository;
import com.indona.invento.dto.RackBinMasterDto;
import com.indona.invento.dto.RackBinStorageQtyUpdateDto;
import com.indona.invento.entities.RackBinMasterEntity;
import com.indona.invento.services.RackBinMasterService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
@Slf4j
public class RackBinMasterServiceImpl implements RackBinMasterService {

    @Autowired
    private RackBinMasterRepository repository;

    @Autowired
    private StockSummaryRepository stockSummaryRepository;

    @Override
    public RackBinMasterEntity create(RackBinMasterDto dto) {
        RackBinMasterEntity entity = RackBinMasterEntity.builder()
                .unitCode(dto.getUnitCode())
                .unitName(dto.getUnitName())
                .storageType(dto.getStorageType())
                .storageArea(dto.getStorageArea())
                .rackNo(dto.getRackNo())
                .columnNo(dto.getColumnNo())
                .binNo(dto.getBinNo())
                .binCapacity(dto.getBinCapacity())
                .currentStorage(dto.getCurrentStorage())
                .distance(dto.getDistance())
                .qr(dto.getQr())
                .itemCategory(dto.getItemCategory())
                .storageAreaOrder(dto.getStorageAreaOrder())
                .automated(dto.isAutomated())
                .status(dto.getStatus() != null ? dto.getStatus() : "Pending")
                .build();

        return repository.save(entity);
    }

    @Override
    public RackBinMasterEntity update(Long id, RackBinMasterDto dto) {
        RackBinMasterEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rack-Bin not found"));

        entity.setUnitCode(dto.getUnitCode());
        entity.setUnitName(dto.getUnitName());
        entity.setStorageType(dto.getStorageType());
        entity.setStorageArea(dto.getStorageArea());
        entity.setRackNo(dto.getRackNo());
        entity.setColumnNo(dto.getColumnNo());
        entity.setBinNo(dto.getBinNo());
        entity.setStatus("PENDING");
        entity.setQr(dto.getQr());
        entity.setItemCategory(dto.getItemCategory());

        return repository.save(entity);
    }

    @Override
    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public Page<RackBinMasterEntity> getAll(Pageable pageable) {
        log.info("Fetching all rack-bin entries (paginated)");
        long startTime = System.currentTimeMillis();

        Page<RackBinMasterEntity> page = repository.findAll(pageable);


        log.info("Fetched {} rack-bin entries in {}ms", page.getContent().size(), System.currentTimeMillis() - startTime);
        return page;
    }

    @Override
    public RackBinMasterEntity getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rack-Bin not found"));
    }

    @Override
    public List<RackBinMasterEntity> getAllWithoutPagination() {
        log.info("Fetching all rack-bin entries (without pagination)");
        long startTime = System.currentTimeMillis();

        List<RackBinMasterEntity> entities = repository.findAll();


        log.info("Fetched {} rack-bin entries in {}ms", entities.size(), System.currentTimeMillis() - startTime);
        return entities;
    }

    /**
     * Build a Map of location key -> storage quantity (Single DB query)
     * Key format: store|storageArea|rackColumnShelf
     */
    private Map<String, Double> buildStorageMap() {
        Map<String, Double> storageMap = new HashMap<>();
        try {
            List<Object[]> results = stockSummaryRepository.findAllStorageQuantitiesGrouped();
            for (Object[] row : results) {
                String store = row[0] != null ? row[0].toString() : "";
                String storageArea = row[1] != null ? row[1].toString() : "";
                String rackColumnShelf = row[2] != null ? row[2].toString() : "";
                Double quantity = 0.0;
                if (row[3] != null) {
                    if (row[3] instanceof BigDecimal) {
                        quantity = ((BigDecimal) row[3]).doubleValue();
                    } else if (row[3] instanceof Double) {
                        quantity = (Double) row[3];
                    } else {
                        quantity = Double.parseDouble(row[3].toString());
                    }
                }
                String key = buildStorageKey(store, storageArea, rackColumnShelf);
                storageMap.merge(key, quantity, Double::sum);
            }
            log.debug("Built storage map with {} entries", storageMap.size());
        } catch (Exception e) {
            log.error("Error building storage map: {}", e.getMessage());
        }
        return storageMap;
    }

    /**
     * Set current storage from pre-built Map (O(1) lookup)
     */
    private void setCurrentStorageFromMap(RackBinMasterEntity entity, Map<String, Double> storageMap) {
        String rackColumnShelf = buildRackColumnShelfNumber(entity.getRackNo(), entity.getColumnNo(), entity.getBinNo());
        String key = buildStorageKey(entity.getStorageType(), entity.getStorageArea(), rackColumnShelf);
        Double storage = storageMap.getOrDefault(key, 0.0);
        entity.setCurrentStorage(storage);
    }

    /**
     * Build unique key for storage map
     * Key format: store|storageArea|rackColumnShelf
     */
    private String buildStorageKey(String store, String storageArea, String rackColumnShelf) {
        return (store != null ? store : "") + "|" +
               (storageArea != null ? storageArea : "") + "|" +
               (rackColumnShelf != null ? rackColumnShelf : "");
    }

    /**
     * Calculate current storage from Stock Summary based on store, storageArea, and rack/column/bin
     */
    private void calculateAndSetCurrentStorage(RackBinMasterEntity entity) {
        try {
            // Build rackColumnShelfNumber by combining rackNo, columnNo, binNo
            String rackColumnShelfNumber = buildRackColumnShelfNumber(entity.getRackNo(), entity.getColumnNo(), entity.getBinNo());

            log.debug("Calculating current storage for Store: {}, StorageArea: {}, RackColumnShelfNumber: {}",
                    entity.getStorageType(), entity.getStorageArea(), rackColumnShelfNumber);

            // Fetch current storage from Stock Summary
            Double currentStorage = stockSummaryRepository.calculateCurrentStorageByLocation(
                    entity.getStorageType(),
                    entity.getStorageArea(),
                    rackColumnShelfNumber
            );

            entity.setCurrentStorage(currentStorage != null ? currentStorage : 0.0);

            log.debug("Current storage calculated: {}", entity.getCurrentStorage());
        } catch (Exception e) {
            log.error("Error calculating current storage for entity ID: {}, Error: {}", entity.getId(), e.getMessage());
            entity.setCurrentStorage(0.0);
        }
    }

    /**
     * Build combined rackColumnShelfNumber from individual components
     */
    private String buildRackColumnShelfNumber(String rackNo, String columnNo, String binNo) {
        StringBuilder sb = new StringBuilder();
        if (rackNo != null && !rackNo.isEmpty()) {
            sb.append(rackNo);
        }
        if (columnNo != null && !columnNo.isEmpty()) {
            if (sb.length() > 0) sb.append("-");
            sb.append(columnNo);
        }
        if (binNo != null && !binNo.isEmpty()) {
            if (sb.length() > 0) sb.append("-");
            sb.append(binNo);
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    @Override
    public RackBinMasterEntity approveRackBin(Long id) throws Exception {
        RackBinMasterEntity rackBin = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rack-Bin not found with ID: " + id));
        rackBin.setStatus("APPROVED");
        return repository.save(rackBin);
    }

    @Override
    public RackBinMasterEntity rejectRackBin(Long id) throws Exception {
        RackBinMasterEntity rackBin = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rack-Bin not found with ID: " + id));
        rackBin.setStatus("REJECTED");
        return repository.save(rackBin);
    }

    @Override
    public List<RackBinMasterEntity> bulkCreate(List<RackBinMasterDto> dtoList) {
        log.info("Bulk creating {} rack-bin entries - OPTIMIZED", dtoList.size());
        long startTime = System.currentTimeMillis();

        // Parallel stream for faster entity creation
        List<RackBinMasterEntity> entities = dtoList.parallelStream().map(dto -> RackBinMasterEntity.builder()
                .unitCode(dto.getUnitCode())
                .unitName(dto.getUnitName())
                .storageType(dto.getStorageType())
                .storageArea(dto.getStorageArea())
                .rackNo(dto.getRackNo())
                .columnNo(dto.getColumnNo())
                .binNo(dto.getBinNo())
                .binCapacity(dto.getBinCapacity())
                .currentStorage(dto.getCurrentStorage() != null ? dto.getCurrentStorage() : 0.0)
                .distance(dto.getDistance())
                .itemCategory(dto.getItemCategory())
                .storageAreaOrder(dto.getStorageAreaOrder())
                .qr(dto.getQr())
                .automated(dto.isAutomated())
                .status(dto.getStatus() != null ? dto.getStatus() : "Pending")
                .build()).toList();

        // Batch save - saveAll uses batch insert internally
        List<RackBinMasterEntity> saved = repository.saveAll(entities);

        log.info("Successfully saved {} rack-bin entries in {}ms", saved.size(), System.currentTimeMillis() - startTime);
        return saved;
    }

    @Override
    public List<RackBinMasterEntity> getByStorageArea(String storageArea) {
        return repository.findByStorageArea(storageArea);
    }

    @Override
    public Map<String, Object> uploadExcelData(MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        List<RackBinMasterEntity> savedEntities = new ArrayList<>();

        try {
            if (file == null || file.isEmpty()) {
                response.put("status", "error");
                response.put("message", "File is empty");
                return response;
            }

            String fileName = file.getOriginalFilename();
            log.info("Processing file: {}", fileName);

            // Parse Excel/CSV file
            List<Map<String, String>> rows = parseFile(file, fileName);
            log.info("Parsed {} rows", rows.size());

            // Process each row
            List<RackBinMasterEntity> validEntities = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                Map<String, String> rowData = rows.get(i);
                try {
                    RackBinMasterEntity entity = mapRowToEntity(rowData);
                    validEntities.add(entity);
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", i + 2);
                    error.put("error", e.getMessage());
                    error.put("data", rowData);
                    errors.add(error);
                }
            }

            // Save valid entities
            if (!validEntities.isEmpty()) {
                savedEntities = repository.saveAll(validEntities);
            }

            response.put("status", errors.isEmpty() ? "success" : "partial");
            response.put("message", "Uploaded " + savedEntities.size() + " records");
            response.put("successCount", savedEntities.size());
            response.put("errorCount", errors.size());
            response.put("data", savedEntities);
            if (!errors.isEmpty()) {
                response.put("errors", errors);
            }

        } catch (Exception e) {
            log.error("Error uploading file: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
        }

        return response;
    }

    private List<Map<String, String>> parseFile(MultipartFile file, String fileName) throws Exception {
        List<Map<String, String>> rows = new ArrayList<>();

        if (fileName.toLowerCase().endsWith(".csv")) {
            // Parse CSV
            try (Scanner scanner = new Scanner(file.getInputStream())) {
                List<String> headers = new ArrayList<>();
                int lineNum = 0;
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    String[] values = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                    if (lineNum == 0) {
                        headers = Arrays.stream(values).map(String::trim).toList();
                    } else {
                        Map<String, String> rowMap = new HashMap<>();
                        for (int i = 0; i < Math.min(headers.size(), values.length); i++) {
                            rowMap.put(headers.get(i), values[i].trim().replaceAll("^\"|\"$", ""));
                        }
                        rows.add(rowMap);
                    }
                    lineNum++;
                }
            }
        } else {
            // Parse Excel
            try (InputStream is = file.getInputStream()) {
                Workbook workbook;
                if (fileName.toLowerCase().endsWith(".xlsx")) {
                    workbook = new XSSFWorkbook(is);
                } else {
                    workbook = new HSSFWorkbook(is);
                }

                Sheet sheet = workbook.getSheetAt(0);
                List<String> headers = new ArrayList<>();

                for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                    Row row = sheet.getRow(i);
                    if (row == null)
                        continue;

                    if (i == 0) {
                        for (int j = 0; j < row.getLastCellNum(); j++) {
                            Cell cell = row.getCell(j);
                            headers.add(cell != null ? getCellValueAsString(cell).trim() : "");
                        }
                    } else {
                        Map<String, String> rowMap = new HashMap<>();
                        for (int j = 0; j < headers.size(); j++) {
                            Cell cell = row.getCell(j);
                            rowMap.put(headers.get(j), cell != null ? getCellValueAsString(cell).trim() : "");
                        }
                        if (rowMap.values().stream().anyMatch(v -> !v.isEmpty())) {
                            rows.add(rowMap);
                        }
                    }
                }
                workbook.close();
            }
        }
        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case BLANK -> "";
            default -> cell.toString();
        };
    }

    private RackBinMasterEntity mapRowToEntity(Map<String, String> rowData) {
        return RackBinMasterEntity.builder()
                .unitCode(getValue(rowData, "UNIT CODE", "UNIT_CODE"))
                .unitName(getValue(rowData, "UNIT NAME", "UNIT_NAME"))
                .storageType(getValue(rowData, "STORE", "STORAGE_TYPE"))
                .storageArea(getValue(rowData, "STORAGE AREA", "STORAGE_AREA"))
                .rackNo(getValue(rowData, "RACK NO", "RACK_NO"))
                .columnNo(getValue(rowData, "COLUMN NO", "COLUMN_NO"))
                .binNo(getValue(rowData, "BIN NO", "BIN_NO"))
                .binCapacity(getValue(rowData, "BIN CAPACITY (Kg)", "BIN CAPACITY", "BIN_CAPACITY"))
                .currentStorage(
                        parseDouble(getValue(rowData, "CURRENT STORAGE (kg)", "CURRENT STORAGE", "CURRENT_STORAGE")))
                .distance(parseDouble(getValue(rowData, "DISTANCE")))
                .storageAreaOrder(parseInt(getValue(rowData, "ORDER", "STORAGE_AREA_ORDER")))
                .itemCategory(getValue(rowData, "ITEM CATEGORY", "ITEM_CATEGORY"))
                .status("Pending")
                .automated(false)
                .build();
    }

    private String getValue(Map<String, String> rowData, String... keys) {
        for (String key : keys) {
            if (rowData.containsKey(key) && !rowData.get(key).isEmpty()) {
                return rowData.get(key);
            }
            for (Map.Entry<String, String> entry : rowData.entrySet()) {
                if (entry.getKey().equalsIgnoreCase(key)) {
                    return entry.getValue();
                }
            }
        }
        return "";
    }

    private Double parseDouble(String value) {
        if (value == null || value.isEmpty())
            return 0.0;
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private Integer parseInt(String value) {
        if (value == null || value.isEmpty())
            return 0;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void deleteAll() {
        log.info("Deleting all rack-bin entries");
        repository.deleteAll();
        log.info("All rack-bin entries deleted successfully");
    }

//    @Override
//    public List<RackBinMasterEntity> getEligibleBins(String itemCategory, Double bundleNetWeight, String store) {
//        // Delegate to new method and extract list if successful
////        Map<String, Object> result = findBestEligibleBin(itemCategory, bundleNetWeight, store);
////        if (Boolean.TRUE.equals(result.get("success")) && result.get("bin") != null) {
////            return List.of((RackBinMasterEntity) result.get("bin"));
////        }
////        return Collections.emptyList();
//    }

    @Override
    public Map<String, Object> findBestEligibleBin(String itemCategory, Double bundleNetWeight, String store, String unitName) {

        Map<String, Object> response = new LinkedHashMap<>();

        log.info("┌─ STEP 1: Filter by Store ───────────────────────────────────────────────────┐");
        log.info("│ Looking for storageType = '{}'", store);

        List<RackBinMasterEntity> storeFilteredBins = repository.findByStorageTypeAndUnitName(store.trim(), unitName);

        log.info("│ ✅ Found {} bins for store: {}", storeFilteredBins.size(), store);
        log.info("└──────────────────────────────────────────────────────────────────────────────┘");

        if (storeFilteredBins.isEmpty()) {
            log.warn("❌ STEP 1 FAILED: No bins available for store: {}", store);
            response.put("error", "No bins available for store: " + store);
            response.put("errorCode", "NO_BINS_FOR_STORE");
            response.put("store", store);
            return response;
        }

        log.info("┌─ STEP 2: Filter by Category ────────────────────────────────────────────────┐");
        log.info("│ Looking for itemCategory = '{}'", itemCategory);

        List<RackBinMasterEntity> categoryFilteredBins = filterByCategory(storeFilteredBins, itemCategory.trim());
        boolean usedFallbackCategory = false;

        if (categoryFilteredBins.isEmpty()) {
            log.info("│ ⚠️ No bins found for category '{}', trying fallback category 'ALL'", itemCategory);
            categoryFilteredBins = filterByCategory(storeFilteredBins, "ALL");
            usedFallbackCategory = true;

            if (categoryFilteredBins.isEmpty()) {
                log.warn("❌ STEP 2 FAILED: No bins available for category: {} or ALL", itemCategory);
                log.info("└──────────────────────────────────────────────────────────────────────────────┘");
                response.put("error", "No bins available for category: " + itemCategory + " or ALL");
                response.put("errorCode", "NO_BINS_FOR_CATEGORY");
                response.put("store", store);
                response.put("requestedCategory", itemCategory);
                return response;
            }
            log.info("│ ✅ Found {} bins with fallback category 'ALL'", categoryFilteredBins.size());
        } else {
            log.info("│ ✅ Found {} bins for category: {}", categoryFilteredBins.size(), itemCategory);
        }
        log.info("└──────────────────────────────────────────────────────────────────────────────┘");

        log.info("┌─ STEP 3: Filter by Available Capacity ─────────────────────────────────────┐");
        log.info("│ Required capacity (bundleNetWeight): {} kg", bundleNetWeight);

        List<RackBinMasterEntity> capacityFilteredBins = filterByCapacity(categoryFilteredBins, bundleNetWeight);

        if (capacityFilteredBins.isEmpty() && !usedFallbackCategory) {
            // BACKTRACK: Try with category = "ALL"
            log.info("│ ⚠️ No bins with sufficient capacity for category '{}', backtracking to 'ALL'", itemCategory);

            List<RackBinMasterEntity> fallbackCategoryBins = filterByCategory(storeFilteredBins, "ALL");
            if (!fallbackCategoryBins.isEmpty()) {
                capacityFilteredBins = filterByCapacity(fallbackCategoryBins, bundleNetWeight);
                if (!capacityFilteredBins.isEmpty()) {
                    usedFallbackCategory = true;
                    log.info("│ ✅ Found {} bins with capacity after backtrack to 'ALL'", capacityFilteredBins.size());
                }
            }
        }

        if (capacityFilteredBins.isEmpty()) {
            log.warn("❌ STEP 3 FAILED: No bins with sufficient capacity for weight: {} kg", bundleNetWeight);
            log.info("└──────────────────────────────────────────────────────────────────────────────┘");
            response.put("error", "No bins with sufficient capacity for weight: " + bundleNetWeight + " kg");
            response.put("errorCode", "NO_BINS_WITH_CAPACITY");
            response.put("store", store);
            response.put("requestedCategory", itemCategory);
            response.put("requiredWeight", bundleNetWeight);
            return response;
        }

        log.info("│ ✅ Found {} bins with sufficient capacity", capacityFilteredBins.size());
        log.info("└──────────────────────────────────────────────────────────────────────────────┘");

        log.info("┌─ STEP 4: Select by Minimum storage_area_order ─────────────────────────────┐");

        List<RackBinMasterEntity> minOrderBins = filterByMinStorageAreaOrder(capacityFilteredBins);

        log.info("│ ✅ Found {} bins with minimum storage_area_order", minOrderBins.size());
        log.info("└──────────────────────────────────────────────────────────────────────────────┘");

        // If only one bin, return it
        if (minOrderBins.size() == 1) {
            RackBinMasterEntity bestBin = minOrderBins.get(0);
            log.info("✅ SINGLE BEST BIN FOUND (after Step 4): ID={}, Rack={}, Bin={}",
                    bestBin.getId(), bestBin.getRackNo(), bestBin.getBinNo());
            return buildSuccessResponse(bestBin, usedFallbackCategory, itemCategory, store, bundleNetWeight);
        }

        log.info("┌─ STEP 5: Select by Minimum Distance ───────────────────────────────────────┐");

        RackBinMasterEntity bestBin = selectByMinDistance(minOrderBins);

        if (bestBin == null) {
            log.error("❌ STEP 5 FAILED: Could not select bin by distance");
            log.info("└──────────────────────────────────────────────────────────────────────────────┘");
            response.put("error", "Failed to select bin by distance");
            response.put("errorCode", "SELECTION_FAILED");
            return response;
        }

        log.info("│ ✅ Selected bin with minimum distance: ID={}, Distance={}",
                bestBin.getId(), bestBin.getDistance());
        log.info("└──────────────────────────────────────────────────────────────────────────────┘");

        return buildSuccessResponse(bestBin, usedFallbackCategory, itemCategory, store, bundleNetWeight);
    }

    /**
     * Filter bins by item category (case-insensitive)
     */
    private List<RackBinMasterEntity> filterByCategory(List<RackBinMasterEntity> bins, String category) {
        return bins.stream()
                .filter(bin -> bin.getItemCategory() != null &&
                        bin.getItemCategory().trim().equalsIgnoreCase(category.trim()))
                .toList();
    }

    /**
     * Filter bins by available capacity: (binCapacity - currentStorage) >=
     * bundleNetWeight
     */
    private List<RackBinMasterEntity> filterByCapacity(List<RackBinMasterEntity> bins, Double bundleNetWeight) {
        return bins.stream()
                .filter(bin -> {
                    Double availableSpace = calculateAvailableSpace(bin);
                    boolean hasCapacity = availableSpace >= bundleNetWeight;
                    log.debug("   Bin ID={}: capacity={}, current={}, available={}, required={}, hasCapacity={}",
                            bin.getId(), bin.getBinCapacity(), bin.getCurrentStorage(),
                            availableSpace, bundleNetWeight, hasCapacity);
                    return hasCapacity;
                })
                .toList();
    }

    /**
     * Calculate available space in a bin: binCapacity - currentStorage
     */
    private Double calculateAvailableSpace(RackBinMasterEntity bin) {
        Double binCapacity = parseBinCapacity(bin.getBinCapacity());
        Double currentStorage = bin.getCurrentStorage() != null ? bin.getCurrentStorage() : 0.0;
        return binCapacity - currentStorage;
    }

    /**
     * Parse bin capacity from String to Double
     */
    private Double parseBinCapacity(String binCapacity) {
        if (binCapacity == null || binCapacity.trim().isEmpty()) {
            return 0.0;
        }
        try {
            // Remove any non-numeric characters except decimal point
            String cleaned = binCapacity.replaceAll("[^0-9.]", "");
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            log.warn("Failed to parse bin capacity: '{}', defaulting to 0.0", binCapacity);
            return 0.0;
        }
    }

    /**
     * Filter bins to get only those with minimum storage_area_order
     */
    private List<RackBinMasterEntity> filterByMinStorageAreaOrder(List<RackBinMasterEntity> bins) {
        if (bins.isEmpty())
            return Collections.emptyList();

        // Find minimum storage_area_order (treat null as MAX_VALUE)
        Integer minOrder = bins.stream()
                .map(bin -> bin.getStorageAreaOrder() != null ? bin.getStorageAreaOrder() : Integer.MAX_VALUE)
                .min(Integer::compareTo)
                .orElse(Integer.MAX_VALUE);

        log.info("│ Minimum storage_area_order: {}", minOrder == Integer.MAX_VALUE ? "N/A" : minOrder);

        // Filter bins with minimum order
        return bins.stream()
                .filter(bin -> {
                    Integer order = bin.getStorageAreaOrder() != null ? bin.getStorageAreaOrder() : Integer.MAX_VALUE;
                    return order.equals(minOrder);
                }).toList();
    }

    /**
     * Select the bin with minimum distance
     */
    private RackBinMasterEntity selectByMinDistance(List<RackBinMasterEntity> bins) {
        if (bins.isEmpty())
            return null;

        return bins.stream()
                .min(Comparator.comparing(
                        bin -> bin.getDistance() != null ? bin.getDistance() : Double.MAX_VALUE))
                .orElse(null);
    }

    /**
     * Build success response map (simplified - no full entity)
     */
    private Map<String, Object> buildSuccessResponse(RackBinMasterEntity bin, boolean usedFallback,
            String requestedCategory, String store, Double bundleNetWeight) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("binId", bin.getId());
        response.put("rackNo", bin.getRackNo());
        response.put("columnNo", bin.getColumnNo());
        response.put("binNo", bin.getBinNo());
        response.put("storageType", bin.getStorageType());
        response.put("storageArea", bin.getStorageArea());
        response.put("itemCategory", bin.getItemCategory());
        response.put("binCapacity", bin.getBinCapacity());
        response.put("currentStorage", bin.getCurrentStorage());
        response.put("availableSpace", calculateAvailableSpace(bin));
        response.put("distance", bin.getDistance());
        response.put("storageAreaOrder", bin.getStorageAreaOrder());
        response.put("usedFallbackCategory", usedFallback);
        response.put("requestedCategory", requestedCategory);
        response.put("store", store);
        response.put("bundleNetWeight", bundleNetWeight);
        return response;
    }

    @Transactional
    public void updateCurrentStorage(RackBinStorageQtyUpdateDto dto) {

        RackBinMasterEntity identifiedBin = repository
                .findByStorageTypeAndStorageAreaAndRackNoAndColumnNoAndBinNoAndUnitName(
                        dto.getStorageType(),
                        dto.getStorageArea(),
                        dto.getRackNo(),
                        dto.getColumnNo(),
                        dto.getBinNo(),
                        dto.getUnit())
                .orElseThrow(() -> new RuntimeException("Identified bin not found"));

        RackBinMasterEntity commonBin = repository
                .findByStorageTypeAndBinNoAndStorageAreaAndUnitName(
                        dto.getStorageType(),
                        "Common bin",
                        "COMMON",
                        dto.getUnit())
                .orElseThrow(() -> new RuntimeException("Common bin not found"));

        Double currentStorage = identifiedBin.getCurrentStorage() == null ? 0.0 : identifiedBin.getCurrentStorage();
        Double commonCurrentStorage = commonBin.getCurrentStorage() == null ? 0.0 : commonBin.getCurrentStorage();
        Double qtyChange = dto.getCurrentStorageQtyChange() == null ? 0.0 : dto.getCurrentStorageQtyChange();

        double updatedIdentified;
        double updatedCommon;

        if ("save".equalsIgnoreCase(dto.getAction())) {

            updatedIdentified = currentStorage + qtyChange;
            updatedCommon = commonCurrentStorage - qtyChange;

        } else if ("delete".equalsIgnoreCase(dto.getAction())) {

            updatedIdentified = currentStorage - qtyChange;
            updatedCommon = commonCurrentStorage + qtyChange;

        } else {
            throw new IllegalArgumentException("Invalid action: " + dto.getAction());
        }

        // ✅ Validation: prevent negative storage
        if (updatedIdentified < 0) {
            throw new RuntimeException("Identified bin storage cannot be negative");
        }

        if (updatedCommon < 0) {
            throw new RuntimeException("Common bin storage cannot be negative");
        }


        // ✅ Apply updates only after validation
        identifiedBin.setCurrentStorage(updatedIdentified);
        commonBin.setCurrentStorage(updatedCommon);

        repository.save(identifiedBin);
        repository.save(commonBin);
    }

//    private void updateCapacity(RackBinMasterEntity bin, double qtyChange, boolean isAdd) {
//
//        if (bin.getBinCapacity() == null)
//            return;
//
//        if ("UNLIMITED".equalsIgnoreCase(bin.getBinCapacity()))
//            return;
//
//        double capacity = Double.parseDouble(bin.getBinCapacity());
//
//        capacity = isAdd ? capacity - qtyChange : capacity + qtyChange;
//
//        if (capacity < 0) {
//            throw new RuntimeException("Bin capacity cannot be negative");
//        }
//
//        bin.setBinCapacity(String.valueOf(capacity));
//    }
}

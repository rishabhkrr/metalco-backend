package com.indona.invento.services.impl;

import com.indona.invento.dao.ItemMasterRepository;
import com.indona.invento.dto.ItemMasterDto;
import com.indona.invento.dto.StockSummaryDto;
import com.indona.invento.entities.ItemMasterEntity;
import com.indona.invento.services.ApprovalWorkflowService;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.ItemMasterService;
import com.indona.invento.services.StockSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ItemMasterServiceImpl implements ItemMasterService {

    private static final Logger logger = LoggerFactory.getLogger(ItemMasterServiceImpl.class);

    @Autowired
    private ItemMasterRepository repository;

    @Autowired
    private StockSummaryService stockSummaryService;

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public ItemMasterEntity createItem(ItemMasterDto dto) {
        logger.info("📝 [Service] Creating Item Master - Section Number: {}", dto.getSectionNumber());

        logger.info("🔨 [Service] Building ItemMasterEntity...");
        ItemMasterEntity item = ItemMasterEntity.builder()
                .productCategory(dto.getProductCategory())
                .materialType(dto.getMaterialType())
                .hsnCode(generateHSN(dto.getProductCategory()))
                .sectionNumber(dto.getSectionNumber())
                .dimension1(dto.getDimension1())
                .dimension2(dto.getDimension2())
                .dimension3(dto.getDimension3())
                .dimension(dto.getDimension())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .brand(dto.getBrand())
                .unitName(dto.getUnitName())
                .narration(dto.getNarration())
                .skuDescription(dto.getSkuDescription())
                .supplierCode(dto.getSupplierCode())
                .supplierName(dto.getSupplierName())
                .primaryUom(dto.getPrimaryUom())
                .altUomApplicable(dto.getAltUomApplicable())
                .altUom(dto.getAltUom())
                .reportingUom(dto.getReportingUom() != null ? dto.getReportingUom() : dto.getPrimaryUom())
                .leadTimeDays(dto.getLeadTimeDays())
                .moq(dto.getMoq())
                .gstApplicable(dto.getGstApplicable())
                .gstRate(dto.getGstRate())
                .openingStockInKgs(dto.getOpeningStockInKgs())
                .openingStockInNos(dto.getOpeningStockInNos())
                .itemPrice(dto.getItemPrice())
                .status("PENDING_APPROVAL")
                .build();

        logger.info("✅ Entity built. Before saving - Section Number: {}, Unit Name: {}", item.getSectionNumber(), item.getUnitName());
        ItemMasterEntity savedItem = repository.save(item);
        logger.info("💾 Item saved to ItemMaster table - ID: {}, Section Number: {}, Unit Name: {}", savedItem.getId(), savedItem.getSectionNumber(), savedItem.getUnitName());

        logger.info("🔄 [Service] Creating StockSummaryDto...");
        String stockDimension = (dto.getDimension());

        StockSummaryDto stockDto = StockSummaryDto.builder()
                .unit(dto.getUnitName()) // default or fetch from context
                .store("") // default or fetch from context
                .storageArea("") // optional
                .rackColumnShelfNumber("") // optional
                .productCategory(dto.getProductCategory())
                .itemDescription(dto.getSkuDescription())
                .brand(dto.getBrand())
                .materialType(dto.getMaterialType())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .dimension(stockDimension)
                .dimension1(dto.getDimension1())
                .dimension2(dto.getDimension2())
                .dimension3(dto.getDimension3())
                .quantityKg(dto.getOpeningStockInKgs() != null ? dto.getOpeningStockInKgs() : BigDecimal.ZERO)
                .quantityNo(dto.getOpeningStockInNos() != null ? dto.getOpeningStockInNos().intValue() : 0)
                .itemPrice(dto.getItemPrice() != null ? dto.getItemPrice() : BigDecimal.ZERO)
                .reprintQr(false)
                .sectionNo(dto.getSectionNumber())
                .build();

        logger.info("📦 StockSummaryDto created - Section No: {}", stockDto.getSectionNo());
        logger.info("📝 About to save to StockSummary table with Section No: {}", stockDto.getSectionNo());
        stockSummaryService.create(stockDto);
        logger.info("✅ StockSummary saved successfully!");

        // Audit log and approval workflow
        auditLogService.logAction("CREATE", "ITEM_MASTER", "ItemMaster",
                savedItem.getId(), savedItem.getSectionNumber(), null, "PENDING_APPROVAL",
                "Item " + savedItem.getSkuDescription() + " created with section " + savedItem.getSectionNumber(),
                "SYSTEM", savedItem.getUnitName());

        approvalWorkflowService.submitForApproval("ItemMaster", savedItem.getId(),
                savedItem.getSectionNumber() != null ? savedItem.getSectionNumber() : String.valueOf(savedItem.getId()),
                "ITEM_MASTER", "SYSTEM", savedItem.getUnitName(), 0L);

        return savedItem;
    }
    @Override
    public ItemMasterEntity getItemById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + id));
    }

    @Override
    public Page<ItemMasterEntity> getAllItems(Pageable pageable) {
        return repository.findAll(pageable);
    }


    @Override
    public void deleteItem(Long id) {
        repository.deleteById(id);
    }

    private String generateHSN(String category) {
        if (category == null || category.isBlank()) return "00000000";
        return String.format("%08d", Math.abs(category.hashCode()) % 100000000);
    }

    @Override
    public ItemMasterEntity updateItem(Long id, ItemMasterDto dto) {
        ItemMasterEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + id));

        existing.setProductCategory(dto.getProductCategory());
        existing.setMaterialType(dto.getMaterialType());
        existing.setHsnCode(generateHSN(dto.getProductCategory())); // Optional: regenerate HSN
        existing.setSectionNumber(dto.getSectionNumber());
        existing.setDimension1(dto.getDimension1());
        existing.setDimension2(dto.getDimension2());
        existing.setDimension3(dto.getDimension3());
        // Set dimension: use provided value or build from dimension1/2/3
        existing.setDimension(dto.getDimension());
        existing.setGrade(dto.getGrade());
        existing.setTemper(dto.getTemper());
        existing.setBrand(dto.getBrand());
        existing.setNarration(dto.getNarration());
        existing.setSkuDescription(dto.getSkuDescription());
        existing.setSupplierCode(dto.getSupplierCode());
        existing.setSupplierName(dto.getSupplierName());
        existing.setPrimaryUom(dto.getPrimaryUom());
        existing.setAltUomApplicable(dto.getAltUomApplicable());
        existing.setAltUom(dto.getAltUom());
        existing.setReportingUom(dto.getReportingUom() != null ? dto.getReportingUom() : dto.getPrimaryUom());
        existing.setLeadTimeDays(dto.getLeadTimeDays());
        existing.setMoq(dto.getMoq());
        existing.setStatus("PENDING_APPROVAL");
        existing.setItemPrice(dto.getItemPrice());
        existing.setOpeningStockInKgs(dto.getOpeningStockInKgs());
        existing.setOpeningStockInNos(dto.getOpeningStockInNos());
        existing.setGstApplicable(dto.getGstApplicable());
        existing.setGstRate(dto.getGstRate());
        existing.setUnitName(dto.getUnitName());

        return repository.save(existing);
    }

    private String buildDimension(String d1, String d2, String d3) {
        StringBuilder sb = new StringBuilder();

        if (d1 != null && !d1.isBlank()) sb.append(d1);
        if (d2 != null && !d2.isBlank()) sb.append("x").append(d2);
        if (d3 != null && !d3.isBlank()) sb.append("x").append(d3);

        return sb.toString();
    }

    @Override
    public List<ItemMasterEntity> getAllItemsWithoutPagination() {
        return repository.findAll();
    }

    @Override
    public Optional<ItemMasterEntity> getItemByCategoryAndDescription(String category, String description) {
        return repository.findAll().stream()
                .filter(item -> item.getProductCategory() != null && item.getProductCategory().equalsIgnoreCase(category)
                        && item.getSkuDescription() != null && item.getSkuDescription().equalsIgnoreCase(description))
                .findFirst();
    }

    @Override
    public ItemMasterEntity approveItem(Long id) throws Exception {
        ItemMasterEntity item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + id));
        item.setStatus("APPROVED");
        ItemMasterEntity approved = repository.save(item);

        approvalWorkflowService.approve("ItemMaster", id,
                item.getSectionNumber() != null ? item.getSectionNumber() : String.valueOf(id),
                "ITEM_MASTER", "ADMIN", null, item.getUnitName());

        return approved;
    }

    @Override
    public ItemMasterEntity rejectItem(Long id) throws Exception {
        ItemMasterEntity item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with ID: " + id));
        item.setStatus("REJECTED");
        ItemMasterEntity rejected = repository.save(item);

        approvalWorkflowService.reject("ItemMaster", id,
                item.getSectionNumber() != null ? item.getSectionNumber() : String.valueOf(id),
                "ITEM_MASTER", "ADMIN", "Item rejected", item.getUnitName());

        return rejected;
    }

    @Override
    public List<ItemMasterEntity> getItemsByCategoryAndBrand(String productCategory, String brand) {
        logger.info("🔍 [Service] Searching items - Category: {}, Brand: {}", productCategory, brand);

        List<ItemMasterEntity> items = getAllItemsWithoutPagination().stream()
                .filter(item ->
                    item.getProductCategory() != null && item.getProductCategory().equalsIgnoreCase(productCategory) &&
                    item.getBrand() != null && item.getBrand().equalsIgnoreCase(brand)
                )
                .toList();

        logger.info("📦 Found {} matching items", items.size());
        return items;
    }

    @Override
    public Map<String, Object> getDimensionByItemDescription(String itemDescription) {
        logger.info("🔍 [ItemMaster] Getting dimension for itemDescription: {}", itemDescription);

        Map<String, Object> response = new HashMap<>();

        // Find all items matching the description (case-insensitive)
        List<ItemMasterEntity> matchingItems = repository.findAll().stream()
                .filter(item -> item.getSkuDescription() != null &&
                        item.getSkuDescription().equalsIgnoreCase(itemDescription))
                .toList();

        logger.info("   Found {} item(s) matching description", matchingItems.size());

        if (matchingItems.isEmpty()) {
            logger.warn("❌ Item not found with description: {}", itemDescription);
            response.put("success", false);
            response.put("message", "Item not found with description: " + itemDescription);
            return response;
        }

        // Pick the first matching item
        ItemMasterEntity item = matchingItems.get(0);
        logger.info("   Using first match - Item ID: {}", item.getId());

        String dimension1 = item.getDimension1() != null ? item.getDimension1() : "";
        String dimension2 = item.getDimension2() != null ? item.getDimension2() : "";
        String dimension3 = item.getDimension3() != null ? item.getDimension3() : "";
        String storedDimension = item.getDimension() != null ? item.getDimension() : "";
        // Use stored dimension if available, otherwise build from dimension1/2/3
        String combinedDimension = !storedDimension.isBlank()
                ? storedDimension
                : buildDimension(dimension1, dimension2, dimension3);

        logger.info("✅ Found item - Dimension1: {}, Dimension2: {}, Dimension3: {}", dimension1, dimension2, dimension3);
        logger.info("   Stored Dimension: {}", storedDimension);
        logger.info("   Combined Dimension: {}", combinedDimension);

        response.put("success", true);
        response.put("itemDescription", itemDescription);
        response.put("dimension1", dimension1);
        response.put("dimension2", dimension2);
        response.put("dimension3", dimension3);
        response.put("dimension", combinedDimension);
        response.put("totalMatchingItems", matchingItems.size());

        return response;
    }

    @Override
    public Map<String, Object> bulkUploadItems(org.springframework.web.multipart.MultipartFile file) {
        logger.info("╔══════════════════════════════════════════════════════════════════════════╗");
        logger.info("║                    📤 BULK UPLOAD ITEM MASTER                            ║");
        logger.info("╠══════════════════════════════════════════════════════════════════════════╣");
        logger.info("║  📁 File Name: {}", file.getOriginalFilename());
        logger.info("╚══════════════════════════════════════════════════════════════════════════╝");

        Map<String, Object> response = new java.util.LinkedHashMap<>();
        List<ItemMasterEntity> items = new java.util.ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        List<String> errors = new java.util.ArrayList<>();

        String fileName = file.getOriginalFilename();
        boolean isExcel = fileName != null && (fileName.endsWith(".xlsx") || fileName.endsWith(".xls"));

        try {
            List<String[]> rows;

            if (isExcel) {
                // Parse Excel file
                logger.info("   📊 Detected Excel file - parsing with Apache POI...");
                rows = parseExcelFile(file);
            } else {
                // Parse CSV file
                logger.info("   📄 Detected CSV file - parsing...");
                rows = parseCsvFile(file);
            }

            logger.info("   📋 Total rows (excluding header): {}", rows.size());

            int rowNumber = 1;
            for (String[] columns : rows) {
                rowNumber++;
                try {
                    // CSV/Excel Column mapping based on your data:
                    // 0: brand, 1: grade, 2: lead_time_days, 3: moq, 4: rm_type (skip)
                    // 5: sku_description, 6: supplier_code, 7: supplier_name, 8: temper
                    // 9: uom (primaryUom), 10: alt_uom, 11: alt_uom_applicable
                    // 12: dimension1, 13: dimension2, 14: dimension3, 15: Dimension
                    // 16: gst_applicable, 17: gst_rate, 18: hsn_code, 19: material_type
                    // 20: primary_uom (skip, using 9), 21: product_category, 22: reporting_uom
                    // 23: section_number, 24: item_price, 25: opening_stock_kg, 26: opening_stock_no

                    ItemMasterEntity item = ItemMasterEntity.builder()
                            .brand(getValueOrNull(columns, 0))
                            .grade(getValueOrNull(columns, 1))
                            .leadTimeDays(parseInteger(getValueOrNull(columns, 2)))
                            .moq(parseBigDecimal(getValueOrNull(columns, 3)))
                            .skuDescription(getValueOrNull(columns, 5))
                            .supplierCode(getValueOrNull(columns, 6))
                            .supplierName(getValueOrNull(columns, 7))
                            .temper(getValueOrNull(columns, 8))
                            .primaryUom(getValueOrNull(columns, 9))
                            .altUom(getValueOrNull(columns, 10))
                            .altUomApplicable(getValueOrNull(columns, 11))
                            .dimension1(getValueOrNull(columns, 12))
                            .dimension2(getValueOrNull(columns, 13))
                            .dimension3(getValueOrNull(columns, 14))
                            .dimension(getValueOrNull(columns, 15))
                            .gstApplicable(getValueOrNull(columns, 16))
                            .gstRate(parseBigDecimal(getValueOrNull(columns, 17)))
                            .hsnCode(getValueOrNull(columns, 18))
                            .materialType(getValueOrNull(columns, 19))
                            .productCategory(getValueOrNull(columns, 21))
                            .reportingUom(getValueOrNull(columns, 22))
                            .sectionNumber(getValueOrNull(columns, 23))
                            .itemPrice(parseBigDecimal(getValueOrNull(columns, 24)))
                            .openingStockInKgs(parseBigDecimal(getValueOrNull(columns, 25)))
                            .openingStockInNos(parseBigDecimal(getValueOrNull(columns, 26)))
                            .status("PENDING_APPROVAL")
                            .build();

                    items.add(item);
                    successCount++;
                    logger.info("   ✅ Row {}: {} - Parsed successfully", rowNumber, item.getSkuDescription());

                } catch (Exception e) {
                    failedCount++;
                    String errorMsg = "Row " + rowNumber + ": " + e.getMessage();
                    errors.add(errorMsg);
                    logger.error("   ❌ {}", errorMsg);
                }
            }

            // Save all items
            if (!items.isEmpty()) {
                repository.saveAll(items);
                logger.info("💾 Saved {} items to database", items.size());
            }

            response.put("success", true);
            response.put("message", "Bulk upload completed");
            response.put("totalRecords", successCount + failedCount);
            response.put("successCount", successCount);
            response.put("failedCount", failedCount);
            response.put("errors", errors);

            logger.info("╔══════════════════════════════════════════════════════════════════════════╗");
            logger.info("║  ✅ BULK UPLOAD COMPLETE                                                 ║");
            logger.info("║     - Total Records: {}", successCount + failedCount);
            logger.info("║     - Success: {}", successCount);
            logger.info("║     - Failed: {}", failedCount);
            logger.info("╚══════════════════════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            logger.error("❌ Bulk upload failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Bulk upload failed: " + e.getMessage());
        }

        return response;
    }

    /**
     * Parse Excel file (.xlsx or .xls) and return list of rows
     */
    private List<String[]> parseExcelFile(org.springframework.web.multipart.MultipartFile file) throws Exception {
        List<String[]> rows = new java.util.ArrayList<>();

        try (java.io.InputStream is = file.getInputStream();
             org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(is)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);
            boolean isFirstRow = true;

            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                if (isFirstRow) {
                    isFirstRow = false; // Skip header row
                    continue;
                }

                int lastCellNum = row.getLastCellNum();
                if (lastCellNum < 0) continue;

                String[] columns = new String[Math.max(lastCellNum, 27)];

                for (int i = 0; i < lastCellNum; i++) {
                    org.apache.poi.ss.usermodel.Cell cell = row.getCell(i);
                    columns[i] = getCellValueAsString(cell);
                }

                // Skip empty rows
                boolean hasData = false;
                for (String col : columns) {
                    if (col != null && !col.trim().isEmpty()) {
                        hasData = true;
                        break;
                    }
                }

                if (hasData) {
                    rows.add(columns);
                }
            }
        }

        return rows;
    }

    /**
     * Get cell value as String
     */
    private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return sanitizeString(cell.getStringCellValue());
            case NUMERIC:
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // Avoid scientific notation
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return null;
            default:
                return null;
        }
    }

    /**
     * Parse CSV file and return list of rows
     */
    private List<String[]> parseCsvFile(org.springframework.web.multipart.MultipartFile file) throws Exception {
        List<String[]> rows = new java.util.ArrayList<>();

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(file.getInputStream()))) {

            String headerLine = reader.readLine(); // Skip header row
            logger.info("   📋 Header: {}", headerLine);

            String line;
            while ((line = reader.readLine()) != null) {
                String[] columns = parseCsvLine(line);
                rows.add(columns);
            }
        }

        return rows;
    }

    private String getValueOrNull(String[] line, int index) {
        if (line == null || index >= line.length) return null;
        String value = line[index];
        if (value == null || value.trim().isEmpty()) return null;
        // Sanitize the string to remove invalid control characters
        return sanitizeString(value.trim());
    }

    /**
     * Remove invalid control characters that cause JSON serialization issues
     * Characters 0x00-0x1F (except tab, newline, carriage return) are invalid
     */
    private String sanitizeString(String input) {
        if (input == null) return null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // Allow tab (0x09), newline (0x0A), carriage return (0x0D), and all chars >= 0x20
            if (c >= 0x20 || c == 0x09 || c == 0x0A || c == 0x0D) {
                sb.append(c);
            }
            // Skip invalid control characters (0x00-0x08, 0x0B, 0x0C, 0x0E-0x1F)
        }
        return sb.toString();
    }

    private java.math.BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new java.math.BigDecimal(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Parse CSV line handling quoted values and tabs
     */
    private String[] parseCsvLine(String line) {
        if (line == null || line.isEmpty()) {
            return new String[0];
        }

        // Try tab-separated first (your data seems to be tab-separated)
        String[] parts = line.split("\t", -1);
        if (parts.length > 5) {
            return parts;
        }

        // Fallback to comma-separated
        java.util.List<String> result = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString().trim());

        return result.toArray(new String[0]);
    }

}

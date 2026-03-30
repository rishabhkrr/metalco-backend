package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.AddBundleRequestDto;
import com.indona.invento.dto.GenerateQrRequestDto;
import com.indona.invento.dto.GenerateQrResponseDto;
import com.indona.invento.dto.GrnLineItemDto;
import com.indona.invento.entities.*;
import com.indona.invento.services.GrnLineItemService;
import com.indona.invento.util.QrCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GrnLineItemServiceImpl implements GrnLineItemService {

    private final GrnLineItemRepository repository;
    private final GRNRepository grnRepository;
    private final StockTransferRepository stockTransferRepository;
    private final StockSummaryRepository stockSummaryRepository;
    private final StockSummaryBundleRepository stockSummaryBundleRepository;
    private final QrCodeGenerator qrCodeGenerator;
    private final ItemMasterRepository itemMasterRepository;

    @Override
    @Transactional
    public List<GrnLineItemDto> addBundles(AddBundleRequestDto request) {
        log.info("\n");
        log.info("╔══════════════════════════════════════════════════════════════════════════════════════╗");
        log.info("║                        🚀 ADD BUNDLES API CALLED                                     ║");
        log.info("╠══════════════════════════════════════════════════════════════════════════════════════╣");
        log.info("║  📋 REQUEST DETAILS:                                                                 ║");
        log.info("║     - GRN Number: '{}'", request.getGrnNumber());
        log.info("║     - GRN ID: {}", request.getGrnId());
        log.info("║     - Transfer Number: '{}'", request.getTransferNumber());
        log.info("║     - Stock Transfer ID: {}", request.getStockTransferId());
        log.info("║     - Transfer Type: '{}'", request.getTransferType());
        log.info("║     - Input Type: '{}'", request.getInputType());
        log.info("║     - User ID: '{}'", request.getUserId());
        log.info("║     - Unit ID: '{}'", request.getUnitId());
        log.info("║     - Store: '{}'", request.getStore());
        log.info("║     - Storage Area: '{}'", request.getStorageArea());
        log.info("║     - Total Items in Request: {}", request.getItems() != null ? request.getItems().size() : 0);
        log.info("╚══════════════════════════════════════════════════════════════════════════════════════╝");
        log.info("\n");

        List<GrnLineItemEntity> lineItems = request.getItems().stream()
                .map(item -> GrnLineItemEntity.builder()
                        .grnNumber(request.getGrnNumber())
                        .grnId(request.getGrnId())
                        .stockTransferId(request.getStockTransferId())
                        .transferNumber(request.getTransferNumber())
                        .inputType(request.getInputType())
                        .slNo(item.getSlNo())
                        .itemDescription(item.getItemDescription())
                        .productCategory(item.getProductCategory())
                        .sectionNumber(item.getSectionNumber())
                        .brand(item.getBrand())
                        .grade(item.getGrade())
                        .temper(item.getTemper())
                        .dimension(item.getDimension())
                        .weighment(item.getWeighment())
                        .weightmentQuantityKg(item.getWeightmentQuantityKg())
                        .uomNetWeight(item.getUomNetWeight())
                        .weightmentQuantityNo(item.getWeightmentQuantityNo())
                        .uomNo(item.getUomNo())
                        // Warehouse Storage Fields (per-bundle)
                        .currentStore(item.getCurrentStore())
                        .materialAcceptance(item.getMaterialAcceptance())
                        .recipientStore(item.getRecipientStore())
                        .storageArea(item.getStorageArea())
                        .rackColumnBinNumber(item.getRackColumnBinNumber())
                        .rackStatus(item.getRackStatus())
                        // PO Number
                        .poNumber(item.getPoNumber())
                        // User and Unit Information (from request - outside items)
                        .userId(request.getUserId())
                        .unitId(request.getUnitId())
                        // QR Code URL (if provided)
                        .qrCodeImageUrl(item.getQrCodeUrl())
                        .status("ADDED")
                        .createdBy(request.getCreatedBy())
                        .build())
                .collect(Collectors.toList());

        List<GrnLineItemEntity> saved = repository.saveAll(lineItems);

        // Update GRN binStatus to COMPLETED
        if (request.getGrnId() != null) {
            GRNEntity grn = grnRepository.findById(request.getGrnId())
                    .orElseThrow(() -> new RuntimeException("GRN not found with ID: " + request.getGrnId()));
            grn.setBinStatus("COMPLETED");
            grnRepository.save(grn);
        }

        // Update Stock Transfer with transferType and item details from first bundle
        if (request.getStockTransferId() != null) {
            StockTransferEntity stockTransfer = stockTransferRepository.findById(request.getStockTransferId())
                    .orElseThrow(() -> new RuntimeException("Stock Transfer not found with ID: " + request.getStockTransferId()));

            // Update transferType
            if (request.getTransferType() != null) {
                stockTransfer.setTransferType(request.getTransferType());
            }

            // Populate brand and other item details from first bundle item
            if (!request.getItems().isEmpty()) {
                AddBundleRequestDto.BundleItemDto firstItem = request.getItems().get(0);
                stockTransfer.setBrand(firstItem.getBrand());
                stockTransfer.setItemDescription(firstItem.getItemDescription());
                stockTransfer.setProductCategory(firstItem.getProductCategory());
                stockTransfer.setSectionNumber(firstItem.getSectionNumber());
                stockTransfer.setGrade(firstItem.getGrade());
                stockTransfer.setTemper(firstItem.getTemper());
            }

            stockTransferRepository.save(stockTransfer);
        }

        // STEP 5.5: Fetch GRN Status from GRNRepository using GRN Number
        log.info("\n▶️ STEP 5.5: Fetching GRN Status from GRNRepository by GRN Number...");
        String grnStatus = null;
        String grnRefNumber = request.getGrnNumber();
        GRNEntity fetchedGrn = null;

        if (grnRefNumber != null && !grnRefNumber.isEmpty()) {
            log.info("   🔍 Searching GRN with Reference Number: {}", grnRefNumber);
            fetchedGrn = grnRepository.findByGrnRefNumber(grnRefNumber).orElse(null);

            if (fetchedGrn != null) {
                grnStatus = fetchedGrn.getStatus();
                log.info("   ✅ GRN Found!");
                log.info("      - GRN Reference Number: {}", grnRefNumber);
                log.info("      - GRN Status: {}", grnStatus);
                log.info("      - GRN ID: {}", fetchedGrn.getId());
                log.info("      - GRN Invoice Number: {}", fetchedGrn.getInvoiceNumber());
                log.info("      - GRN Unit: {}", fetchedGrn.getUnit());
                log.info("      - GRN Supplier: {}", fetchedGrn.getSupplierName());
                log.info("      - GRN Vehicle Number: {}", fetchedGrn.getVehicleNumber());
                log.info("      - GRN Items Count: {}", fetchedGrn.getGrnItems() != null ? fetchedGrn.getGrnItems().size() : 0);
                log.info("   ✅ STEP 5.5 DONE: GRN Status fetched successfully");
            } else {
                log.warn("   ❌ GRN not found with Reference Number: {}", grnRefNumber);
            }
        } else {
            log.warn("   ⚠️ STEP 5.5 SKIPPED: GRN Number is null or empty");
        }

        // STEP 6: If GRN Status is APPROVED, add GRN items to Stock Summary (Create New Entries Only)
        log.info("\n▶️ STEP 6: Processing Items from Add-Bundles Request for Stock Summary...");

        if (fetchedGrn != null && "APPROVED".equalsIgnoreCase(grnStatus)) {
            log.info("   ✅ GRN Status is APPROVED - Processing {} items from request...",
                    request.getItems() != null ? request.getItems().size() : 0);

            if (request.getItems() != null && !request.getItems().isEmpty()) {

                for (AddBundleRequestDto.BundleItemDto bundleItem : request.getItems()) {
                    log.info("\n   🔄 Processing Item from Request: {} (Category: {})",
                            bundleItem.getItemDescription(), bundleItem.getProductCategory());

                    // Get all details from request item
                    String itemDesc = bundleItem.getItemDescription();
                    String unit = fetchedGrn.getUnit();
                    String productCategory = bundleItem.getProductCategory();
                    String brand = bundleItem.getBrand();
                    String grade = bundleItem.getGrade();
                    String temper = bundleItem.getTemper();
                    String sectionNo = bundleItem.getSectionNumber();
                    BigDecimal itemPrice = bundleItem.getItemPrice();
                    // Get warehouse storage fields from request
                    String store = bundleItem.getCurrentStore() != null ? bundleItem.getCurrentStore() :
                                  (request.getStore() != null ? request.getStore() : "Warehouse");
                    String storageArea = bundleItem.getStorageArea() != null ? bundleItem.getStorageArea() :
                                        (request.getStorageArea() != null ? request.getStorageArea() : "Common");
                    String rackColumnBin = bundleItem.getRackColumnBinNumber() != null ? bundleItem.getRackColumnBinNumber() : "Common Bin";

                    log.info("      - Item Details:");
                    log.info("        - Description: {}", itemDesc);
                    log.info("        - Category: {}", productCategory);
                    log.info("        - Brand: {}", brand);
                    log.info("        - Grade: {}", grade);
                    log.info("        - Temper: {}", temper);
                    log.info("        - Unit: {}", unit);
                    log.info("        - Section: {}", sectionNo);
                    log.info("      - Warehouse Storage Details:");
                    log.info("        - Store: {}", store);
                    log.info("        - Storage Area: {}", storageArea);
                    log.info("        - Rack/Bin: {}", rackColumnBin);

                    // STEP 6.1: GRN-wise cleanup from Common Bin
                    log.info("      🔍 STEP 6.1: GRN-wise cleanup from Common Bin...");
                    log.info("         - GRN Number: '{}'", grnRefNumber);
                    log.info("         - Unit: '{}'", unit);
                    log.info("         - Item Description: '{}'", itemDesc);

                    // STEP 6.1.1: Find all entries with SAME UNIT + SAME ITEM DESC + COMMON BIN
                    log.info("         - Looking for entries with:");
                    log.info("           * Unit = '{}' (must match)", unit);
                    log.info("           * ItemDesc = '{}' (must match)", itemDesc);
                    log.info("           * Storage Area = 'Common Bin' (must be common bin)");

                    List<StockSummaryEntity> commonBinEntriesToUpdate = stockSummaryRepository.findAll().stream()
                            .filter(e ->
                                e.getUnit() != null && e.getUnit().equalsIgnoreCase(unit) &&
                                    e.getItemDescription() != null && e.getItemDescription().equalsIgnoreCase(itemDesc) &&
                                     e.getStore() != null && "Warehouse".equalsIgnoreCase(e.getStore()) &&
                                    (("Common Bin".equalsIgnoreCase(e.getStorageArea()) ||
                                            "COMMON_BIN".equalsIgnoreCase(e.getStorageArea()) ||
                                            "COMMON BIN".equalsIgnoreCase(e.getStorageArea())) ||
                                            "COMMON".equalsIgnoreCase(e.getStorageArea()) ||
                                            ("Common Bin".equalsIgnoreCase(e.getRackColumnShelfNumber()) ||
                                                    "COMMON_BIN".equalsIgnoreCase(e.getRackColumnShelfNumber()) ||
                                                    "Common bin".equalsIgnoreCase(e.getRackColumnShelfNumber()) ||
                                                    "common bin".equalsIgnoreCase(e.getRackColumnShelfNumber()))))
                            .collect(Collectors.toList());

                    log.info("         - Found {} entries matching criteria", commonBinEntriesToUpdate.size());

                    if (!commonBinEntriesToUpdate.isEmpty()) {
                        log.info("      ✂️ Removing GRN '{}' from {} Common Bin entry(ies)",
                                grnRefNumber, commonBinEntriesToUpdate.size());

                        for (StockSummaryEntity commonBinEntry : commonBinEntriesToUpdate) {
                            log.info("         🔄 Processing Entry ID: {}", commonBinEntry.getId());
                            log.info("            - Store: '{}', StorageArea: '{}', Rack: '{}'",
                                    commonBinEntry.getStore(),
                                    commonBinEntry.getStorageArea(),
                                    commonBinEntry.getRackColumnShelfNumber());

                            String existingGrnNumbersStr = commonBinEntry.getGrnNumbers();

                            if (existingGrnNumbersStr != null && !existingGrnNumbersStr.isEmpty()) {
                                try {
                                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    java.util.List<String> grnNumbersList = new java.util.ArrayList<>();

                                    log.info("            📋 RAW GRN Numbers from DB: '{}'", existingGrnNumbersStr);

                                    // Clean up corrupted format like "[], MEGRN2602010" or "[], GRN1, GRN2"
                                    String cleanedGrnStr = existingGrnNumbersStr.trim();

                                    // Remove leading "[]," or "[], " if present (corrupted format)
                                    if (cleanedGrnStr.startsWith("[],")) {
                                        cleanedGrnStr = cleanedGrnStr.substring(3).trim();
                                    log.info("            🔧 Cleaned corrupted format, remaining: '{}'", cleanedGrnStr);
                                    }

                                    // Check if it's valid JSON array format
                                    if (cleanedGrnStr.startsWith("[") && cleanedGrnStr.endsWith("]")) {
                                    log.info("            📋 GRN Array (JSON format) before removal: {}", cleanedGrnStr);
                                        try {
                                            grnNumbersList = mapper.readValue(cleanedGrnStr,
                                                mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                                        } catch (Exception jsonEx) {
                                        log.warn("            ⚠️ JSON parse failed, treating as plain text: {}", jsonEx.getMessage());
                                            // Fall back to plain text parsing
                                            String[] parts = cleanedGrnStr.replaceAll("[\\[\\]\"]", "").split(",");
                                            for (String part : parts) {
                                                String trimmed = part.trim();
                                                if (!trimmed.isEmpty()) {
                                                    grnNumbersList.add(trimmed);
                                                }
                                            }
                                        }
                                    } else if (!cleanedGrnStr.isEmpty() && !cleanedGrnStr.equals("[]")) {
                                        // Plain string format (comma-separated or single value)
                                    log.info("            📋 GRN String (plain format) before removal: {}", cleanedGrnStr);
                                        // Split by comma and trim each value
                                        String[] parts = cleanedGrnStr.split(",");
                                        for (String part : parts) {
                                            String trimmed = part.trim();
                                            if (!trimmed.isEmpty()) {
                                                grnNumbersList.add(trimmed);
                                            }
                                        }
                                    } else {
                                        log.info("            ℹ️ GRN Numbers is empty or '[]'");
                                    }

                                    log.info("            📋 Parsed GRN List: {}", grnNumbersList);

                                    // FIX: Always deduct quantity if GRN was ever in this Common Bin entry.
                                    // Don't gate deduction on grnNumbersList.contains() because the GRN gets
                                    // removed on the first bundle — subsequent bundles of the same item would
                                    // skip deduction entirely (bug: only first item was deducted).
                                    boolean grnExistsInList = grnNumbersList.contains(grnRefNumber);
                                    boolean hasRemainingQty = commonBinEntry.getQuantityKg() != null
                                            && commonBinEntry.getQuantityKg().compareTo(BigDecimal.ZERO) > 0;

                                    if (grnExistsInList || hasRemainingQty) {
                                        // DEDUCT QUANTITY from Common Bin (transfer to new rack)
                                    BigDecimal bundleQtyKg = bundleItem.getWeightmentQuantityKg() != null ? bundleItem.getWeightmentQuantityKg() : BigDecimal.ZERO;
                                    Integer bundleQtyNo = bundleItem.getWeightmentQuantityNo() != null ? bundleItem.getWeightmentQuantityNo() : 0;

                                    BigDecimal oldQtyKg = commonBinEntry.getQuantityKg() != null ? commonBinEntry.getQuantityKg() : BigDecimal.ZERO;
                                    Integer oldQtyNo = commonBinEntry.getQuantityNo() != null ? commonBinEntry.getQuantityNo() : 0;

                                        BigDecimal newQtyKg = oldQtyKg.subtract(bundleQtyKg);
                                        Integer newQtyNo = oldQtyNo - bundleQtyNo;

                                        // Ensure quantity doesn't go negative
                                        if (newQtyKg.compareTo(BigDecimal.ZERO) < 0) {
                                            newQtyKg = BigDecimal.ZERO;
                                        }
                                        if (newQtyNo < 0) {
                                            newQtyNo = 0;
                                        }

                                        commonBinEntry.setQuantityKg(newQtyKg);
                                        commonBinEntry.setQuantityNo(newQtyNo);
                                        commonBinEntry.setItemPrice(BigDecimal.ZERO);

                                        log.info("            📉 QUANTITY DEDUCTED from Common Bin:");
                                        log.info("               - OLD Qty: {} KG, {} NO", oldQtyKg, oldQtyNo);
                                        log.info("               - DEDUCTED: {} KG, {} NO", bundleQtyKg, bundleQtyNo);
                                        log.info("               - NEW Qty: {} KG, {} NO", newQtyKg, newQtyNo);

                                        // Only remove GRN from array when quantity reaches zero (all bundles
                                        // transferred)
                                        if (newQtyKg.compareTo(BigDecimal.ZERO) <= 0 && grnExistsInList) {
                                            grnNumbersList.remove(grnRefNumber);
                                            log.info(
                                                    "            ✅ Quantity reached zero — removed '{}' from GRN array",
                                                    grnRefNumber);
                                        }

                                        // Update the entry - save as JSON array format
                                        if (grnNumbersList.isEmpty() && newQtyKg.compareTo(BigDecimal.ZERO) <= 0) {
                                            commonBinEntry.setGrnNumbers("[]");
                                            log.info("            ⚠️ Array now empty: []");
                                        } else {
                                            String updatedGrnJson = mapper.writeValueAsString(grnNumbersList);
                                            commonBinEntry.setGrnNumbers(updatedGrnJson);
                                            log.info("            📋 GRN Array after update: {}", grnNumbersList);
                                        }

                                        stockSummaryRepository.save(commonBinEntry);
                                    log.info("            ✅ Entry saved to DB with updated quantity and GRN numbers");

                                        // DELETE ALL StockSummaryBundleEntity from this Common Bin entry
                                        log.info(
                                                "            🗑️ STEP 6.1.1: Deleting ALL StockSummaryBundleEntity from Common Bin for Entry ID: {}, Unit: '{}', Item: '{}'",
                                                commonBinEntry.getId(), unit, itemDesc);
                                        try {
                                            final Long targetStockSummaryId = commonBinEntry.getId();

                                            List<StockSummaryBundleEntity> commonBinBundles = stockSummaryBundleRepository
                                                    .findAll().stream()
                                                    .filter(bundle -> {
                                                        // Match by parent stock_summary_id — this is the Common Bin entry we already found
                                                        boolean belongsToCommonBin = bundle.getStockSummary() != null &&
                                                                bundle.getStockSummary().getId() != null &&
                                                                bundle.getStockSummary().getId().equals(targetStockSummaryId);

                                                        return belongsToCommonBin;
                                                    })
                                                    .collect(Collectors.toList());

                                            log.info("               🔍 Search criteria for deletion:");
                                            log.info("                  - stock_summary_id: {}", targetStockSummaryId);
                                            log.info("                  - (Common Bin Entry for Unit: '{}', Item: '{}')", unit, itemDesc);

                                            if (!commonBinBundles.isEmpty()) {
                                                log.info(
                                                        "               🔍 Found {} bundle(s) matching in Common Bin to delete",
                                                        commonBinBundles.size());
                                                for (StockSummaryBundleEntity bundleToDelete : commonBinBundles) {
                                                    log.info(
                                                            "               🗑️ Deleting Bundle ID: {} | GRN: {} | SlNo: {} | ItemDesc: {} | Qty: {} KG",
                                                            bundleToDelete.getId(), bundleToDelete.getGrnNumber(),
                                                            bundleToDelete.getSlNo(),
                                                            bundleToDelete.getItemDescription(),
                                                            bundleToDelete.getWeightmentQuantityKg());
                                                    stockSummaryBundleRepository.delete(bundleToDelete);
                                                }
                                                log.info("               ✅ Deleted {} Common Bin bundle(s)",
                                                        commonBinBundles.size());
                                            } else {
                                                log.info(
                                                        "               ℹ️ No bundles found for Common Bin Entry ID {} (may already be deleted or not created)",
                                                        targetStockSummaryId);
                                            }
                                        } catch (Exception deleteEx) {
                                        log.error("               ❌ Error deleting Common Bin bundles for GRN '{}': {}", 
                                                    grnRefNumber, deleteEx.getMessage());
                                        }
                                    } else {
                                        log.info(
                                                "            ℹ️ GRN '{}' not in array and Common Bin qty is already zero — skipping deduction",
                                                grnRefNumber);
                                    }
                                } catch (Exception e) {
                                    log.error("            ❌ Error processing GRN array: {}", e.getMessage());
                                    e.printStackTrace();
                                }
                            } else {
                                log.info("            ℹ️ No GRN numbers in this entry");
                            }
                        }
                    } else {
                        log.info("      ℹ️ No Common Bin entries found with Unit='{}' + ItemDesc='{}' (OK - may already be transferred)",
                                unit, itemDesc);
                    }

                    // Check if matching entry exists in Stock Summary with NEW rack/bin location
                    log.info("      🔍 STEP 6.2: Checking for matching entry in Stock Summary with new rack/bin...");
                    log.info("         🔎 SEARCH CRITERIA:");
                    log.info("            - ItemDescription: '{}'", itemDesc);
                    log.info("            - Unit: '{}'", unit);
                    log.info("            - RackColumnShelfNumber: '{}'", rackColumnBin);
                    log.info("            - Store: '{}'", store);
                    log.info("            - StorageArea: '{}'", storageArea);
                    log.info("            - ItemGroup: 'RAW MATERIAL' (must match)");

                    List<StockSummaryEntity> matchingEntries = stockSummaryRepository.findAll().stream()
                            .filter(e ->
                                itemDesc.equals(e.getItemDescription()) &&
                                    unit.equals(e.getUnit()) &&
                                    rackColumnBin.equals(e.getRackColumnShelfNumber()) &&
                                    store.equals(e.getStore()) &&
                                    storageArea.equals(e.getStorageArea()) &&
                                "RAW MATERIAL".equalsIgnoreCase(e.getItemGroup())
                            )
                            .collect(Collectors.toList());

                    log.info("         📊 MATCHING ENTRIES FOUND (with RAW MATERIAL): {}", matchingEntries.size());
                    for (StockSummaryEntity entry : matchingEntries) {
                        log.info("            ▶️ Entry ID: {}", entry.getId());
                        log.info("               - ItemDescription: '{}'", entry.getItemDescription());
                        log.info("               - Unit: '{}'", entry.getUnit());
                        log.info("               - Store: '{}'", entry.getStore());
                        log.info("               - StorageArea: '{}'", entry.getStorageArea());
                        log.info("               - RackColumnShelfNumber: '{}'", entry.getRackColumnShelfNumber());
                        log.info("               - ItemGroup: '{}'", entry.getItemGroup());
                        log.info("               - Current QuantityKg: {}", entry.getQuantityKg());
                        log.info("               - Current QuantityNo: {}", entry.getQuantityNo());
                        log.info("               - EXISTING GRN Numbers: '{}'", entry.getGrnNumbers());
                    }

                    if (!matchingEntries.isEmpty()) {
                        // Update existing entry
                        log.info("      ✏️ Found {} matching entry(s) - UPDATING quantities and GRN numbers", matchingEntries.size());

                        for (StockSummaryEntity existingEntry : matchingEntries) {
                            log.info("         🔄 UPDATING Entry ID: {}", existingEntry.getId());
                            log.info("            - OLD QuantityKg: {}", existingEntry.getQuantityKg());
                            log.info("            - OLD QuantityNo: {}", existingEntry.getQuantityNo());
                            log.info("            - OLD ItemPrice: {}", existingEntry.getItemPrice());
                            log.info("            - OLD GRN Numbers: '{}'", existingEntry.getGrnNumbers());
                            log.info("            - ADDING QuantityKg: {}", bundleItem.getWeightmentQuantityKg());
                            log.info("            - ADDING QuantityNo: {}", bundleItem.getWeightmentQuantityNo());
                            log.info("            - ADDING ItemPrice: {}", bundleItem.getItemPrice());
                            log.info("            - ADDING GRN Number: '{}'", grnRefNumber);

                            // Get OLD quantities BEFORE updating (needed for weighted average price calculation)
                            BigDecimal oldQtyKg = existingEntry.getQuantityKg() != null ? existingEntry.getQuantityKg() : BigDecimal.ZERO;
                            Integer oldQtyNo = existingEntry.getQuantityNo() != null ? existingEntry.getQuantityNo() : 0;
                            BigDecimal oldItemPrice = existingEntry.getItemPrice() != null ? existingEntry.getItemPrice() : BigDecimal.ZERO;

                            // Get NEW bundle quantities and price
                            BigDecimal bundleQtyKg = bundleItem.getWeightmentQuantityKg() != null ? bundleItem.getWeightmentQuantityKg() : BigDecimal.ZERO;
                            Integer bundleQtyNo = bundleItem.getWeightmentQuantityNo() != null ? bundleItem.getWeightmentQuantityNo() : 0;
                            BigDecimal bundlePrice = bundleItem.getItemPrice() != null ? bundleItem.getItemPrice() : BigDecimal.ZERO;

                            // Calculate NEW total quantities
                            BigDecimal newQtyKg = oldQtyKg.add(bundleQtyKg);
                            Integer newQtyNo = oldQtyNo + bundleQtyNo;

                            // Update quantities
                            existingEntry.setQuantityKg(newQtyKg);
                            existingEntry.setQuantityNo(newQtyNo);

                            // Set itemGroup to RAW MATERIAL if not already set
                            if (existingEntry.getItemGroup() == null || existingEntry.getItemGroup().isEmpty()) {
                                existingEntry.setItemGroup("RAW MATERIAL");
                                log.info("            📦 ItemGroup set to: RAW MATERIAL");
                            }

                            // Calculate weighted average price: (oldPrice * oldQty + newPrice * newQty) / totalQty
                            if (newQtyKg.compareTo(BigDecimal.ZERO) > 0) {
                                BigDecimal oldTotalValue = oldItemPrice.multiply(oldQtyKg);
                                BigDecimal newTotalValue = bundlePrice.multiply(bundleQtyKg);
                                BigDecimal combinedTotalValue = oldTotalValue.add(newTotalValue);
                                BigDecimal avgPrice = combinedTotalValue.divide(newQtyKg, 2, java.math.RoundingMode.HALF_UP);
                                existingEntry.setItemPrice(avgPrice);
                                log.info("            💰 PRICE CALCULATION:");
                                log.info("               - Old Total Value: {} * {} = {}", oldItemPrice, oldQtyKg, oldTotalValue);
                                log.info("               - New Bundle Value: {} * {} = {}", bundlePrice, bundleQtyKg, newTotalValue);
                                log.info("               - Combined Total Value: {}", combinedTotalValue);
                                log.info("               - NEW Average Price: {} / {} = {}", combinedTotalValue, newQtyKg, avgPrice);
                            }

                            // Add GRN number (handle multiple GRN numbers as JSON string in TEXT)
                            String existingGrnNumbersStr = existingEntry.getGrnNumbers();
                            log.info("        📋 EXISTING GRN Numbers in DB (before update): '{}'", existingGrnNumbersStr);
                            java.util.List<String> grnNumbersList = new java.util.ArrayList<>();

                            if (existingGrnNumbersStr != null && !existingGrnNumbersStr.isEmpty()) {
                                // Parse existing JSON string to list
                                try {
                                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                    grnNumbersList = mapper.readValue(existingGrnNumbersStr,
                                            mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                                    log.info("            ✅ Parsed existing GRN list: {}", grnNumbersList);
                                } catch (Exception e) {
                                    log.error("            ❌ Error parsing GRN numbers: {}", e.getMessage());
                                }
                            } else {
                                log.info("            ℹ️ No existing GRN numbers - starting fresh list");
                            }

                            log.info("            🔍 Checking if '{}' already in list: {}", grnRefNumber, grnNumbersList.contains(grnRefNumber));
                            if (!grnNumbersList.contains(grnRefNumber)) {
                                grnNumbersList.add(grnRefNumber);
                                log.info("            ➕ Added '{}' to GRN list", grnRefNumber);
                            } else {
                                log.info("            ⏭️ '{}' already exists in list - SKIPPING", grnRefNumber);
                            }

                            // Convert back to JSON string
                            try {
                                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                                String grnNumbersJson = mapper.writeValueAsString(grnNumbersList);
                                existingEntry.setGrnNumbers(grnNumbersJson);
                                log.info("            ✅ FINAL GRN Numbers to save: {}", grnNumbersJson);
                            } catch (Exception e) {
                                log.error("            ❌ Error converting GRN numbers to JSON: {}", e.getMessage());
                            }

                            stockSummaryRepository.save(existingEntry);
                            log.info("      💾 SAVED Entry ID: {} with NEW QuantityKg: {}, NEW QuantityNo: {}, GRN Numbers: '{}'",
                                    existingEntry.getId(), existingEntry.getQuantityKg(), existingEntry.getQuantityNo(), existingEntry.getGrnNumbers());

                            // STEP 6.2.1: Save complete GRN bundle data to StockSummaryBundleEntity
                            log.info("      📦 STEP 6.2.1: Saving complete GRN bundle data to StockSummaryBundleEntity...");

                            // Fetch heatNo, lotNo, testCertificate from GRN Items
                            String heatNo = "";
                            String lotNo = "";
                            String testCertificate = "";
                            if (fetchedGrn != null && fetchedGrn.getGrnItems() != null && !fetchedGrn.getGrnItems().isEmpty()) {
                                // Try to find matching item by itemDescription, else use first item
                                GRNItemEntity matchingGrnItem = fetchedGrn.getGrnItems().stream()
                                        .filter(grnItem -> grnItem.getItemDescription() != null &&
                                                grnItem.getItemDescription().equalsIgnoreCase(bundleItem.getItemDescription()))
                                        .findFirst()
                                        .orElse(fetchedGrn.getGrnItems().get(0));

                                heatNo = matchingGrnItem.getHeatNumber() != null ? matchingGrnItem.getHeatNumber() : "";
                                lotNo = matchingGrnItem.getLotNumber() != null ? matchingGrnItem.getLotNumber() : "";
                                testCertificate = matchingGrnItem.getTestCertificateNumber() != null ? matchingGrnItem.getTestCertificateNumber() : "";
                                log.info("      📋 GRN Item Details - HeatNo: {}, LotNo: {}, TestCertificate: {}", heatNo, lotNo, testCertificate);
                            }

                            StockSummaryBundleEntity bundleEntity = StockSummaryBundleEntity.builder()
                                    .stockSummary(existingEntry)
                                    .grnNumber(grnRefNumber)
                                    .grnId(request.getGrnId())
                                    .stockTransferId(request.getStockTransferId())
                                    .transferNumber(request.getTransferNumber())
                                    .transferType(request.getTransferType())
                                    .slNo(bundleItem.getSlNo())
                                    .itemDescription(bundleItem.getItemDescription())
                                    .productCategory(bundleItem.getProductCategory())
                                    .sectionNumber(bundleItem.getSectionNumber())
                                    .brand(bundleItem.getBrand())
                                    .grade(bundleItem.getGrade())
                                    .temper(bundleItem.getTemper())
                                    .dimension(bundleItem.getDimension())
                                    .weighment(bundleItem.getWeighment())
                                    .weightmentQuantityKg(bundleItem.getWeightmentQuantityKg())
                                    .uomNetWeight(bundleItem.getUomNetWeight())
                                    .weightmentQuantityNo(bundleItem.getWeightmentQuantityNo())
                                    .uomNo(bundleItem.getUomNo())
                                    .materialAcceptance(bundleItem.getMaterialAcceptance())
                                    .currentStore(bundleItem.getCurrentStore())
                                    .recipientStore(bundleItem.getRecipientStore())
                                    .storageArea(bundleItem.getStorageArea())
                                    .rackColumnBinNumber(bundleItem.getRackColumnBinNumber())
                                    .rackStatus(bundleItem.getRackStatus())
                                    .qrCodeUrl(bundleItem.getQrCodeUrl())
                                    .poNumber(bundleItem.getPoNumber())
                                    .heatNo(heatNo)
                                    .lotNo(lotNo)
                                    .testCertificate(testCertificate)
                                    .userId(request.getUserId())
                                    .unitId(request.getUnitId())
                                    .status("ADDED")
                                    .createdBy(request.getCreatedBy())
                                    .itemPrice(itemPrice)
                                    .build();
                            stockSummaryBundleRepository.save(bundleEntity);
                            log.info("      ✅ GRN Bundle saved with complete data - GRN: {}, SlNo: {}, Qty: {} KG",
                                    grnRefNumber, bundleItem.getSlNo(), bundleItem.getWeightmentQuantityKg());

                            log.info("      ✅ Updated entry:");
                            log.info("        - New Qty Kg: {}", newQtyKg);
                            log.info("        - New Qty No: {}", newQtyNo);
                            log.info("        - New Item Price: {}", existingEntry.getItemPrice());
                            log.info("        - GRN Numbers: {}", existingEntry.getGrnNumbers());
                        }
                    } else {
                        // Create new entry
                        log.info("      ➕ STEP 6.3: No matching entry found - Creating NEW Stock Summary entry");
                        log.info("         📝 NEW ENTRY DETAILS:");
                        log.info("            - Unit: '{}'", unit);
                        log.info("            - Store: '{}'", store);
                        log.info("            - StorageArea: '{}'", storageArea);
                        log.info("            - RackColumnBin: '{}'", rackColumnBin);
                        log.info("            - ItemDescription: '{}'", itemDesc);
                        log.info("            - ProductCategory: '{}'", productCategory);
                        log.info("            - Brand: '{}'", brand);
                        log.info("            - Grade: '{}'", grade);
                        log.info("            - Temper: '{}'", temper);
                        log.info("            - QuantityKg: {}", bundleItem.getWeightmentQuantityKg());
                        log.info("            - QuantityNo: {}", bundleItem.getWeightmentQuantityNo());
                        log.info("            - GRN Number to add: '{}'", grnRefNumber);

                        // Fetch materialType from ItemMaster by itemDescription
                        String materialTypeFromItemMaster = null;
                        if (itemDesc != null && !itemDesc.isEmpty()) {
                            ItemMasterEntity itemMaster = itemMasterRepository.findBySkuDescriptionIgnoreCase(itemDesc).orElse(null);
                            if (itemMaster != null && itemMaster.getMaterialType() != null) {
                                materialTypeFromItemMaster = itemMaster.getMaterialType();
                                log.info("            ✅ MaterialType fetched from ItemMaster: '{}'", materialTypeFromItemMaster);
                            } else {
                                log.warn("            ⚠️ ItemMaster not found or materialType is null for itemDescription: '{}', using productCategory as fallback", itemDesc);
                                materialTypeFromItemMaster = productCategory;
                            }
                        } else {
                            log.warn("            ⚠️ ItemDescription is null or empty, using productCategory as fallback for materialType");
                            materialTypeFromItemMaster = productCategory;
                        }

                        // Convert GRN numbers to JSON string
                        String grnNumbersJson = "[]";
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            grnNumbersJson = mapper.writeValueAsString(java.util.Arrays.asList(grnRefNumber));
                            log.info("            ✅ GRN Numbers JSON created: {}", grnNumbersJson);
                        } catch (Exception e) {
                            log.error("            ❌ Error converting GRN numbers to JSON: {}", e.getMessage());
                        }

                        StockSummaryEntity newEntry = StockSummaryEntity.builder()
                                .unit(unit)
                                .store(store)
                                .storageArea(storageArea)
                                .rackColumnShelfNumber(rackColumnBin)
                                .productCategory(productCategory)
                                .itemDescription(itemDesc)
                                .brand(brand)
                                .grade(grade)
                                .temper(temper)
                                .sectionNo(sectionNo)
                                .dimension(bundleItem.getDimension())
                                .quantityKg(bundleItem.getWeightmentQuantityKg() != null ? bundleItem.getWeightmentQuantityKg() : BigDecimal.ZERO)
                                .quantityNo(bundleItem.getWeightmentQuantityNo() != null ? bundleItem.getWeightmentQuantityNo() : 0)
                                .itemPrice(itemPrice)
                                .materialType(materialTypeFromItemMaster)
                                .itemGroup("RAW MATERIAL")
                                .reprintQr(false)
                                .pickListLocked(false)
                                .grnNumbers(grnNumbersJson)
                                .build();

                        log.info("      - New Stock Summary Entry Details:");
                        log.info("        - Store: {}", store);
                        log.info("        - StorageArea: {}", storageArea);
                        log.info("        - RackColumnBin: {}", rackColumnBin);
                        log.info("        - GRN Numbers JSON: {}", grnNumbersJson);
                        log.info("        - ItemGroup: RAW MATERIAL");
                        log.info("        - Item: {} | Brand: {} | Grade: {} | Temper: {}",
                                itemDesc, brand, grade, temper);
                        log.info("        - Qty Kg: {}, Qty No: {}", newEntry.getQuantityKg(), newEntry.getQuantityNo());

                        StockSummaryEntity savedEntry = stockSummaryRepository.save(newEntry);
                        log.info("      ✅ New Stock Summary entry created with GRN numbers: {}", grnNumbersJson);

                        // STEP 6.3.1: Save complete GRN bundle data to StockSummaryBundleEntity for new entry
                        log.info("      📦 STEP 6.3.1: Saving complete GRN bundle data to StockSummaryBundleEntity...");

                        // Fetch heatNo, lotNo, testCertificate from GRN Items
                        String heatNoNew = "";
                        String lotNoNew = "";
                        String testCertificateNew = "";
                        if (fetchedGrn != null && fetchedGrn.getGrnItems() != null && !fetchedGrn.getGrnItems().isEmpty()) {
                            // Try to find matching item by itemDescription, else use first item
                            GRNItemEntity matchingGrnItemNew = fetchedGrn.getGrnItems().stream()
                                    .filter(grnItem -> grnItem.getItemDescription() != null &&
                                            grnItem.getItemDescription().equalsIgnoreCase(bundleItem.getItemDescription()))
                                    .findFirst()
                                    .orElse(fetchedGrn.getGrnItems().get(0));

                            heatNoNew = matchingGrnItemNew.getHeatNumber() != null ? matchingGrnItemNew.getHeatNumber() : "";
                            lotNoNew = matchingGrnItemNew.getLotNumber() != null ? matchingGrnItemNew.getLotNumber() : "";
                            testCertificateNew = matchingGrnItemNew.getTestCertificateNumber() != null ? matchingGrnItemNew.getTestCertificateNumber() : "";
                            log.info("      📋 GRN Item Details - HeatNo: {}, LotNo: {}, TestCertificate: {}", heatNoNew, lotNoNew, testCertificateNew);
                        }

                        StockSummaryBundleEntity bundleEntity = StockSummaryBundleEntity.builder()
                                .stockSummary(savedEntry)
                                .grnNumber(grnRefNumber)
                                .grnId(request.getGrnId())
                                .stockTransferId(request.getStockTransferId())
                                .transferNumber(request.getTransferNumber())
                                .transferType(request.getTransferType())
                                .slNo(bundleItem.getSlNo())
                                .itemDescription(bundleItem.getItemDescription())
                                .productCategory(bundleItem.getProductCategory())
                                .sectionNumber(bundleItem.getSectionNumber())
                                .brand(bundleItem.getBrand())
                                .grade(bundleItem.getGrade())
                                .temper(bundleItem.getTemper())
                                .dimension(bundleItem.getDimension())
                                .weighment(bundleItem.getWeighment())
                                .weightmentQuantityKg(bundleItem.getWeightmentQuantityKg())
                                .uomNetWeight(bundleItem.getUomNetWeight())
                                .weightmentQuantityNo(bundleItem.getWeightmentQuantityNo())
                                .uomNo(bundleItem.getUomNo())
                                .materialAcceptance(bundleItem.getMaterialAcceptance())
                                .currentStore(bundleItem.getCurrentStore())
                                .recipientStore(bundleItem.getRecipientStore())
                                .storageArea(bundleItem.getStorageArea())
                                .rackColumnBinNumber(bundleItem.getRackColumnBinNumber())
                                .rackStatus(bundleItem.getRackStatus())
                                .qrCodeUrl(bundleItem.getQrCodeUrl())
                                .poNumber(bundleItem.getPoNumber())
                                .heatNo(heatNoNew)
                                .lotNo(lotNoNew)
                                .testCertificate(testCertificateNew)
                                .userId(request.getUserId())
                                .unitId(request.getUnitId())
                                .status("ADDED")
                                .createdBy(request.getCreatedBy())
                                .itemPrice(itemPrice)
                                .build();
                        stockSummaryBundleRepository.save(bundleEntity);
                        log.info("      ✅ GRN Bundle saved with complete data - GRN: {}, SlNo: {}, Qty: {} KG",
                                grnRefNumber, bundleItem.getSlNo(), bundleItem.getWeightmentQuantityKg());
                    }
                }

                log.info("\n   ✅ STEP 6 DONE: Processed {} items (updated existing or created new entries)",
                        request.getItems().size());
            } else {
                log.warn("   ⚠️ Add-bundles request has no items to process");
            }
        } else if (fetchedGrn != null) {
            log.warn("   ⚠️ GRN Status is {} (not APPROVED) - Skipping Stock Summary creation", grnStatus);
        } else {
            log.warn("   ⚠️ STEP 6 SKIPPED: GRN not found");
        }

        return saved.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GenerateQrResponseDto generateQrCode(GenerateQrRequestDto request) {
        GrnLineItemEntity lineItem = repository.findById(request.getLineItemId())
                .orElseThrow(() -> new RuntimeException("Line item not found with ID: " + request.getLineItemId()));

        // Update line item with user-provided weighment and quantity data
        if (request.getWeighment() != null) {
            lineItem.setWeighment(request.getWeighment());
        }
        if (request.getWeightmentQuantityKg() != null) {
            lineItem.setWeightmentQuantityKg(request.getWeightmentQuantityKg());
        }
        if (request.getUomNetWeight() != null) {
            lineItem.setUomNetWeight(request.getUomNetWeight());
        }
        if (request.getWeightmentQuantityNo() != null) {
            lineItem.setWeightmentQuantityNo(request.getWeightmentQuantityNo());
        }
        if (request.getUomNo() != null) {
            lineItem.setUomNo(request.getUomNo());
        }

        // Generate QR code data with updated item information
        String qrData = generateQrData(lineItem);

        // Generate QR code image
        String qrCodeImageUrl = qrCodeGenerator.generateQrCodeImage(qrData);

        // Update line item with QR details
        lineItem.setQrCode(qrData);
        lineItem.setQrCodeImageUrl(qrCodeImageUrl);
        lineItem.setQrGenerated(true);
        lineItem.setStatus("QR_GENERATED");

        GrnLineItemEntity updated = repository.save(lineItem);

        return GenerateQrResponseDto.builder()
                .lineItemId(updated.getId())
                .qrCode(updated.getQrCode())
                .qrCodeImageUrl(updated.getQrCodeImageUrl())
                .success(true)
                .message("QR code generated successfully")
                .build();
    }

    @Override
    public List<GrnLineItemDto> getLineItemsByGrnNumber(String grnNumber) {
        return repository.findByGrnNumber(grnNumber).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GrnLineItemDto> getLineItemsByTransferNumber(String transferNumber) {
        return repository.findByTransferNumber(transferNumber).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public GrnLineItemDto getLineItemById(Long id) {
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Line item not found with ID: " + id));
    }

    @Override
    @Transactional
    public GrnLineItemDto updateLineItem(Long id, GrnLineItemDto dto) {
        GrnLineItemEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Line item not found with ID: " + id));

        entity.setItemDescription(dto.getItemDescription());
        entity.setProductCategory(dto.getProductCategory());
        entity.setBrand(dto.getBrand());
        entity.setGrade(dto.getGrade());
        entity.setTemper(dto.getTemper());
        entity.setWeighment(dto.getWeighment());
        entity.setWeightmentQuantityKg(dto.getWeightmentQuantityKg());
        entity.setUomNetWeight(dto.getUomNetWeight());
        entity.setWeightmentQuantityNo(dto.getWeightmentQuantityNo());
        entity.setUomNo(dto.getUomNo());
        // Warehouse Storage Fields (per-bundle)
        entity.setCurrentStore(dto.getCurrentStore());
        entity.setMaterialAcceptance(dto.getMaterialAcceptance());
        entity.setRecipientStore(dto.getRecipientStore());
        entity.setStorageArea(dto.getStorageArea());
        entity.setRackColumnBinNumber(dto.getRackColumnBinNumber());
        entity.setUpdatedBy(dto.getUpdatedBy());

        GrnLineItemEntity updated = repository.save(entity);
        return mapToDto(updated);
    }

    @Override
    @Transactional
    public void deleteLineItem(Long id) {
        repository.deleteById(id);
    }

    @Override
    public List<GrnLineItemDto> getLineItemsWithQrGenerated() {
        return repository.findByQrGeneratedTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<GrnLineItemDto> getLineItemsByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private String generateQrData(GrnLineItemEntity lineItem) {
        return String.format(
                "GRN:%s|TRANSFER:%s|ITEM:%s|BRAND:%s|GRADE:%s|QTY_KG:%s|QTY_NO:%s|UOM_NET_WEIGHT:%s|UOM_NO:%s",
                lineItem.getGrnNumber(),
                lineItem.getTransferNumber(),
                lineItem.getItemDescription(),
                lineItem.getBrand(),
                lineItem.getGrade(),
                lineItem.getWeightmentQuantityKg(),
                lineItem.getWeightmentQuantityNo(),
                lineItem.getUomNetWeight(),
                lineItem.getUomNo()
        );
    }

    private GrnLineItemDto mapToDto(GrnLineItemEntity entity) {
        // Fetch transferType from StockTransferEntity if available
        String transferType = null;
        if (entity.getStockTransferId() != null) {
            transferType = stockTransferRepository.findById(entity.getStockTransferId())
                    .map(StockTransferEntity::getTransferType)
                    .orElse(null);
        }

        return GrnLineItemDto.builder()
                .id(entity.getId())
                .grnNumber(entity.getGrnNumber())
                .grnId(entity.getGrnId())
                .stockTransferId(entity.getStockTransferId())
                .transferNumber(entity.getTransferNumber())
                .transferType(transferType)
                .slNo(entity.getSlNo())
                .itemDescription(entity.getItemDescription())
                .productCategory(entity.getProductCategory())
                .sectionNumber(entity.getSectionNumber())
                .brand(entity.getBrand())
                .grade(entity.getGrade())
                .temper(entity.getTemper())
                .dimension(entity.getDimension())
                .weighment(entity.getWeighment())
                .weightmentQuantityKg(entity.getWeightmentQuantityKg())
                .uomNetWeight(entity.getUomNetWeight())
                .weightmentQuantityNo(entity.getWeightmentQuantityNo())
                .uomNo(entity.getUomNo())
                // Warehouse Storage Fields (per-bundle)
                .currentStore(entity.getCurrentStore())
                .recipientStore(entity.getRecipientStore())
                .storageArea(entity.getStorageArea())
                .rackColumnBinNumber(entity.getRackColumnBinNumber())
                .rackStatus(entity.getRackStatus())
                .poNumber(entity.getPoNumber())
                .qrCode(entity.getQrCode())
                .qrCodeImageUrl(entity.getQrCodeImageUrl())
                .qrGenerated(entity.getQrGenerated())
                .status(entity.getStatus())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .updatedBy(entity.getUpdatedBy())
                .updatedDate(entity.getUpdatedDate())
                .build();
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getItemSummaryByUnitAndDescription(String unit, String itemDescription) {
        log.info("📊 Fetching Item Summary by Unit and ItemDescription...");
        log.info("   - Unit: '{}'", unit);
        log.info("   - ItemDescription: '{}'", itemDescription);

        // STEP 1: Find all stock summary entries matching unit + itemDescription
        log.info("\n🔍 STEP 1: Finding Stock Summary entries for all storage locations...");
        List<StockSummaryEntity> stockEntries = stockSummaryRepository.findAll().stream()
                .filter(stock ->
                    stock.getUnit() != null && stock.getUnit().equalsIgnoreCase(unit) &&
                    stock.getItemDescription() != null && stock.getItemDescription().equalsIgnoreCase(itemDescription)
                )
                .toList();

        log.info("   ✅ Found {} stock summary entries (all locations)", stockEntries.size());

        if (stockEntries.isEmpty()) {
            log.warn("   ⚠️ No stock entries found for unit: {} and item: {}", unit, itemDescription);
            return java.util.List.of();
        }

        // STEP 2: Process each stock summary entry as a separate location
        log.info("\n📊 STEP 2: Processing each location separately...");
        java.util.List<java.util.Map<String, Object>> results = new java.util.ArrayList<>();

        for (StockSummaryEntity stock : stockEntries) {
            String store = stock.getStore() != null ? stock.getStore() : "N/A";
            String storageArea = stock.getStorageArea() != null ? stock.getStorageArea() : "N/A";

            log.info("   📍 Processing Location: Store='{}', Area='{}', ID={}", store, storageArea, stock.getId());

            // STEP 2.1: Extract GRN numbers from this stock entry
            log.info("      🔍 Extracting GRN numbers...");
            java.util.Set<String> locationGrnNumbers = new java.util.LinkedHashSet<>();

            if (stock.getGrnNumbers() != null && !stock.getGrnNumbers().isEmpty()) {
                try {
                    String grnStr = stock.getGrnNumbers().trim();
                    log.info("      📋 GRN String: {}", grnStr);

                    // Parse JSON or plain string
                    if (grnStr.startsWith("[")) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        java.util.List<String> grnList = mapper.readValue(grnStr,
                                mapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class));
                        locationGrnNumbers.addAll(grnList);
                    } else {
                        // Plain string format (comma-separated)
                        String[] parts = grnStr.split(",");
                        for (String part : parts) {
                            String trimmed = part.trim();
                            if (!trimmed.isEmpty()) {
                                locationGrnNumbers.add(trimmed);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("      ⚠️ Error parsing GRN: {}", e.getMessage());
                    locationGrnNumbers.add(stock.getGrnNumbers());
                }
            }

            log.info("      ✅ Extracted {} GRN numbers: {}", locationGrnNumbers.size(), locationGrnNumbers);

            // STEP 2.2: Find matching line items for these GRN numbers
            log.info("      🔍 Finding line items for these GRNs...");
            List<GrnLineItemEntity> matchingLineItems = repository.findAll().stream()
                    .filter(item ->
                        item.getGrnNumber() != null && locationGrnNumbers.contains(item.getGrnNumber()) &&
                        item.getItemDescription() != null && item.getItemDescription().equalsIgnoreCase(itemDescription)
                    )
                    .toList();

            log.info("      ✅ Found {} matching line items", matchingLineItems.size());

            // STEP 2.3: Calculate totals for this location
            // If afterStockTransferQtyKg/No has data, use that, otherwise use original weightmentQuantityKg/No
            java.math.BigDecimal totalQtyKg = matchingLineItems.stream()
                    .map(item -> {
                        if (item.getAfterStockTransferQtyKg() != null) {
                            return item.getAfterStockTransferQtyKg();
                        }
                        return item.getWeightmentQuantityKg() != null ? item.getWeightmentQuantityKg() : java.math.BigDecimal.ZERO;
                    })
                    .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

            Integer totalQtyNo = matchingLineItems.stream()
                    .map(item -> {
                        if (item.getAfterStockTransferQtyNo() != null) {
                            return item.getAfterStockTransferQtyNo();
                        }
                        return item.getWeightmentQuantityNo() != null ? item.getWeightmentQuantityNo() : 0;
                    })
                    .reduce(0, Integer::sum);

            // Get price from GRN Summary's GRNItemEntity (where itemDescription matches)
            // Collect rates from ALL GRNs and calculate average
            java.util.List<java.math.BigDecimal> allRates = new java.util.ArrayList<>();
            log.info("      🔍 Fetching prices from all GRN Summaries for item: {}", itemDescription);

            for (String grnNumber : locationGrnNumbers) {
                var grnOpt = grnRepository.findByGrnRefNumber(grnNumber);
                if (grnOpt.isPresent()) {
                    var grn = grnOpt.get();
                    if (grn.getGrnItems() != null) {
                        for (var grnItem : grn.getGrnItems()) {
                            if (grnItem.getItemDescription() != null &&
                                    grnItem.getItemDescription().equalsIgnoreCase(itemDescription)) {
                                if (grnItem.getRate() != null && grnItem.getRate() > 0) {
                                    java.math.BigDecimal rate = java.math.BigDecimal.valueOf(grnItem.getRate());
                                    allRates.add(rate);
                                    log.info("      ✅ Found rate {} from GRN {} for item {}",
                                            rate, grnNumber, itemDescription);
                                }
                            }
                        }
                    }
                }
            }

            // Calculate average price from all collected rates
            java.math.BigDecimal averagePrice = java.math.BigDecimal.ZERO;
            if (!allRates.isEmpty()) {
                java.math.BigDecimal sum = allRates.stream()
                        .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
                averagePrice = sum.divide(java.math.BigDecimal.valueOf(allRates.size()), 2, java.math.RoundingMode.HALF_UP);
                log.info("      📊 Calculated Average Price: {} (from {} GRNs)", averagePrice, allRates.size());
            } else {
                log.warn("      ⚠️ No price found in any GRN Summary for item: {}", itemDescription);
            }

            log.info("      📊 Totals: Qty KG={}, Qty No={}, Price={}", totalQtyKg, totalQtyNo, averagePrice);

            // STEP 2.4: Build result map for this location
            java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("store", store);
            result.put("storageArea", storageArea);
            result.put("totalQuantityKg", totalQtyKg);
            result.put("totalQuantityNo", totalQtyNo);
            result.put("averagePrice", averagePrice);
            result.put("grnNumbers", new java.util.ArrayList<>(locationGrnNumbers));
            result.put("itemCount", matchingLineItems.size());

            results.add(result);
        }

        log.info("\n✅ Summary: Found {} location(s) for unit='{}', item='{}'",
                results.size(), unit, itemDescription);

        return results;
    }
}

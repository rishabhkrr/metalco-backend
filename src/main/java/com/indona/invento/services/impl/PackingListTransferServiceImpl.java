package com.indona.invento.services.impl;

import com.indona.invento.dao.PackingListTransferRepository;
import com.indona.invento.dao.PackingSubmissionRepository;
import com.indona.invento.dao.StockSummaryRepository;
import com.indona.invento.dto.PackingListTransferDTO;
import com.indona.invento.dto.RfdListSummaryDTO;
import com.indona.invento.dto.RfdListItemDTO;
import com.indona.invento.dto.RfdListBatchDTO;
import com.indona.invento.entities.PackingListTransferEntity;
import com.indona.invento.entities.PackingSubmission;
import com.indona.invento.entities.StockSummaryEntity;
import com.indona.invento.services.PackingListTransferService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PackingListTransferServiceImpl implements PackingListTransferService {

    @Autowired
    private PackingListTransferRepository repository;

    @Autowired
    private PackingSubmissionRepository packingSubmissionRepository;

    @Autowired
    private StockSummaryRepository stockSummaryRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public List<PackingListTransferEntity> savePackingList(List<PackingListTransferDTO> dtos) {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║        📋 savePackingList() CALLED (RFD List Transfer)      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println("   Total DTOs received: " + dtos.size());
        System.out.println("════════════════════════════════════════════════════════════\n");

        // Generate SINGLE RFD List number for ALL entries in this batch
        LocalDateTime batchTimestamp = LocalDateTime.now();
        String rfdListNumber = "MERFD" +
                batchTimestamp.format(DateTimeFormatter.ofPattern("ddMMyy")) +
                batchTimestamp.format(DateTimeFormatter.ofPattern("HHmmss"));

        System.out.println("🔑 Generated RFD LIST NUMBER: " + rfdListNumber);

        List<PackingListTransferEntity> savedEntities = dtos.stream().map(dto -> {
            String logPrefix = "📦 [SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber() + "]";
            System.out.println(logPrefix + " Processing...");

            // Check if SO+Line combination already exists
            PackingListTransferEntity existingBySOLine = repository.findBySoNumberAndLineNumber(
                    dto.getSoNumber(), dto.getLineNumber());
            if (existingBySOLine != null) {
                System.out.println(logPrefix + " ⚠️  DUPLICATE SO+LINE DETECTED! Skipping.");
                return existingBySOLine;
            }

            // Look up packing status
            PackingSubmission submission = packingSubmissionRepository
                    .findBySoNumberAndLineNumber(dto.getSoNumber(), dto.getLineNumber());
            String packingStatus = submission != null ? submission.getPackingStatus() : null;

            PackingListTransferEntity entity = PackingListTransferEntity.builder()
                    .timestamp(batchTimestamp)
                    .packingListNumber(rfdListNumber)
                    .transferType(dto.getTransferType())
                    .soNumber(dto.getSoNumber())
                    .lineNumber(dto.getLineNumber())
                    .unit(dto.getUnit())
                    .customerCode(dto.getCustomerCode())
                    .customerName(dto.getCustomerName())
                    .packingStatus(packingStatus)
                    .orderType(dto.getOrderType())
                    .productCategory(dto.getProductCategory())
                    .itemDescription(dto.getItemDescription())
                    .brand(dto.getBrand())
                    .grade(dto.getGrade())
                    .temper(dto.getTemper())
                    .dimension(dto.getDimension())
                    .quantityKg(dto.getQuantityKg())
                    .uomKg(dto.getUomKg())
                    .quantityNo(dto.getQuantityNo())
                    .uomNo(dto.getUomNo())
                    .transferStatus("Completed")
                    // Customer & Dispatch details
                    .customerBillingAddress(dto.getCustomerBillingAddress())
                    .customerShippingAddress(dto.getCustomerShippingAddress())
                    .customerPoNumber(dto.getCustomerPoNumber())
                    .customerPoDate(dto.getCustomerPoDate())
                    .vehicleNumber(dto.getVehicleNumber())
                    .dispatchThrough(dto.getDispatchThrough())
                    // Charges
                    .itemRate(dto.getItemRate())
                    .taxableValue(dto.getTaxableValue())
                    .packingCharges(dto.getPackingCharges())
                    .freightCharges(dto.getFreightCharges())
                    .cuttingCharges(dto.getCuttingCharges())
                    .laminationCharges(dto.getLaminationCharges())
                    .hamaliCharges(dto.getHamaliCharges())
                    .cgst(dto.getCgst())
                    .sgst(dto.getSgst())
                    .igst(dto.getIgst())
                    .totalValue(dto.getTotalValue())
                    .batchDetails(dto.getBatchDetails())
                    .build();

            System.out.println(logPrefix + " ✅ Creating new entry with RFD List Number: " + rfdListNumber);
            return entity;
        }).map(entity -> {
            try {
                PackingListTransferEntity saved = repository.save(entity);
                System.out.println("📦 [SO: " + saved.getSoNumber() + " | Line: " + saved.getLineNumber() +
                        "] ✅ SAVED with RFD: " + saved.getPackingListNumber());
                return saved;
            } catch (Exception e) {
                System.err.println("📦 [SO: " + entity.getSoNumber() + " | Line: " + entity.getLineNumber() +
                        "] ❌ SAVE ERROR: " + e.getMessage());
                throw new RuntimeException("Failed to save RFD list: " + e.getMessage(), e);
            }
        }).collect(Collectors.toList());

        // Post-save: Stock movement for each item
        for (PackingListTransferEntity saved : savedEntities) {
            try {
                performStockMovement(saved);
            } catch (Exception e) {
                System.err.println("⚠️ Stock movement failed for SO: " + saved.getSoNumber() +
                        " Line: " + saved.getLineNumber() + " - " + e.getMessage());
            }
        }

        System.out.println("\n✅ RFD List Transfer complete. Total saved: " + savedEntities.size());
        return savedEntities;
    }

    /**
     * Post-save stock movement:
     * 1. Remove scanned QR stocks from Stock Summary (reduce FG stock)
     * 2. Create new Dispatch stock record with: Item Group=FG, Store=Dispatch, StorageArea=Common, Rack=Common
     */
    private void performStockMovement(PackingListTransferEntity saved) {
        String unit = saved.getUnit();
        String itemDescription = saved.getItemDescription();
        BigDecimal quantityKg = saved.getQuantityKg();
        Integer quantityNo = saved.getQuantityNo();

        System.out.println("\n🔄 Stock Movement for: " + itemDescription);
        System.out.println("   Unit: " + unit + ", Qty: " + quantityKg + " Kg, " + quantityNo + " Nos");

        // Step 1: Reduce FG stock from Stock Summary
        List<StockSummaryEntity> fgStocks = stockSummaryRepository
                .findByUnitAndItemDescription(unit, itemDescription);

        // Filter to FG items only
        List<StockSummaryEntity> fgOnlyStocks = fgStocks.stream()
                .filter(s -> "FG".equalsIgnoreCase(s.getItemGroup()))
                .collect(Collectors.toList());

        BigDecimal remainingKg = quantityKg != null ? quantityKg : BigDecimal.ZERO;

        for (StockSummaryEntity stock : fgOnlyStocks) {
            if (remainingKg.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal stockQty = stock.getQuantityKg() != null ? stock.getQuantityKg() : BigDecimal.ZERO;

            if (stockQty.compareTo(remainingKg) <= 0) {
                // Consume entire stock record
                remainingKg = remainingKg.subtract(stockQty);
                System.out.println("   🗑️ Removing stock record ID: " + stock.getId() + " (" + stockQty + " Kg)");
                stockSummaryRepository.delete(stock);
            } else {
                // Reduce stock quantity
                stock.setQuantityKg(stockQty.subtract(remainingKg));
                if (stock.getQuantityNo() != null && quantityNo != null) {
                    stock.setQuantityNo(Math.max(0, stock.getQuantityNo() - quantityNo));
                }
                System.out.println("   📉 Reduced stock ID: " + stock.getId() + " by " + remainingKg + " Kg");
                stockSummaryRepository.save(stock);
                remainingKg = BigDecimal.ZERO;
            }
        }

        // Step 2: Create Dispatch stock record
        StockSummaryEntity dispatchStock = StockSummaryEntity.builder()
                .unit(unit)
                .itemDescription(itemDescription)
                .itemGroup("FG")
                .store("Dispatch")
                .storageArea("Common")
                .rackColumnShelfNumber("Common")
                .productCategory(saved.getProductCategory())
                .brand(saved.getBrand())
                .grade(saved.getGrade())
                .temper(saved.getTemper())
                .dimension(saved.getDimension())
                .quantityKg(quantityKg)
                .quantityNo(quantityNo)
                .build();

        stockSummaryRepository.save(dispatchStock);
        System.out.println("   ✅ Created Dispatch stock: " + itemDescription + " (" + quantityKg + " Kg)");
    }

    @Override
    public List<PackingListTransferEntity> getAllPackingLists() {
        return repository.findAll();
    }

    // ═══════════════════════════════════════════════════════════════
    //  NEW: RFD List Summary — 3-Level Hierarchy APIs
    // ═══════════════════════════════════════════════════════════════

    @Override
    public List<RfdListSummaryDTO> getRfdListSummary() {
        System.out.println("\n📊 getRfdListSummary() — Grouping by packingListNumber...");

        List<PackingListTransferEntity> allEntities = repository.findAll();

        // Group by packingListNumber
        Map<String, List<PackingListTransferEntity>> grouped = allEntities.stream()
                .filter(e -> e.getPackingListNumber() != null)
                .collect(Collectors.groupingBy(
                        PackingListTransferEntity::getPackingListNumber,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<RfdListSummaryDTO> summaries = new ArrayList<>();

        for (Map.Entry<String, List<PackingListTransferEntity>> entry : grouped.entrySet()) {
            String rfdNumber = entry.getKey();
            List<PackingListTransferEntity> items = entry.getValue();

            // Take the first item for shared fields (they should all be the same per RFD List)
            PackingListTransferEntity first = items.get(0);

            RfdListSummaryDTO dto = RfdListSummaryDTO.builder()
                    .packingListNumber(rfdNumber)
                    .timestamp(first.getTimestamp())
                    .productionStrategy(first.getTransferType())
                    .unitName(first.getUnit())
                    .customerCode(first.getCustomerCode())
                    .customerName(first.getCustomerName())
                    .customerBillingAddress(first.getCustomerBillingAddress())
                    .customerShippingAddress(first.getCustomerShippingAddress())
                    .customerPoNumber(first.getCustomerPoNumber())
                    .customerPoDate(first.getCustomerPoDate())
                    .vehicleNumber(first.getVehicleNumber())
                    .dispatchThrough(first.getDispatchThrough())
                    .itemCount(items.size())
                    .build();

            summaries.add(dto);
        }

        // Sort by timestamp descending (newest first)
        summaries.sort((a, b) -> {
            if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
            if (a.getTimestamp() == null) return 1;
            if (b.getTimestamp() == null) return -1;
            return b.getTimestamp().compareTo(a.getTimestamp());
        });

        System.out.println("   ✅ Total distinct RFD Lists: " + summaries.size());
        return summaries;
    }

    @Override
    public List<RfdListItemDTO> getRfdListItems(String packingListNumber) {
        System.out.println("\n📦 getRfdListItems() — packingListNumber: " + packingListNumber);

        List<PackingListTransferEntity> items = repository.findByPackingListNumberOrderByIdAsc(packingListNumber);

        List<RfdListItemDTO> result = items.stream().map(entity -> {
            int batchCount = 0;
            if (entity.getBatchDetails() != null && !entity.getBatchDetails().isEmpty()) {
                try {
                    List<?> batches = objectMapper.readValue(entity.getBatchDetails(), List.class);
                    batchCount = batches.size();
                } catch (Exception e) {
                    System.err.println("   ⚠️ Failed to parse batchDetails JSON: " + e.getMessage());
                }
            }

            return RfdListItemDTO.builder()
                    .id(entity.getId())
                    .soNumber(entity.getSoNumber())
                    .lineNumber(entity.getLineNumber())
                    .orderType(entity.getOrderType())
                    .productCategory(entity.getProductCategory())
                    .itemDescription(entity.getItemDescription())
                    .brand(entity.getBrand())
                    .grade(entity.getGrade())
                    .temper(entity.getTemper())
                    .dimension(entity.getDimension())
                    .quantityKg(entity.getQuantityKg())
                    .quantityNo(entity.getQuantityNo())
                    .batchCount(batchCount)
                    .build();
        }).collect(Collectors.toList());

        System.out.println("   ✅ Total items: " + result.size());
        return result;
    }

    @Override
    public List<RfdListBatchDTO> getRfdListBatchDetails(String packingListNumber, Long itemId) {
        System.out.println("\n🔍 getRfdListBatchDetails() — packingListNumber: " + packingListNumber + ", itemId: " + itemId);

        Optional<PackingListTransferEntity> entityOpt = repository.findById(itemId);
        if (entityOpt.isEmpty()) {
            System.out.println("   ❌ Item not found with ID: " + itemId);
            return Collections.emptyList();
        }

        PackingListTransferEntity entity = entityOpt.get();

        // Verify the item belongs to the specified RFD List
        if (!packingListNumber.equals(entity.getPackingListNumber())) {
            System.out.println("   ❌ Item does not belong to RFD List: " + packingListNumber);
            return Collections.emptyList();
        }

        if (entity.getBatchDetails() == null || entity.getBatchDetails().isEmpty()) {
            System.out.println("   ℹ️ No batch details for this item");
            return Collections.emptyList();
        }

        try {
            List<Map<String, Object>> batchList = objectMapper.readValue(
                    entity.getBatchDetails(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<RfdListBatchDTO> result = new ArrayList<>();
            int index = 0;
            for (Map<String, Object> batch : batchList) {
                index++;
                Map<String, Object> qrCodeFG = new HashMap<>();
                String qrUrl = getStringValue(batch, "qrCodeImageUrl");
                if (qrUrl != null && !qrUrl.isEmpty()) {
                    qrCodeFG.put("exists", true);
                    qrCodeFG.put("url", qrUrl);
                } else {
                    qrCodeFG.put("exists", false);
                }

                BigDecimal batchQtyKg = BigDecimal.ZERO;
                Object qtyKgVal = batch.get("quantityKg");
                if (qtyKgVal != null) {
                    try {
                        batchQtyKg = new BigDecimal(qtyKgVal.toString());
                    } catch (NumberFormatException ignored) {}
                }

                Integer batchQtyNo = null;
                Object qtyNoVal = batch.get("quantityNo");
                if (qtyNoVal != null) {
                    try {
                        batchQtyNo = Integer.parseInt(qtyNoVal.toString());
                    } catch (NumberFormatException ignored) {}
                }

                RfdListBatchDTO batchDTO = RfdListBatchDTO.builder()
                        .id(String.valueOf(index))
                        .itemDescription(getStringValue(batch, "itemDescription"))
                        .itemDimension(getStringValue(batch, "dimension"))
                        .batchNumber(getStringValue(batch, "batchNumber"))
                        .dateOfInward(getStringValue(batch, "dateOfInward"))
                        .quantityKg(batchQtyKg)
                        .quantityNo(batchQtyNo)
                        .qrCodeFG(qrCodeFG)
                        .build();

                result.add(batchDTO);
            }

            System.out.println("   ✅ Total batches: " + result.size());
            return result;

        } catch (Exception e) {
            System.err.println("   ❌ Failed to parse batch details JSON: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
}

package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.PickListDTOs;
import com.indona.invento.dto.PickListSchedulerDTO;
import com.indona.invento.dto.SalesOrderSchedulerDTO;
import com.indona.invento.dto.WarehouseStockRetrievalEntrySchedulerDTO;
import com.indona.invento.dto.WarehouseStockTransferSchedulerDTO;
import com.indona.invento.entities.*;
import com.indona.invento.services.GRNService;
import com.indona.invento.services.SalesOrderSchedulerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SalesOrderSchedulerServiceImpl implements SalesOrderSchedulerService {

    private final SalesOrderSchedulerRepository repository;

    private final StockSummaryRepository stockSummaryRepository;

    private final StockSummaryBundleRepository stockSummaryBundleRepository;

    private final PickListSchedulerRepository pickListSchedulerRepository;

    private final WarehouseStockTransferSchedulerRepository warehouseStockTransferSchedulerRepository;

    private final PackingSchedulerRepository packingSchedulerRepository;

    private final GRNService grnService;

    private final GrnLineItemRepository grnLineItemRepository;

    private final ProductionScheduleRepository productionScheduleRepository;

    @Override
    public void saveSchedule(List<SalesOrderSchedulerDTO> dtoList) {
        System.out.println("\n\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          🎯 saveSchedule() METHOD CALLED 🎯                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("📋 Total DTOs to save: " + dtoList.size());

        if (dtoList.isEmpty()) {
            System.out.println("⚠️  WARNING: Empty DTO list received!");
            return;
        }

        // Get ALL existing entries first
        System.out.println("\n🔍 Fetching all existing SalesOrderScheduler entries from DB...");
        List<SalesOrderSchedulerEntity> allExisting = repository.findAll();
        System.out.println("📊 Total entries in DB: " + allExisting.size());

        // Print all existing serial numbers
        if (!allExisting.isEmpty()) {
            System.out.println("📝 Existing serial numbers in DB:");
            allExisting.forEach(e -> System.out.println("   - slNo: " + e.getSlNo() + " | SO: " + e.getSoNumber() + " | Line: " + e.getLineNumber()));
        } else {
            System.out.println("   (No existing entries in DB)");
        }

        // Get the highest existing serial number
        Integer maxSlNo = allExisting.stream()
                .map(SalesOrderSchedulerEntity::getSlNo)
                .filter(slNo -> slNo != null)
                .max(Integer::compareTo)
                .orElse(0);

        System.out.println("\n✅ Calculated max serial number: " + maxSlNo);
        System.out.println("✅ Next serial number will start from: " + (maxSlNo + 1));

        List<SalesOrderSchedulerEntity> entities = new ArrayList<>();
        Integer currentSlNo = maxSlNo + 1;

        System.out.println("\n📥 Processing DTOs:");
        for (int i = 0; i < dtoList.size(); i++) {
            SalesOrderSchedulerDTO dto = dtoList.get(i);
            System.out.println("\n   [" + (i+1) + "] Processing DTO:");
            System.out.println("       SO Number: " + dto.getSoNumber());
            System.out.println("       Line Number: " + dto.getLineNumber());
            System.out.println("       Assigning Serial No: " + currentSlNo);

            SalesOrderSchedulerEntity entity = SalesOrderSchedulerEntity.builder()
                    .slNo(currentSlNo)  // ✅ Auto-increment serial number
                    .nextProcess(dto.getNextProcess())
                    .planDate(LocalDate.now())
                    .soNumber(dto.getSoNumber())
                    .lineNumber(dto.getLineNumber())
                    .unit(dto.getUnit())
                    .primeCustomer(dto.getPrimeCustomer())
                    .customerCode(dto.getCustomerCode())
                    .customerName(dto.getCustomerName())
                    .customerCategory(dto.getCustomerCategory())
                    .packing(dto.getPacking())
                    .orderType(dto.getOrderType())
                    .productCategory(dto.getProductCategory())
                    .itemDescription(dto.getItemDescription())
                    .brand(dto.getBrand())
                    .grade(dto.getGrade())
                    .temper(dto.getTemper())
                    .dimension(dto.getDimension())
                    .uomKg(dto.getUomKg())
                    .uomNo(dto.getUomNo())
                    .requiredQuantityKg(dto.getRequiredQuantityKg())
                    .requiredQuantityNo(dto.getRequiredQuantityNo())
                    .targetDateOfDispatch(dto.getTargetDateOfDispatch())
                    .retrievalStatus(dto.getRetrievalStatus())
                    .productionStrategy(dto.getProductionStrategy())
                    .build();

            entities.add(entity);
            System.out.println("       ✅ Entity created with slNo: " + entity.getSlNo());
            currentSlNo++;
        }

        System.out.println("\n\n💾 Saving all entities to database...");
        System.out.println("   Total entities to save: " + entities.size());

        List<SalesOrderSchedulerEntity> savedEntities = repository.saveAll(entities);

        System.out.println("   ✅ Saved entities count: " + savedEntities.size());

        System.out.println("\n✅ SAVE OPERATION COMPLETED!");
        System.out.println("📊 Final saved entries:");
        savedEntities.forEach(e -> {
            System.out.println("   ✓ slNo: " + e.getSlNo() +
                    " | id: " + e.getId() +
                    " | SO: " + e.getSoNumber() +
                    " | Line: " + e.getLineNumber());
        });

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   ✅ SAVE COMPLETE ✅                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
    }

    @Override
    public List<SalesOrderSchedulerDTO> getAllSchedules() {
        LocalDate today = LocalDate.now();

        return repository.findAll().stream()
                .filter(e -> {
                    String status = e.getRetrievalStatus();

                    // ✅ PENDING or IN PROGRESS - always show
                    if ("PENDING".equalsIgnoreCase(status) || "IN PROGRESS".equalsIgnoreCase(status)) {
                        System.out.println("✅ Including: SO=" + e.getSoNumber() + " | Line=" + e.getLineNumber() +
                                " | Status=" + status + " (always included)");
                        return true;
                    }

                    // COMPLETE - show only if completedTime is today
                    if ("COMPLETE".equalsIgnoreCase(status)) {
                        // Check if completedTime exists and is today
                        if (e.getCompletedTime() != null) {
                            LocalDate completedDate = e.getCompletedTime().toLocalDate();
                            boolean isToday = today.equals(completedDate);

                            if (isToday) {
                                System.out.println("✅ Including: SO=" + e.getSoNumber() + " | Line=" + e.getLineNumber() +
                                        " | Status=COMPLETE | CompletedTime=" + e.getCompletedTime() + " (today)");
                                return true;
                            } else {
                                System.out.println("❌ Excluding: SO=" + e.getSoNumber() + " | Line=" + e.getLineNumber() +
                                        " | Status=COMPLETE | CompletedTime=" + e.getCompletedTime() + " (not today)");
                                return false;
                            }
                        } else {
                            // No completedTime set, exclude it
                            System.out.println("⚠️  Excluding: SO=" + e.getSoNumber() + " | Line=" + e.getLineNumber() +
                                    " | Status=COMPLETE but no completedTime");
                            return false;
                        }
                    }

                    // Other statuses - exclude
                    System.out.println("❌ Excluding: SO=" + e.getSoNumber() + " | Line=" + e.getLineNumber() +
                            " | Status=" + status + " (unknown status)");
                    return false;
                })
                .map(e -> SalesOrderSchedulerDTO.builder()
                        .slNo(e.getSlNo())
                        .nextProcess(e.getNextProcess())
                        .soNumber(e.getSoNumber())
                        .lineNumber(e.getLineNumber())
                        .unit(e.getUnit())
                        .primeCustomer(e.getPrimeCustomer())
                        .customerCode(e.getCustomerCode())
                        .customerName(e.getCustomerName())
                        .packing(e.getPacking())
                        .orderType(e.getOrderType())
                        .productCategory(e.getProductCategory())
                        .itemDescription(e.getItemDescription())
                        .brand(e.getBrand())
                        .grade(e.getGrade())
                        .temper(e.getTemper())
                        .customerCategory(e.getCustomerCategory())
                        .dimension(e.getDimension())
                        .requiredQuantityKg(e.getRequiredQuantityKg())
                        .requiredQuantityNo(e.getRequiredQuantityNo())
                        .targetDateOfDispatch(e.getTargetDateOfDispatch())
                        .uomNo(e.getUomNo())
                        .uomKg(e.getUomKg())
                        .retrievalStatus(e.getRetrievalStatus())
                        .completedTime(e.getCompletedTime())  // ✅ Add completedTime to response
                        .planDate(today) // ✅ response me hamesha current date
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<PickListDTOs> generatePickList(List<SalesOrderSchedulerDTO> schedulerList) {
        List<StockSummaryEntity> stockList = stockSummaryRepository.findAll();
        List<PickListDTOs> result = new ArrayList<>();

        for (SalesOrderSchedulerDTO s : schedulerList) {
            System.out.println("\n🔍 Searching stock for: " + s.getItemDescription() + " | Brand: " + s.getBrand() + " | Unit: " + s.getUnit());

            List<StockSummaryEntity> candidates = stockList.stream()
                    .filter(stock -> stock.getPickListLocked() == null || !stock.getPickListLocked())
                    .filter(stock -> stock.getUnit() != null && stock.getUnit().trim().equalsIgnoreCase(s.getUnit().trim()))
                    .filter(stock -> stock.getItemDescription() != null && stock.getItemDescription().trim().equalsIgnoreCase(s.getItemDescription().trim()))
                    .filter(stock -> stock.getBrand() != null && stock.getBrand().trim().equalsIgnoreCase(s.getBrand().trim()))
                    .sorted(Comparator.comparing(StockSummaryEntity::getId))
                    .collect(Collectors.toList());

            System.out.println("→ Matching candidates found: " + candidates.size());
            candidates.forEach(c -> System.out.println(
                    "   ↪ ID=" + c.getId() +
                            " | Item=" + c.getItemDescription() +
                            " | Brand=" + c.getBrand() +
                            " | Unit=" + c.getUnit() +
                            " | Store=" + c.getStore() +
                            " | StorageArea=" + c.getStorageArea() +
                            " | Rack=" + c.getRackColumnShelfNumber() +
                            " | Locked=" + c.getPickListLocked()
            ));

            StockSummaryEntity selected = null;

            if ("FULL".equalsIgnoreCase(s.getOrderType())) {
                selected = candidates.stream()
                        .filter(c -> c.getQuantityNo() != null && c.getQuantityNo() >= s.getRequiredQuantityNo())
                        .findFirst()
                        .orElse(null);

                if (selected == null && !candidates.isEmpty()) {
                    selected = candidates.get(0);
                }

            } else if ("CUT".equalsIgnoreCase(s.getOrderType())) {
                selected = candidates.stream()
                        .filter(c -> c.getDimension() != null && c.getDimension().trim().equalsIgnoreCase(s.getDimension().trim()))
                        .findFirst()
                        .orElse(null);

                if (selected == null && !candidates.isEmpty()) {
                    selected = candidates.get(0);
                }

            } else {
                selected = candidates.stream().findFirst().orElse(null);
            }

            String store = selected != null ? selected.getStore() : "N/A";
            String storageArea = selected != null ? selected.getStorageArea() : "N/A";
            String rackColumnShelfNumber = selected != null ? selected.getRackColumnShelfNumber() : "N/A";

            if (selected != null) {
                selected.setPickListLocked(true);
                stockSummaryRepository.save(selected);
                System.out.println("✅ Selected stock: " + selected.getItemDescription() +
                        " | Store: " + store +
                        " | StorageArea: " + storageArea +
                        " | Rack: " + rackColumnShelfNumber);
            } else {
                System.out.println("❌ No matching stock found for: " + s.getItemDescription());
            }

            SalesOrderSchedulerEntity scheduler = repository.findBySoNumberAndLineNumber(s.getSoNumber(), s.getLineNumber());
            if (scheduler != null) {
                scheduler.setRetrievalStatus("IN PROGRESS");
                repository.save(scheduler);
                System.out.println("📦 Retrieval status updated to IN_PROGRESS for SO="
                        + s.getSoNumber() + " Line=" + s.getLineNumber());
            }

            PickListDTOs dto = PickListDTOs.builder()
                    .unit(s.getUnit())
                    .soNumber(s.getSoNumber())
                    .lineNumber(String.valueOf(s.getLineNumber()))
                    .nextProcess(s.getNextProcess())
                    .orderType(s.getOrderType())
                    .productCategory(s.getProductCategory())
                    .itemDescription(s.getItemDescription())
                    .brand(s.getBrand())
                    .retrievalQuantityKg(s.getRequiredQuantityKg() != null ? s.getRequiredQuantityKg() : BigDecimal.ZERO)
                    .retrievalQuantityNo(s.getRequiredQuantityNo() != null ? s.getRequiredQuantityNo() : 0)
                    .store(store)
                    .storageArea(storageArea)
                    .rackColumnShelfNumber(rackColumnShelfNumber)
                    .build();

            result.add(dto);
        }

        return result;
    }

    @Override
    public List<SalesOrderSchedulerEntity> updateSchedulerWithPickList(List<PickListDTOs> dtos) {
        List<SalesOrderSchedulerEntity> updatedSchedulers = new ArrayList<>();

        for (PickListDTOs dto : dtos) {
            SalesOrderSchedulerEntity scheduler = repository.findBySoNumberAndLineNumber(dto.getSoNumber(), (dto.getLineNumber()));
            if (scheduler != null) {
                PickListEntityScheduler pickList = PickListEntityScheduler.builder()
                        .unit(dto.getUnit())
                        .soNumber(dto.getSoNumber())
                        .lineNumber(dto.getLineNumber())
                        .nextProcess(dto.getNextProcess())
                        .orderType(dto.getOrderType())
                        .productCategory(dto.getProductCategory())
                        .itemDescription(dto.getItemDescription())
                        .brand(dto.getBrand())
                        .retrievalQuantityKg(dto.getRetrievalQuantityKg())
                        .retrievalQuantityNo(dto.getRetrievalQuantityNo())
                        .storageArea(dto.getStorageArea())
                        .store(dto.getStore())
                        .rackColumnShelfNumber(dto.getRackColumnShelfNumber())
                        .build();

                scheduler.setPickList(pickList);
                updatedSchedulers.add(repository.save(scheduler));
            }
        }

        return updatedSchedulers;
    }

    @Override
    @Transactional
    public List<SalesOrderSchedulerEntity> updateSchedulerWithStockTransfer(List<WarehouseStockTransferSchedulerDTO> dtos) {
        List<SalesOrderSchedulerEntity> updatedSchedulers = new ArrayList<>();

        for (WarehouseStockTransferSchedulerDTO dto : dtos) {
            System.out.println("🔄 Processing DTO → SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber());

            System.out.println("📥 Incoming QR Code from DTO: " + dto.getQrCode());
            System.out.println("📥 Incoming nextProcess from DTO: " + dto.getNextProcess());

            SalesOrderSchedulerEntity scheduler = repository.findBySoNumberAndLineNumber(dto.getSoNumber(), dto.getLineNumber());
            if (scheduler == null) {
                System.out.println("❌ Scheduler not found for SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber());
                continue;
            }

            System.out.println("✅ Scheduler found → ID: " + scheduler.getId());

            // Preserve nextProcess if DTO is blank
            String incomingNextProcess = dto.getNextProcess();
            if (incomingNextProcess == null || incomingNextProcess.isBlank()) {
                incomingNextProcess = scheduler.getNextProcess();
                System.out.println("⚠️ nextProcess was blank, preserving existing: " + incomingNextProcess);
            }

            List<WarehouseStockRetrievalEntityScheduler> retrievals = Optional.ofNullable(dto.getRetrievalEntries())
                    .orElse(List.of())
                    .stream()
                    .map(r -> {
                        System.out.println("   ↪ Retrieval → QtyKg: " + r.getRetrievalQuantityKg() + " | QtyNo: " + r.getRetrievalQuantityNo());
                        return WarehouseStockRetrievalEntityScheduler.builder()
                                .retrievalQuantityKg(r.getRetrievalQuantityKg())
                                .retrievalQuantityNo(r.getRetrievalQuantityNo())
                                .build();
                    })
                    .toList();

            // QR code nextProcess fallback
            String qrNextProcess = dto.getNextProcess();
            if (qrNextProcess == null || qrNextProcess.isBlank()) {
                qrNextProcess = incomingNextProcess;
            }

            WarehouseStockTransferEntityScheduler transfer = WarehouseStockTransferEntityScheduler.builder()
                    .soNumber(dto.getSoNumber())
                    .lineNumber(dto.getLineNumber())
                    .nextProcess(incomingNextProcess)
                    .requiredQuantityKg(dto.getRequiredQuantityKg())
                    .requiredQuantityNo(dto.getRequiredQuantityNo())
                    .weighmentQuantityKg(dto.getWeighmentQuantityKg())
                    .weighmentQuantityNo(dto.getWeighmentQuantityNo())
                    .returnableQuantityKg(dto.getReturnableQuantityKg())
                    .returnableQuantityNo(dto.getReturnableQuantityNo())
                    .scrapQuantityKg(dto.getScrapQuantityKg())
                    .scrapQuantityNo(dto.getScrapQuantityNo())
                    .itemDescription(dto.getItemDescription())
                    .brand(dto.getBrand())
                    .grade(dto.getGrade())
                    .temper(dto.getTemper())
                    .qrCode(dto.getQrCode()) // QR code already contains nextProcess
                    .unit(dto.getUnit())
                    .generateQr(dto.getGenerateQr())
                    .retrievalEntries(retrievals)
                    .build();

            System.out.println("📦 Transfer object created → QR Code: " + transfer.getQrCode());

            retrievals.forEach(r -> r.setStockTransfer(transfer));
            scheduler.setStockTransfer(transfer);

            scheduler.setRetrievalStatus("COMPLETE");
            scheduler.setNextProcess(incomingNextProcess); // ✅ preserved or updated

            System.out.println("📦 Retrieval status set to COMPLETE");
            System.out.println("🔍 Checking packing trigger → NextProcess: " + scheduler.getNextProcess() + " | Packing: " + scheduler.getPacking());

            if ("DISPATCH".equalsIgnoreCase(scheduler.getNextProcess()) && Boolean.TRUE.equals(scheduler.getPacking())) {
                System.out.println("🚚 DISPATCH + Packing = true → Creating PackingEntity");

                PackingEntityScheduler packing = PackingEntityScheduler.builder()
                        .soNumber(scheduler.getSoNumber())
                        .lineNumber(scheduler.getLineNumber())
                        .unit(scheduler.getUnit())
                        .customerCode(scheduler.getCustomerCode())
                        .customerName(scheduler.getCustomerName())
                        .orderType(scheduler.getOrderType())
                        .productCategory(scheduler.getProductCategory())
                        .itemDescription(scheduler.getItemDescription())
                        .brand(scheduler.getBrand())
                        .grade(scheduler.getGrade())
                        .temper(scheduler.getTemper())
                        .dimension(scheduler.getDimension())
                        .quantityKg(scheduler.getStockTransfer().getWeighmentQuantityKg())
                        .uomKg("KG")
                        .quantityNo(scheduler.getStockTransfer().getWeighmentQuantityNo())
                        .uomNo("NO")
                        .targetDateOfDispatch(scheduler.getTargetDateOfDispatch())
                        .packingInstructions(null)
                        .packingStatus("PENDING")
                        .build();

                PackingEntityScheduler savedPacking = packingSchedulerRepository.save(packing);
                System.out.println("✅ Packing saved → ID: " + savedPacking.getId());
            } else {
                System.out.println("⚠️ Packing not triggered → NextProcess: " + scheduler.getNextProcess() + " | Packing: " + scheduler.getPacking());
            }

            SalesOrderSchedulerEntity savedScheduler = repository.save(scheduler);
            updatedSchedulers.add(savedScheduler);
            System.out.println("💾 Scheduler updated → ID: " + savedScheduler.getId() + " | Status: " + savedScheduler.getRetrievalStatus());
        }

        System.out.println("✅ All DTOs processed. Total updated: " + updatedSchedulers.size());
        return updatedSchedulers;
    }

    @Override
    public void deleteAllSchedulers() {
        repository.deleteAll();
    }

    @Override
    public List<SalesOrderSchedulerEntity> getBySoNumber(String soNumber) {
        return repository.findBySoNumber(soNumber);
    }

    @Override
    public SalesOrderSchedulerEntity getBySoNumberAndLineNumber(String soNumber, String lineNumber) {
        return repository.findBySoNumberAndLineNumber(soNumber, lineNumber);
    }

    @Override
    public List<SalesOrderSchedulerEntity> getAllSchedulersEntities() {
        return repository.findAll();
    }

    @Override
    public java.util.Map<String, Object> generatePickListForFullOrder(SalesOrderSchedulerDTO dto) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            📋 PICKLIST GENERATION - FULL ORDER                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");

        // Check if DTO is null
        if (dto == null) {
            System.out.println("   ❌ ERROR: No data received!");
            return java.util.Map.of(
                    "success", false,
                    "message", "No data provided",
                    "data", new ArrayList<>()
            );
        }

        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ INPUT PARAMETERS                                                            │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ SO Number      : " + dto.getSoNumber());
        System.out.println("│ Line Number    : " + dto.getLineNumber());
        System.out.println("│ Unit           : " + dto.getUnit());
        System.out.println("│ Item Desc      : " + dto.getItemDescription());
        System.out.println("│ Dimension      : " + dto.getDimension());
        System.out.println("│ Order Type     : " + dto.getOrderType());
        System.out.println("│ Required Qty KG: " + dto.getRequiredQuantityKg());
        System.out.println("│ Required Qty NO: " + dto.getRequiredQuantityNo());
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // Check if orderType is FULL
        if (!"FULL".equalsIgnoreCase(dto.getOrderType())) {
            System.out.println("   ❌ REJECTED: Order type is not FULL. Actual: " + dto.getOrderType());
            return java.util.Map.of(
                    "success", false,
                    "message", "Order type must be FULL. Current: " + dto.getOrderType(),
                    "data", new ArrayList<>()
            );
        }

        // Step 1: Filter by Unit from StockSummary
        System.out.println("\n┌─ STEP 1: FILTER BY UNIT ─────────────────────────────────────────────────────┐");
        List<StockSummaryEntity> stockList = stockSummaryRepository.findAll();
        System.out.println("│ Total stocks in DB: " + stockList.size());

        List<StockSummaryEntity> unitFiltered = stockList.stream()
                .filter(stock -> stock.getUnit() != null && stock.getUnit().trim().equalsIgnoreCase(dto.getUnit().trim()))
                .collect(Collectors.toList());
        System.out.println("│ ✅ Unit matched: " + unitFiltered.size() + " stocks");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // Step 2: Filter by Item Description within Unit
        System.out.println("\n┌─ STEP 2: FILTER BY ITEM DESCRIPTION ────────────────────────────────────────┐");
        List<StockSummaryEntity> itemFiltered = unitFiltered.stream()
                .filter(stock -> stock.getItemDescription() != null && stock.getItemDescription().trim().equalsIgnoreCase(dto.getItemDescription().trim()))
                .collect(Collectors.toList());
        System.out.println("│ ✅ Item Description matched: " + itemFiltered.size() + " stocks");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // Step 2.5: Filter by ItemGroup = "RAW MATERIAL" and Dimension match
        System.out.println("\n┌─ STEP 2.5: FILTER BY ITEM GROUP & DIMENSION ────────────────────────────────┐");
        System.out.println("│ Required ItemGroup: RAW MATERIAL");
        System.out.println("│ Required Dimension: " + dto.getDimension());
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        // Helper to normalize dimension for comparison (remove spaces, lowercase)
        java.util.function.Function<String, String> normalizeDimension = dim -> {
            if (dim == null) return "";
            return dim.trim().toLowerCase().replaceAll("\\s+", "").replaceAll("[xX×]", "x");
        };

        String normalizedRequiredDim = normalizeDimension.apply(dto.getDimension());
        System.out.println("│ Normalized Required Dimension: '" + normalizedRequiredDim + "'");

        List<StockSummaryEntity> rawMaterialFiltered = itemFiltered.stream()
                .filter(stock -> {
                    // Check itemGroup is RAW MATERIAL
                    boolean isRawMaterial = stock.getItemGroup() != null &&
                            stock.getItemGroup().trim().equalsIgnoreCase("RAW MATERIAL");

                    // Check dimension matches (if dimension is provided) - NORMALIZED comparison
                    boolean dimensionMatch = true;
                    if (dto.getDimension() != null && !dto.getDimension().trim().isEmpty()) {
                        String normalizedStockDim = normalizeDimension.apply(stock.getDimension());
                        dimensionMatch = normalizedStockDim.equals(normalizedRequiredDim);

                        // Log dimension comparison for debugging
                        System.out.println("│    → Stock Dim: '" + stock.getDimension() + "' → Normalized: '" + normalizedStockDim +
                                "' vs Required: '" + normalizedRequiredDim + "' = " + (dimensionMatch ? "MATCH" : "NO MATCH"));
                    }

                    String status = (isRawMaterial && dimensionMatch) ? "✅ PASS" : "❌ FAIL";
                    String reason = "";
                    if (!isRawMaterial) reason += "ItemGroup≠RAW_MATERIAL ";
                    if (!dimensionMatch) reason += "Dimension mismatch ";

                    System.out.println("│ Stock ID: " + String.format("%-6d", stock.getId()) +
                            " | ItemGroup: " + String.format("%-15s", stock.getItemGroup()) +
                            " | Dim: " + String.format("%-15s", stock.getDimension()) +
                            " | " + status + (reason.isEmpty() ? "" : " (" + reason.trim() + ")"));

                    return isRawMaterial && dimensionMatch;
                })
                .collect(Collectors.toList());
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ ✅ RAW MATERIAL + Dimension matched: " + rawMaterialFiltered.size() + " stocks");
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // Use rawMaterialFiltered instead of itemFiltered for further processing
        List<StockSummaryEntity> finalFilteredList = rawMaterialFiltered;

        // Step 3: Extract racks with store, storageArea and their GRN numbers + Bundle details from StockSummaryBundleEntity
        System.out.println("   🔍 Step 3: Extracting racks with Store, Storage Area, GRN references and Bundle details");

        // Map: rack -> { store, storageArea, grnNumbers[], bundles[] }
        java.util.Map<String, java.util.Map<String, Object>> rackDetailsMap = new java.util.LinkedHashMap<>();

        // Also collect all bundles from StockSummaryBundleEntity (one-to-many relationship)
        List<java.util.Map<String, Object>> allBundlesList = new ArrayList<>();

        for (StockSummaryEntity stock : finalFilteredList) {
            String rack = stock.getRackColumnShelfNumber();
            if (rack == null || rack.isBlank()) {
                rack = "UNKNOWN";
            }

            String store = stock.getStore() != null ? stock.getStore() : "N/A";
            String storageArea = stock.getStorageArea() != null ? stock.getStorageArea() : "N/A";
            String stockDimension = stock.getDimension() != null ? stock.getDimension() : "";

            // Parse GRN numbers from JSON string
            List<String> grnList = new ArrayList<>();
            if (stock.getGrnNumbers() != null && !stock.getGrnNumbers().isBlank()) {
                try {
                    String grnJson = stock.getGrnNumbers().trim();
                    if (grnJson.startsWith("[") && grnJson.endsWith("]")) {
                        grnJson = grnJson.substring(1, grnJson.length() - 1);
                        if (!grnJson.isBlank()) {
                            String[] grnArray = grnJson.split(",");
                            for (String grn : grnArray) {
                                String cleanGrn = grn.trim().replace("\"", "");
                                if (!cleanGrn.isBlank()) {
                                    grnList.add(cleanGrn);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("   ⚠️ Error parsing GRN numbers for rack " + rack + ": " + e.getMessage());
                }
            }

            // Fetch bundle details from StockSummaryBundleEntity (one-to-many relationship)
            List<StockSummaryBundleEntity> bundles = stock.getBundles();
            System.out.println("      📦 Stock ID: " + stock.getId() + " has " + (bundles != null ? bundles.size() : 0) + " bundles");

            if (bundles != null && !bundles.isEmpty()) {
                for (StockSummaryBundleEntity bundle : bundles) {
                    java.util.Map<String, Object> bundleMap = new java.util.LinkedHashMap<>();
                    bundleMap.put("bundleId", bundle.getId());
                    bundleMap.put("slNo", bundle.getSlNo());
                    bundleMap.put("grnNumber", bundle.getGrnNumber());
                    bundleMap.put("grnId", bundle.getGrnId());
                    bundleMap.put("itemDescription", bundle.getItemDescription());
                    bundleMap.put("productCategory", bundle.getProductCategory());
                    bundleMap.put("brand", bundle.getBrand());
                    bundleMap.put("grade", bundle.getGrade());
                    bundleMap.put("temper", bundle.getTemper());
                    bundleMap.put("dimension", bundle.getDimension()); // Bundle dimension
                    bundleMap.put("quantityKg", bundle.getWeightmentQuantityKg() != null ? bundle.getWeightmentQuantityKg().doubleValue() : 0.0);
                    bundleMap.put("quantityNo", bundle.getWeightmentQuantityNo() != null ? bundle.getWeightmentQuantityNo() : 0);
                    bundleMap.put("weighment", bundle.getWeighment());
                    bundleMap.put("heatNo", bundle.getHeatNo());
                    bundleMap.put("lotNo", bundle.getLotNo());
                    bundleMap.put("testCertificate", bundle.getTestCertificate());
                    bundleMap.put("materialAcceptance", bundle.getMaterialAcceptance());
                    bundleMap.put("currentStore", bundle.getCurrentStore());
                    bundleMap.put("recipientStore", bundle.getRecipientStore());
                    bundleMap.put("storageArea", bundle.getStorageArea());
                    bundleMap.put("rackColumnBinNumber", bundle.getRackColumnBinNumber());
                    bundleMap.put("qrCodeUrl", bundle.getQrCodeUrl());
                    bundleMap.put("status", bundle.getStatus());
                    bundleMap.put("stockSummaryId", stock.getId());
                    bundleMap.put("rack", rack);
                    bundleMap.put("store", store);
                    bundleMap.put("stockDimension", stockDimension);

                    allBundlesList.add(bundleMap);
                    System.out.println("         - Bundle SlNo: " + bundle.getSlNo() +
                            " | GRN: " + bundle.getGrnNumber() +
                            " | Dimension: " + bundle.getDimension() +
                            " | Qty KG: " + bundle.getWeightmentQuantityKg());
                }
            }

            if (rackDetailsMap.containsKey(rack)) {
                // Append GRNs to existing list (avoid duplicates)
                @SuppressWarnings("unchecked")
                List<String> existingGrns = (List<String>) rackDetailsMap.get(rack).get("grnNumbers");
                for (String grn : grnList) {
                    if (!existingGrns.contains(grn)) {
                        existingGrns.add(grn);
                    }
                }
            } else {
                java.util.Map<String, Object> rackInfo = new java.util.LinkedHashMap<>();
                rackInfo.put("store", store);
                rackInfo.put("storageArea", storageArea);
                rackInfo.put("grnNumbers", new ArrayList<>(grnList));
                rackInfo.put("dimension", stockDimension);
                rackDetailsMap.put(rack, rackInfo);
            }

            System.out.println("      - Rack: " + rack + " | Store: " + store + " | StorageArea: " + storageArea + " | Dimension: " + stockDimension + " | GRNs: " + grnList);
        }

        System.out.println("   ✅ Total racks found: " + rackDetailsMap.size());
        System.out.println("   ✅ Total bundles collected: " + allBundlesList.size());

        // Step 4: Group bundles by GRN number (using data from StockSummaryBundleEntity)
        System.out.println("   🔍 Step 4: Grouping bundles by GRN number from StockSummaryBundleEntity");

        // Store all GRN details with their rack, store, storageArea info
        java.util.Map<String, java.util.Map<String, Object>> grnDetailsMap = new java.util.LinkedHashMap<>();

        // Group bundles by GRN number
        java.util.Map<String, List<java.util.Map<String, Object>>> bundlesByGrn = allBundlesList.stream()
                .filter(b -> b.get("grnNumber") != null)
                .collect(Collectors.groupingBy(b -> (String) b.get("grnNumber")));

        for (java.util.Map.Entry<String, List<java.util.Map<String, Object>>> grnEntry : bundlesByGrn.entrySet()) {
            String grnNumber = grnEntry.getKey();
            List<java.util.Map<String, Object>> grnBundles = grnEntry.getValue();

            java.util.Map<String, Object> grnData = new java.util.LinkedHashMap<>();
            grnData.put("grnRefNo", grnNumber);

            // Get first bundle's GRN details (they should be same for all bundles in same GRN)
            java.util.Map<String, Object> firstBundle = grnBundles.get(0);
            grnData.put("grnId", firstBundle.get("grnId"));
            grnData.put("heatNo", firstBundle.get("heatNo"));
            grnData.put("lotNo", firstBundle.get("lotNo"));
            grnData.put("testCertificate", firstBundle.get("testCertificate"));
            grnData.put("rack", firstBundle.get("rack"));
            grnData.put("store", firstBundle.get("store"));
            grnData.put("storageArea", firstBundle.get("storageArea"));
            grnData.put("dimension", firstBundle.get("stockDimension"));

            // Calculate totals
            double totalQtyKg = 0.0;
            int totalQtyNo = 0;
            for (java.util.Map<String, Object> bundle : grnBundles) {
                totalQtyKg += (Double) bundle.get("quantityKg");
                totalQtyNo += (Integer) bundle.get("quantityNo");
            }

            grnData.put("bundleDetails", grnBundles);
            grnData.put("bundleCount", grnBundles.size());
            grnData.put("totalQuantityKg", totalQtyKg);
            grnData.put("totalQuantityNo", totalQtyNo);

            grnDetailsMap.put(grnNumber, grnData);
            System.out.println("      ✅ GRN " + grnNumber + " - Bundles: " + grnBundles.size() + " | Total Qty KG: " + totalQtyKg);
        }

        System.out.println("   ✅ Total GRNs processed: " + grnDetailsMap.size());

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // STEP 5: APPLY SELECTION LOGIC FOR FULL ORDERS
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n   ═══════════════════════════════════════════════════════════════════════");
        System.out.println("   ║ STEP 5: APPLYING FULL ORDER SELECTION LOGIC                        ║");
        System.out.println("   ═══════════════════════════════════════════════════════════════════════");

        BigDecimal requiredQtyKg = dto.getRequiredQuantityKg() != null ? dto.getRequiredQuantityKg() : BigDecimal.ZERO;
        Integer requiredQtyNo = dto.getRequiredQuantityNo() != null ? dto.getRequiredQuantityNo() : 0;

        // Tolerance: ±5%
        BigDecimal tolerancePercent = new BigDecimal("0.05");
        BigDecimal lowerTolerance = requiredQtyKg.multiply(BigDecimal.ONE.subtract(tolerancePercent));
        BigDecimal upperTolerance = requiredQtyKg.multiply(BigDecimal.ONE.add(tolerancePercent));

        System.out.println("   📊 Required Qty: " + requiredQtyKg + " KG");
        System.out.println("   📊 Tolerance: -5% = " + lowerTolerance + " KG | +5% = " + upperTolerance + " KG");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CATEGORIZE BUNDLES BY STORE TYPE
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        List<java.util.Map<String, Object>> warehouseBundles = new ArrayList<>();      // Bundles WITH QR
        List<java.util.Map<String, Object>> loosePieceBundles = new ArrayList<>();     // Loose Piece storage
        List<java.util.Map<String, Object>> commonBundles = new ArrayList<>();         // Common bin (WITHOUT QR)

        System.out.println("\n   🔍 Categorizing bundles by store type...");

        for (java.util.Map.Entry<String, java.util.Map<String, Object>> grnEntry : grnDetailsMap.entrySet()) {
            String grnNumber = grnEntry.getKey();
            java.util.Map<String, Object> grnData = grnEntry.getValue();

            @SuppressWarnings("unchecked")
            List<java.util.Map<String, Object>> bundleDetails = (List<java.util.Map<String, Object>>) grnData.get("bundleDetails");

            if (bundleDetails != null) {
                for (java.util.Map<String, Object> bundle : bundleDetails) {
                    java.util.Map<String, Object> enrichedBundle = new java.util.LinkedHashMap<>(bundle);
                    enrichedBundle.put("grnRefNo", grnNumber);

                    String bundleStore = (String) bundle.get("currentStore");
                    String bundleStorageArea = (String) bundle.get("storageArea");
                    String bundleRack = (String) bundle.get("rackColumnBinNumber");

                    // Get dimension from bundle or GRN level
                    String bundleDimension = bundle.get("dimension") != null ? (String) bundle.get("dimension") :
                                             (bundle.get("stockDimension") != null ? (String) bundle.get("stockDimension") :
                                             (String) grnData.get("dimension"));

                    if (bundleStore == null || bundleStore.isBlank()) {
                        bundleStore = (String) grnData.get("store");
                    }
                    if (bundleStorageArea == null || bundleStorageArea.isBlank()) {
                        bundleStorageArea = (String) grnData.get("storageArea");
                    }
                    if (bundleRack == null || bundleRack.isBlank()) {
                        bundleRack = (String) grnData.get("rack");
                    }

                    enrichedBundle.put("store", bundleStore);
                    enrichedBundle.put("storageArea", bundleStorageArea);
                    enrichedBundle.put("rack", bundleRack);
                    enrichedBundle.put("dimension", bundleDimension);

                    // Categorize by storage area
                    String storageAreaLower = bundleStorageArea != null ? bundleStorageArea.toLowerCase().trim() : "";
                    String storeLower = bundleStore != null ? bundleStore.toLowerCase().trim() : "";

                    if (storageAreaLower.contains("loose") || storageAreaLower.contains("piece") ||
                        storeLower.contains("loose") || storeLower.contains("piece")) {
                        loosePieceBundles.add(enrichedBundle);
                    } else if (storageAreaLower.contains("common") || storageAreaLower.contains("common_bin") ||
                               storeLower.contains("common")) {
                        commonBundles.add(enrichedBundle);
                    } else {
                        warehouseBundles.add(enrichedBundle);
                    }
                }
            }
        }

        // Helper function to get bundle quantity
        java.util.function.Function<java.util.Map<String, Object>, BigDecimal> getBundleQtyKg = bundle -> {
            Object qty = bundle.get("quantityKg");
            return qty instanceof Number ? BigDecimal.valueOf(((Number) qty).doubleValue()) : BigDecimal.ZERO;
        };

        // Sort ALL bundles by GRN Timestamp (FIFO - earliest first)
        Comparator<java.util.Map<String, Object>> fifoComparator = (a, b) -> {
            Long tsA = a.get("grnTimestamp") != null ? ((Number) a.get("grnTimestamp")).longValue() : Long.MAX_VALUE;
            Long tsB = b.get("grnTimestamp") != null ? ((Number) b.get("grnTimestamp")).longValue() : Long.MAX_VALUE;
            return tsA.compareTo(tsB);
        };

        warehouseBundles.sort(fifoComparator);
        loosePieceBundles.sort(fifoComparator);
        commonBundles.sort(fifoComparator);

        // Calculate totals
        BigDecimal totalWarehouseQty = warehouseBundles.stream().map(getBundleQtyKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLoosePieceQty = loosePieceBundles.stream().map(getBundleQtyKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommonQty = commonBundles.stream().map(getBundleQtyKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWithQR = totalWarehouseQty.add(totalLoosePieceQty);
        BigDecimal grandTotal = totalWithQR.add(totalCommonQty);

        System.out.println("\n┌─ BUNDLE CATEGORIZATION SUMMARY ─────────────────────────────────────────────┐");
        System.out.println("│ Category              │ Bundles │ Total Qty (KG)     │ Has QR │");
        System.out.println("├───────────────────────┼─────────┼────────────────────┼────────┤");
        System.out.println("│ WAREHOUSE             │ " + String.format("%-7d", warehouseBundles.size()) + " │ " + String.format("%-18s", totalWarehouseQty) + " │ YES    │");
        System.out.println("│ LOOSE PIECE           │ " + String.format("%-7d", loosePieceBundles.size()) + " │ " + String.format("%-18s", totalLoosePieceQty) + " │ YES    │");
        System.out.println("│ COMMON (No QR)        │ " + String.format("%-7d", commonBundles.size()) + " │ " + String.format("%-18s", totalCommonQty) + " │ NO     │");
        System.out.println("├───────────────────────┼─────────┼────────────────────┼────────┤");
        System.out.println("│ TOTAL WITH QR         │         │ " + String.format("%-18s", totalWithQR) + " │        │");
        System.out.println("│ GRAND TOTAL           │         │ " + String.format("%-18s", grandTotal) + " │        │");
        System.out.println("└───────────────────────┴─────────┴────────────────────┴────────┘");

        // Log individual bundles
        if (!warehouseBundles.isEmpty()) {
            System.out.println("\n┌─ WAREHOUSE BUNDLES (Sorted by GRN Timestamp - FIFO) ────────────────────────┐");
            for (int i = 0; i < warehouseBundles.size(); i++) {
                java.util.Map<String, Object> b = warehouseBundles.get(i);
                System.out.println("│ [" + (i+1) + "] BundleID: " + b.get("bundleId") +
                        " | GRN: " + b.get("grnRefNo") +
                        " | Qty: " + getBundleQtyKg.apply(b) + " KG" +
                        " | Dim: " + b.get("dimension"));
            }
            System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        }

        if (!loosePieceBundles.isEmpty()) {
            System.out.println("\n┌─ LOOSE PIECE BUNDLES (Sorted by GRN Timestamp - FIFO) ──────────────────────┐");
            for (int i = 0; i < loosePieceBundles.size(); i++) {
                java.util.Map<String, Object> b = loosePieceBundles.get(i);
                System.out.println("│ [" + (i+1) + "] BundleID: " + b.get("bundleId") +
                        " | GRN: " + b.get("grnRefNo") +
                        " | Qty: " + getBundleQtyKg.apply(b) + " KG" +
                        " | Dim: " + b.get("dimension"));
            }
            System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        }

        if (!commonBundles.isEmpty()) {
            System.out.println("\n┌─ COMMON BUNDLES (No QR - Sorted by GRN Timestamp - FIFO) ───────────────────┐");
            for (int i = 0; i < commonBundles.size(); i++) {
                java.util.Map<String, Object> b = commonBundles.get(i);
                System.out.println("│ [" + (i+1) + "] BundleID: " + b.get("bundleId") +
                        " | GRN: " + b.get("grnRefNo") +
                        " | Qty: " + getBundleQtyKg.apply(b) + " KG" +
                        " | Dim: " + b.get("dimension"));
            }
            System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");
        }

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // SELECTION VARIABLES
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        List<java.util.Map<String, Object>> selectedBundles = new ArrayList<>();
        String storageType = "";
        String selectionReason = "";
        BigDecimal totalSelectedQtyKg = BigDecimal.ZERO;
        boolean selectionDone = false;

        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            APPLYING SELECTION CRITERIA (FULL ORDER)                         ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Required Qty    : " + requiredQtyKg + " KG");
        System.out.println("║ Tolerance       : ±5%");
        System.out.println("║ Lower Tolerance : " + lowerTolerance + " KG");
        System.out.println("║ Upper Tolerance : " + upperTolerance + " KG");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CRITERIA 1: Required Qty < Bundle Qty → LOOSE PIECE STORAGE
        // (Stock with QR is >= Required, but Required < single bundle)
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n┌─ CRITERIA 1: Required Qty < Bundle Qty? ────────────────────────────────────┐");
        System.out.println("│ Condition: Stock with QR >= Required, but Required < single bundle qty      │");
        System.out.println("│ Action   : Select from LOOSE PIECE STORAGE                                  │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        if (!selectionDone && totalWithQR.compareTo(requiredQtyKg) >= 0) {
            boolean criteria1Checked = false;
            for (java.util.Map<String, Object> bundle : warehouseBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                boolean isMatch = requiredQtyKg.compareTo(bundleQty) < 0 && requiredQtyKg.compareTo(bundleQty.multiply(new BigDecimal("0.95"))) < 0;

                System.out.println("│ Bundle " + bundle.get("bundleId") + " | Qty: " + bundleQty + " KG | Required < Bundle? " + (isMatch ? "✅ YES" : "❌ NO"));

                if (isMatch && !criteria1Checked) {
                    criteria1Checked = true;
                    System.out.println("│ ✅ CRITERIA 1 MATCHED!");
                    System.out.println("│    Required: " + requiredQtyKg + " KG < Bundle: " + bundleQty + " KG");
                    System.out.println("│    → Selecting from LOOSE PIECE STORAGE");

                    if (!loosePieceBundles.isEmpty()) {
                        for (java.util.Map<String, Object> looseBundle : loosePieceBundles) {
                            BigDecimal looseQty = getBundleQtyKg.apply(looseBundle);
                            selectedBundles.add(looseBundle);
                            totalSelectedQtyKg = totalSelectedQtyKg.add(looseQty);
                            System.out.println("│    ➕ Selected: Bundle " + looseBundle.get("bundleId") + " | " + looseQty + " KG | Running Total: " + totalSelectedQtyKg + " KG");

                            if (totalSelectedQtyKg.compareTo(requiredQtyKg) >= 0) {
                                break;
                            }
                        }

                        if (totalSelectedQtyKg.compareTo(requiredQtyKg) >= 0) {
                            storageType = "LOOSE_PIECE";
                            selectionReason = "Required Qty (" + requiredQtyKg + " KG) < Bundle Qty (" + bundleQty + " KG) → Selected pieces from Loose Piece Storage (FIFO)";
                            selectionDone = true;
                        }
                    } else {
                        System.out.println("│    ⚠️ No Loose Piece bundles available - will check other criteria");
                    }
                    break;
                }
            }
            if (!criteria1Checked) {
                System.out.println("│ ❌ No bundle found where Required < Bundle Qty");
            }
        } else {
            System.out.println("│ ⏭️ SKIPPED: Total with QR (" + totalWithQR + ") < Required (" + requiredQtyKg + ")");
        }
        System.out.println("│ Status: " + (selectionDone ? "✅ SELECTION DONE" : "➡️ Continue to next criteria"));
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CRITERIA 2: Required Qty ≈ Bundle Qty (±5% tolerance) → WAREHOUSE
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n┌─ CRITERIA 2: Required Qty ≈ Single Bundle Qty (±5%)? ────────────────────────┐");
        System.out.println("│ Condition: Single bundle qty within ±5% of required                          │");
        System.out.println("│ Action   : Select that bundle from WAREHOUSE                                 │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        if (!selectionDone && totalWithQR.compareTo(requiredQtyKg) >= 0) {
            boolean found = false;
            for (java.util.Map<String, Object> bundle : warehouseBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                boolean inRange = bundleQty.compareTo(lowerTolerance) >= 0 && bundleQty.compareTo(upperTolerance) <= 0;

                System.out.println("│ Bundle " + bundle.get("bundleId") + " | Qty: " + bundleQty + " KG | In Range [" + lowerTolerance + "-" + upperTolerance + "]? " + (inRange ? "✅ YES" : "❌ NO"));

                if (inRange && !found) {
                    found = true;
                    System.out.println("│ ✅ CRITERIA 2 MATCHED!");
                    System.out.println("│    Required: " + requiredQtyKg + " KG ≈ Bundle: " + bundleQty + " KG (within ±5%)");
                    System.out.println("│    → Selecting this bundle from WAREHOUSE");
                    System.out.println("│    ➕ Selected: Bundle " + bundle.get("bundleId") + " | " + bundleQty + " KG");

                    selectedBundles.add(bundle);
                    totalSelectedQtyKg = bundleQty;
                    storageType = "WAREHOUSE";
                    selectionReason = "Required Qty (" + requiredQtyKg + " KG) ≈ Bundle Qty (" + bundleQty + " KG) within ±5% → Selected from Warehouse (Earliest GRN first)";
                    selectionDone = true;
                    break;
                }
            }
            if (!found) {
                System.out.println("│ ❌ No single bundle found within ±5% tolerance");
            }
        } else if (!selectionDone) {
            System.out.println("│ ⏭️ SKIPPED: Already selected or insufficient stock");
        }
        System.out.println("│ Status: " + (selectionDone ? "✅ SELECTION DONE" : "➡️ Continue to next criteria"));
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CRITERIA 3: Required Qty ≈ Sum of Multiple Bundles (±5% tolerance) → WAREHOUSE
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n┌─ CRITERIA 3: Required Qty ≈ Sum of Multiple Bundles (±5%)? ─────────────────┐");
        System.out.println("│ Condition: Sum of multiple bundles within ±5% of required                   │");
        System.out.println("│ Action   : Select those bundles from WAREHOUSE                              │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        if (!selectionDone && totalWithQR.compareTo(requiredQtyKg) >= 0) {
            BigDecimal runningTotal = BigDecimal.ZERO;
            List<java.util.Map<String, Object>> tempSelected = new ArrayList<>();

            for (java.util.Map<String, Object> bundle : warehouseBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                runningTotal = runningTotal.add(bundleQty);
                tempSelected.add(bundle);

                System.out.println("│ + Bundle " + bundle.get("bundleId") + " | " + bundleQty + " KG | Running Total: " + runningTotal + " KG");

                // Check if sum is within tolerance
                if (runningTotal.compareTo(lowerTolerance) >= 0 && runningTotal.compareTo(upperTolerance) <= 0) {
                    System.out.println("│ ✅ CRITERIA 3 MATCHED!");
                    System.out.println("│    Required: " + requiredQtyKg + " KG ≈ Sum: " + runningTotal + " KG (within ±5%)");
                    System.out.println("│    → Selecting " + tempSelected.size() + " bundles from WAREHOUSE");

                    selectedBundles.addAll(tempSelected);
                    totalSelectedQtyKg = runningTotal;
                    storageType = "WAREHOUSE";
                    selectionReason = "Required Qty (" + requiredQtyKg + " KG) ≈ Sum of " + tempSelected.size() + " bundles (" + runningTotal + " KG) within ±5% → Selected from Warehouse (FIFO)";
                    selectionDone = true;
                    break;
                }

                // If exceeded upper tolerance, stop
                if (runningTotal.compareTo(upperTolerance) > 0) {
                    System.out.println("│ ⚠️ Sum (" + runningTotal + ") exceeded upper tolerance (" + upperTolerance + ") - stopping");
                    break;
                }
            }
            if (!selectionDone) {
                System.out.println("│ ❌ Could not find combination within ±5% tolerance");
            }
        } else if (!selectionDone) {
            System.out.println("│ ⏭️ SKIPPED: Already selected or insufficient stock");
        }
        System.out.println("│ Status: " + (selectionDone ? "✅ SELECTION DONE" : "➡️ Continue to next criteria"));
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CRITERIA 4: Sum outside tolerance but Loose Piece available → WAREHOUSE + LOOSE PIECE
        // (Select one bundle less than required, remaining from loose piece)
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n┌─ CRITERIA 4: Warehouse + Loose Piece Combination? ───────────────────────────┐");
        System.out.println("│ Condition: Sum outside tolerance, Loose Piece available                      │");
        System.out.println("│ Action   : Select bundles under required + remaining from Loose Piece        │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        if (!selectionDone && totalWithQR.compareTo(requiredQtyKg) >= 0 && !loosePieceBundles.isEmpty()) {
            System.out.println("│ Loose Piece bundles available: " + loosePieceBundles.size());

            // Find bundles that are just under the required quantity
            BigDecimal runningTotal = BigDecimal.ZERO;
            List<java.util.Map<String, Object>> tempSelected = new ArrayList<>();

            for (java.util.Map<String, Object> bundle : warehouseBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                BigDecimal potentialTotal = runningTotal.add(bundleQty);

                // Stop when adding this bundle would exceed required qty
                if (potentialTotal.compareTo(requiredQtyKg) >= 0) {
                    System.out.println("│ ⏹️ Stop: Adding Bundle " + bundle.get("bundleId") + " (" + bundleQty + " KG) would exceed required");
                    break;
                }

                runningTotal = potentialTotal;
                tempSelected.add(bundle);
                System.out.println("│ + Bundle " + bundle.get("bundleId") + " | " + bundleQty + " KG | Running Total: " + runningTotal + " KG");
            }

            if (!tempSelected.isEmpty()) {
                BigDecimal remainingQty = requiredQtyKg.subtract(runningTotal);
                System.out.println("│ Warehouse bundles selected: " + tempSelected.size() + " = " + runningTotal + " KG");
                System.out.println("│ Remaining needed from Loose Piece: " + remainingQty + " KG");

                // Add loose pieces for remaining (FIFO)
                BigDecimal looseTotal = BigDecimal.ZERO;
                List<java.util.Map<String, Object>> looseSelected = new ArrayList<>();

                for (java.util.Map<String, Object> looseBundle : loosePieceBundles) {
                    BigDecimal looseQty = getBundleQtyKg.apply(looseBundle);
                    looseSelected.add(looseBundle);
                    looseTotal = looseTotal.add(looseQty);
                    System.out.println("│ + Loose Bundle " + looseBundle.get("bundleId") + " | " + looseQty + " KG | Loose Total: " + looseTotal + " KG");

                    if (looseTotal.compareTo(remainingQty) >= 0) {
                        break;
                    }
                }

                BigDecimal combinedTotal = runningTotal.add(looseTotal);

                if (combinedTotal.compareTo(lowerTolerance) >= 0) {
                    System.out.println("│ ✅ CRITERIA 4 MATCHED!");
                    System.out.println("│    Warehouse: " + runningTotal + " KG + Loose Piece: " + looseTotal + " KG = " + combinedTotal + " KG");
                    System.out.println("│    → Selecting from WAREHOUSE + LOOSE PIECE");

                    selectedBundles.addAll(tempSelected);
                    selectedBundles.addAll(looseSelected);
                    totalSelectedQtyKg = combinedTotal;
                    storageType = "WAREHOUSE_PLUS_LOOSE_PIECE";
                    selectionReason = "Sum outside tolerance → Warehouse (" + runningTotal + " KG) + Loose Piece (" + looseTotal + " KG) = " + combinedTotal + " KG (FIFO)";
                    selectionDone = true;
                } else {
                    System.out.println("│ ❌ Combined total (" + combinedTotal + " KG) < lower tolerance (" + lowerTolerance + " KG)");
                }
            } else {
                System.out.println("│ ❌ No warehouse bundles selected under required qty");
            }
        } else if (!selectionDone) {
            System.out.println("│ ⏭️ SKIPPED: Already selected or no loose pieces available");
        }
        System.out.println("│ Status: " + (selectionDone ? "✅ SELECTION DONE" : "➡️ Continue to next criteria"));
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CRITERIA 5: Sum outside tolerance, NO Loose Piece → WAREHOUSE (fulfill required)
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n┌─ CRITERIA 5: Warehouse Only (No Loose Piece)? ───────────────────────────────┐");
        System.out.println("│ Condition: Sum outside tolerance, No Loose Piece available                   │");
        System.out.println("│ Action   : Select bundles to fulfill required from WAREHOUSE                 │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        if (!selectionDone && totalWithQR.compareTo(requiredQtyKg) >= 0) {
            BigDecimal runningTotal = BigDecimal.ZERO;
            List<java.util.Map<String, Object>> tempSelected = new ArrayList<>();

            for (java.util.Map<String, Object> bundle : warehouseBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                runningTotal = runningTotal.add(bundleQty);
                tempSelected.add(bundle);
                System.out.println("│ + Bundle " + bundle.get("bundleId") + " | " + bundleQty + " KG | Running Total: " + runningTotal + " KG");

                if (runningTotal.compareTo(requiredQtyKg) >= 0) {
                    break;
                }
            }

            if (runningTotal.compareTo(requiredQtyKg) >= 0) {
                System.out.println("│ ✅ CRITERIA 5 MATCHED!");
                System.out.println("│    Selected " + tempSelected.size() + " bundles = " + runningTotal + " KG");
                System.out.println("│    → Selecting from WAREHOUSE to fulfill required quantity");

                selectedBundles.addAll(tempSelected);
                totalSelectedQtyKg = runningTotal;
                storageType = "WAREHOUSE";
                selectionReason = "Sum outside tolerance, no Loose Piece → Selected " + tempSelected.size() + " bundles (" + runningTotal + " KG) from Warehouse (FIFO)";
                selectionDone = true;
            } else {
                System.out.println("│ ❌ Could not reach required qty. Running total: " + runningTotal + " KG");
            }
        } else if (!selectionDone) {
            System.out.println("│ ⏭️ SKIPPED: Already selected or insufficient stock");
        }
        System.out.println("│ Status: " + (selectionDone ? "✅ SELECTION DONE" : "➡️ Continue to next criteria"));
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CRITERIA 6: Stock with QR < Required Qty → WAREHOUSE + LOOSE PIECE + COMMON
        // (Use all available with QR + remaining from Common without QR)
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n┌─ CRITERIA 6: Warehouse + Loose Piece + Common (No QR)? ──────────────────────┐");
        System.out.println("│ Condition: Stock with QR < Required, Common available for remaining          │");
        System.out.println("│ Action   : Use all with QR + remaining from COMMON (without QR)              │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        if (!selectionDone && totalWithQR.compareTo(requiredQtyKg) < 0 && grandTotal.compareTo(requiredQtyKg) >= 0) {
            System.out.println("│ Total with QR: " + totalWithQR + " KG < Required: " + requiredQtyKg + " KG");
            System.out.println("│ Common stock available: " + totalCommonQty + " KG");
            System.out.println("│ Grand Total: " + grandTotal + " KG >= Required: " + requiredQtyKg + " KG");

            // Add all warehouse bundles
            System.out.println("│ Adding ALL Warehouse bundles (" + warehouseBundles.size() + "):");
            for (java.util.Map<String, Object> b : warehouseBundles) {
                System.out.println("│   ➕ Bundle " + b.get("bundleId") + " | " + getBundleQtyKg.apply(b) + " KG");
            }
            selectedBundles.addAll(warehouseBundles);
            totalSelectedQtyKg = totalWarehouseQty;

            // Add all loose piece bundles
            System.out.println("│ Adding ALL Loose Piece bundles (" + loosePieceBundles.size() + "):");
            for (java.util.Map<String, Object> b : loosePieceBundles) {
                System.out.println("│   ➕ Bundle " + b.get("bundleId") + " | " + getBundleQtyKg.apply(b) + " KG");
            }
            selectedBundles.addAll(loosePieceBundles);
            totalSelectedQtyKg = totalSelectedQtyKg.add(totalLoosePieceQty);

            BigDecimal remainingFromCommon = requiredQtyKg.subtract(totalSelectedQtyKg);
            System.out.println("│ After Warehouse + Loose: " + totalSelectedQtyKg + " KG");
            System.out.println("│ Remaining from Common: " + remainingFromCommon + " KG");

            // Add common entries for remaining (FIFO)
            BigDecimal commonUsed = BigDecimal.ZERO;
            System.out.println("│ Adding from Common (without QR):");
            for (java.util.Map<String, Object> commonEntry : commonBundles) {
                BigDecimal commonQty = getBundleQtyKg.apply(commonEntry);
                selectedBundles.add(commonEntry);
                commonUsed = commonUsed.add(commonQty);
                totalSelectedQtyKg = totalSelectedQtyKg.add(commonQty);
                System.out.println("│   ➕ Common Entry " + commonEntry.get("bundleId") + " | " + commonQty + " KG | Total: " + totalSelectedQtyKg + " KG");

                if (totalSelectedQtyKg.compareTo(requiredQtyKg) >= 0) {
                    break;
                }
            }

            System.out.println("│ ✅ CRITERIA 6 MATCHED!");
            System.out.println("│    Total selected: " + totalSelectedQtyKg + " KG");
            System.out.println("│    → WAREHOUSE + LOOSE PIECE + COMMON");

            storageType = "WAREHOUSE_PLUS_LOOSE_PIECE_PLUS_COMMON";
            selectionReason = "Stock with QR (" + totalWithQR + " KG) < Required (" + requiredQtyKg + " KG) → Used Warehouse + Loose Piece + Common (" + commonUsed + " KG without QR)";
            selectionDone = true;
        } else if (!selectionDone) {
            System.out.println("│ ⏭️ SKIPPED: Already selected or insufficient grand total");
        }
        System.out.println("│ Status: " + (selectionDone ? "✅ SELECTION DONE" : "➡️ Continue to next criteria"));
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // CRITERIA 7: NO Stock with QR available → COMMON only (without QR)
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n┌─ CRITERIA 7: Common Only (No QR Stock)? ────────────────────────────────────┐");
        System.out.println("│ Condition: No stock with QR available, Common stock >= Required              │");
        System.out.println("│ Action   : Select from COMMON only (without QR)                              │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");

        if (!selectionDone && totalWithQR.compareTo(BigDecimal.ZERO) == 0 && totalCommonQty.compareTo(requiredQtyKg) >= 0) {
            System.out.println("│ No stock with QR available (Total with QR = 0)");
            System.out.println("│ Common stock: " + totalCommonQty + " KG >= Required: " + requiredQtyKg + " KG");

            BigDecimal runningTotal = BigDecimal.ZERO;
            for (java.util.Map<String, Object> commonEntry : commonBundles) {
                BigDecimal commonQty = getBundleQtyKg.apply(commonEntry);
                selectedBundles.add(commonEntry);
                runningTotal = runningTotal.add(commonQty);
                System.out.println("│ + Common Entry " + commonEntry.get("bundleId") + " | " + commonQty + " KG | Running Total: " + runningTotal + " KG");

                if (runningTotal.compareTo(requiredQtyKg) >= 0) {
                    break;
                }
            }

            totalSelectedQtyKg = runningTotal;

            System.out.println("│ ✅ CRITERIA 7 MATCHED!");
            System.out.println("│    Selected: " + runningTotal + " KG from Common (without QR)");
            System.out.println("│    → WAREHOUSE (COMMON) only");

            storageType = "WAREHOUSE_COMMON";
            selectionReason = "No stock with QR available → Selected " + runningTotal + " KG from Common (without QR) - FIFO";
            selectionDone = true;
        } else if (!selectionDone) {
            System.out.println("│ ⏭️ SKIPPED: Stock with QR available or insufficient common stock");
        }
        System.out.println("│ Status: " + (selectionDone ? "✅ SELECTION DONE" : "❌ NO STOCK AVAILABLE"));
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // NO STOCK AVAILABLE
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        if (!selectionDone) {
            System.out.println("\n┌─ ❌ NO SUITABLE STOCK AVAILABLE ─────────────────────────────────────────────┐");
            System.out.println("│ Required: " + requiredQtyKg + " KG");
            System.out.println("│ Total Available: " + grandTotal + " KG");
            System.out.println("│ Total with QR: " + totalWithQR + " KG");
            System.out.println("│ Total Common: " + totalCommonQty + " KG");
            System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

            storageType = "NO_STOCK_AVAILABLE";
            selectionReason = "No suitable stock available. Required: " + requiredQtyKg + " KG, Available: " + grandTotal + " KG";
        }

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // FINAL SELECTION SUMMARY
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            📋 SELECTION COMPLETE - FULL ORDER                               ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Storage Type    : " + storageType);
        System.out.println("║ Bundles Selected: " + selectedBundles.size());
        System.out.println("║ Total Qty (KG)  : " + totalSelectedQtyKg);
        System.out.println("║ Required Qty    : " + requiredQtyKg + " KG");
        System.out.println("║ Reason          : " + selectionReason);
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");

        System.out.println("\n┌─ SELECTED BUNDLES (FIFO Order) ─────────────────────────────────────────────┐");

        // ═══════════════════════════════════════════════════════════════════════════════════════════
        // BUILD RESPONSE
        // ═══════════════════════════════════════════════════════════════════════════════════════════
        java.util.Map<String, Object> responseData = new java.util.LinkedHashMap<>();
        responseData.put("unit", dto.getUnit());
        responseData.put("itemDescription", dto.getItemDescription());
        responseData.put("dimension", dto.getDimension());
        responseData.put("soNumber", dto.getSoNumber());
        responseData.put("lineNumber", dto.getLineNumber());
        responseData.put("orderType", dto.getOrderType());
        responseData.put("requiredQuantityKg", dto.getRequiredQuantityKg());
        responseData.put("requiredQuantityNo", dto.getRequiredQuantityNo());
        responseData.put("racks", rackDetailsMap);
        responseData.put("totalRacks", rackDetailsMap.size());
        responseData.put("grnDetails", grnDetailsMap);

        // Add selection result
        java.util.Map<String, Object> selectionResult = new java.util.LinkedHashMap<>();
        selectionResult.put("storageType", storageType);
        selectionResult.put("selectionReason", selectionReason);
        selectionResult.put("selectedBundles", selectedBundles);
        selectionResult.put("totalSelectedQuantityKg", totalSelectedQtyKg);
        selectionResult.put("toleranceRange", java.util.Map.of(
                "lower", lowerTolerance,
                "upper", upperTolerance,
                "percent", "±5%"
        ));

        // Add availability summary
        selectionResult.put("availabilitySummary", java.util.Map.of(
                "warehouseWithQR", totalWarehouseQty,
                "loosePiece", totalLoosePieceQty,
                "commonWithoutQR", totalCommonQty,
                "totalWithQR", totalWithQR,
                "grandTotal", grandTotal
        ));

        // Shortfall Alert — when total selected < required (FRD requirement)
        if (totalSelectedQtyKg.compareTo(requiredQtyKg) < 0) {
            BigDecimal shortfallKg = requiredQtyKg.subtract(totalSelectedQtyKg);
            java.util.Map<String, Object> shortfallAlert = new java.util.LinkedHashMap<>();
            shortfallAlert.put("hasShortfall", true);
            shortfallAlert.put("shortfallQuantityKg", shortfallKg);
            shortfallAlert.put("allocatedQuantityKg", totalSelectedQtyKg);
            shortfallAlert.put("requiredQuantityKg", requiredQtyKg);
            shortfallAlert.put("message", "Insufficient stock. Shortfall: " + shortfallKg + " Kg. Consider raising a Purchase Indent for the remaining quantity.");
            selectionResult.put("shortfallAlert", shortfallAlert);
            System.out.println("   ⚠️ SHORTFALL ALERT: Required=" + requiredQtyKg + " KG | Allocated=" + totalSelectedQtyKg + " KG | Shortfall=" + shortfallKg + " KG");
        }

        // Add detailed bundle breakdown
        System.out.println("\n   📋 SELECTED BUNDLES SUMMARY:");

        // Ensure grnRefNo is present in all selected bundles
        for (java.util.Map<String, Object> bundle : selectedBundles) {
            if (!bundle.containsKey("grnRefNo") || bundle.get("grnRefNo") == null) {
                System.out.println("   ⚠️ WARNING: Bundle missing grnRefNo - " + bundle.get("bundleId"));
            } else {
                System.out.println("   ✅ Bundle has grnRefNo: " + bundle.get("grnRefNo"));
            }
        }

        // Sort selectedBundles by GRN timestamp (FIFO - earliest first) before creating response
        selectedBundles.sort((a, b) -> {
            Long tsA = a.get("grnTimestamp") != null ? ((Number) a.get("grnTimestamp")).longValue() : Long.MAX_VALUE;
            Long tsB = b.get("grnTimestamp") != null ? ((Number) b.get("grnTimestamp")).longValue() : Long.MAX_VALUE;
            return tsA.compareTo(tsB);
        });

        System.out.println("   📦 Sorted by GRN Timestamp (FIFO - Earliest First):");

        List<java.util.Map<String, Object>> bundleBreakdown = new ArrayList<>();
        BigDecimal totalQtyVerify = BigDecimal.ZERO;

        for (int i = 0; i < selectedBundles.size(); i++) {
            java.util.Map<String, Object> bundle = selectedBundles.get(i);
            BigDecimal qty = getBundleQtyKg.apply(bundle);
            totalQtyVerify = totalQtyVerify.add(qty);

            java.util.Map<String, Object> bundleInfo = new java.util.LinkedHashMap<>();
            bundleInfo.put("slNo", i + 1);
            bundleInfo.put("bundleId", bundle.get("bundleId"));
            bundleInfo.put("quantityKg", qty);
            bundleInfo.put("quantityNo", bundle.get("quantityNo"));
            bundleInfo.put("grnRefNo", bundle.get("grnRefNo"));
            bundleInfo.put("grnTimestamp", bundle.get("grnTimestamp"));
            bundleInfo.put("store", bundle.get("store"));
            bundleInfo.put("storageArea", bundle.get("storageArea"));
            bundleInfo.put("rack", bundle.get("rack"));
            bundleInfo.put("itemDescription", bundle.get("itemDescription"));
            bundleInfo.put("dimension", bundle.get("dimension"));
            bundleInfo.put("brand", bundle.get("brand"));
            bundleInfo.put("grade", bundle.get("grade"));
            bundleInfo.put("temper", bundle.get("temper"));
            bundleInfo.put("qrCode", bundle.get("qrCode"));
            bundleInfo.put("qrCodeImageUrl", bundle.get("qrCodeImageUrl"));

            bundleBreakdown.add(bundleInfo);

            System.out.println("   [" + (i + 1) + "] Bundle ID: " + bundle.get("bundleId") +
                             " | Qty: " + qty + " KG | GRN: " + bundle.get("grnRefNo") +
                             " | Dim: " + bundle.get("dimension") +
                             " | Store: " + bundle.get("store") + " | Rack: " + bundle.get("rack"));
        }

        System.out.println("   📊 Total Selected: " + totalQtyVerify + " KG (" + selectedBundles.size() + " bundles)");

        selectionResult.put("bundleBreakdown", bundleBreakdown);
        selectionResult.put("totalBundlesSelected", selectedBundles.size());

        responseData.put("selectionResult", selectionResult);

        System.out.println("✅ [PickList] Selection logic complete");

        // ========== AUTO SAVE PICKLIST ==========
        System.out.println("\n💾 [PickList] Auto-saving pick list...");
        try {
            // Build PickListDTOs from selection result
            PickListDTOs pickListDto = new PickListDTOs();
            pickListDto.setUnit(dto.getUnit());
            pickListDto.setSoNumber(dto.getSoNumber());
            pickListDto.setLineNumber(dto.getLineNumber());
            pickListDto.setItemDescription(dto.getItemDescription());
            pickListDto.setOrderType(dto.getOrderType());
            pickListDto.setStorageType(storageType);
            pickListDto.setSelectionReason(selectionReason);
            pickListDto.setTotalSelectedQuantityKg(totalSelectedQtyKg);
            pickListDto.setTotalBundlesSelected(selectedBundles.size());

            // Convert selected bundles to SelectedBundleDTO list
            List<PickListDTOs.SelectedBundleDTO> bundleDtos = new ArrayList<>();
            for (java.util.Map<String, Object> bundle : selectedBundles) {
                PickListDTOs.SelectedBundleDTO bundleDto = new PickListDTOs.SelectedBundleDTO();
                bundleDto.setBundleId(parseLong(bundle.get("bundleId")));
                bundleDto.setSlNo(parseInteger(bundle.get("slNo")));
                bundleDto.setGrnRefNo(parseString(bundle.get("grnRefNo")));
                bundleDto.setGrnTimestamp(parseLong(bundle.get("grnTimestamp")));
                bundleDto.setStore(parseString(bundle.get("store")));
                bundleDto.setStorageArea(parseString(bundle.get("storageArea")));
                bundleDto.setRack(parseString(bundle.get("rack")));
                bundleDto.setItemDescription(parseString(bundle.get("itemDescription")));
                bundleDto.setProductCategory(parseString(bundle.get("productCategory")));
                bundleDto.setBrand(parseString(bundle.get("brand")));
                bundleDto.setGrade(parseString(bundle.get("grade")));
                bundleDto.setTemper(parseString(bundle.get("temper")));
                bundleDto.setQuantityKg(parseBigDecimal(bundle.get("quantityKg")));
                bundleDto.setQuantityNo(parseInteger(bundle.get("quantityNo")));
                bundleDto.setQrCode(parseString(bundle.get("qrCode")));
                bundleDto.setQrCodeImageUrl(parseString(bundle.get("qrCodeImageUrl")));
                bundleDtos.add(bundleDto);
            }
            pickListDto.setSelectedBundles(bundleDtos);

            // Set store, storageArea, rack from first bundle
            if (!selectedBundles.isEmpty()) {
                java.util.Map<String, Object> firstBundle = selectedBundles.get(0);
                pickListDto.setStore(parseString(firstBundle.get("store")));
                pickListDto.setStorageArea(parseString(firstBundle.get("storageArea")));
                pickListDto.setRackColumnShelfNumber(parseString(firstBundle.get("rack")));
                pickListDto.setBrand(parseString(firstBundle.get("brand")));
                pickListDto.setProductCategory(parseString(firstBundle.get("productCategory")));
                pickListDto.setRetrievalQuantityKg(totalSelectedQtyKg);

                // Calculate total quantityNo
                int totalQtyNo = selectedBundles.stream()
                        .map(b -> parseInteger(b.get("quantityNo")) != null ? parseInteger(b.get("quantityNo")) : 0)
                        .reduce(0, Integer::sum);
                pickListDto.setRetrievalQuantityNo(totalQtyNo);
            }

            // Save using existing method
            java.util.Map<String, Object> saveResult = savePickListWithBundles(List.of(pickListDto));
            System.out.println("   ✅ Auto-save complete: " + saveResult.get("message"));

            responseData.put("autoSaved", true);
            responseData.put("saveResult", saveResult);

        } catch (Exception e) {
            System.out.println("   ❌ Auto-save failed: " + e.getMessage());
            responseData.put("autoSaved", false);
            responseData.put("autoSaveError", e.getMessage());
        }

        return java.util.Map.of(
                "success", true,
                "message", "Pick list generated and saved successfully",
                "data", responseData
        );
    }

    @Override
    public java.util.Map<String, Object> generatePickListForCutOrder(SalesOrderSchedulerDTO dto) {
        System.out.println("\n═══════════════════════════════════════════════════════════════════════════════");
        System.out.println("║ 📋 [PickList-CUT] STARTING PICK LIST GENERATION FOR CUT ORDERS              ║");
        System.out.println("═══════════════════════════════════════════════════════════════════════════════");

        // Check if DTO is null
        if (dto == null) {
            System.out.println("   ⚠️ No data received!");
            return java.util.Map.of(
                    "success", false,
                    "message", "No data provided",
                    "data", new ArrayList<>()
            );
        }

        System.out.println("   📦 SO Number: " + dto.getSoNumber());
        System.out.println("   📦 Line Number: " + dto.getLineNumber());
        System.out.println("   📦 Unit: " + dto.getUnit());
        System.out.println("   📦 Item Description: " + dto.getItemDescription());
        System.out.println("   📦 Product Category: " + dto.getProductCategory());
        System.out.println("   📐 Required Dimension: " + dto.getDimension());
        System.out.println("   📊 Required Quantity: " + dto.getRequiredQuantityKg() + " KG");

        // Check if orderType is CUT
        if (!"CUT".equalsIgnoreCase(dto.getOrderType())) {
            System.out.println("   ⚠️ Order type is not CUT. Actual: " + dto.getOrderType());
            return java.util.Map.of(
                    "success", false,
                    "message", "Order type must be CUT. Current: " + dto.getOrderType(),
                    "data", new ArrayList<>()
            );
        }

        // ═══════════════════════════════════════════════════════════════════════════════════
        // PARSE REQUIRED DIMENSION BASED ON PRODUCT CATEGORY
        // ═══════════════════════════════════════════════════════════════════════════════════
        // Product Category Dimension Mapping:
        // COIL: Thickness x Width (2 parts)
        // ROUND ROD: Diameter x Length (2 parts)
        // SHEET: Thickness x Width x Length (3 parts)
        // PLATE: Thickness x Width x Length (3 parts)
        // SQUARE BAR: Square Side x Length (2 parts)
        // ═══════════════════════════════════════════════════════════════════════════════════

        String productCategory = dto.getProductCategory() != null ? dto.getProductCategory().toUpperCase().trim() : "";
        String requiredDimensionStr = dto.getDimension() != null ? dto.getDimension().trim() : "";
        String[] requiredDimParts = requiredDimensionStr.split("[Xx×\\s]+");

        BigDecimal reqDim1 = BigDecimal.ZERO; // Thickness/Diameter/Square Side
        BigDecimal reqDim2 = BigDecimal.ZERO; // Width/Length
        BigDecimal reqDim3 = BigDecimal.ZERO; // Length (for 3-part dimensions)
        boolean hasDimensionFilter = false;
        String dim1Label = "Thickness";
        String dim2Label = "Width";
        String dim3Label = "Length";

        System.out.println("\n   🔍 Parsing dimension for category: " + productCategory);

        switch (productCategory) {
            case "COIL":
                // Thickness x Width
                dim1Label = "Thickness";
                dim2Label = "Width";
                dim3Label = "N/A";
                if (requiredDimParts.length >= 2) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing COIL dimension: " + e.getMessage());
                    }
                }
                break;

            case "ROUND ROD":
                // Diameter x Length
                dim1Label = "Diameter";
                dim2Label = "Length";
                dim3Label = "N/A";
                if (requiredDimParts.length >= 2) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing ROUND ROD dimension: " + e.getMessage());
                    }
                }
                break;

            case "SHEET":
            case "PLATE":
                // Thickness x Width x Length
                dim1Label = "Thickness";
                dim2Label = "Width";
                dim3Label = "Length";
                if (requiredDimParts.length >= 3) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        reqDim3 = new BigDecimal(requiredDimParts[2].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing SHEET/PLATE dimension: " + e.getMessage());
                    }
                }
                break;

            case "SQUARE BAR":
                // Square Side x Length
                dim1Label = "Square Side";
                dim2Label = "Length";
                dim3Label = "N/A";
                if (requiredDimParts.length >= 2) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing SQUARE BAR dimension: " + e.getMessage());
                    }
                }
                break;

            case "HEX BAR":
                // Hex dimension x Length
                dim1Label = "Hex";
                dim2Label = "Length";
                dim3Label = "N/A";
                if (requiredDimParts.length >= 2) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing HEX BAR dimension: " + e.getMessage());
                    }
                }
                break;

            case "EXTRUSION":
                // Kg/Mtr x Length
                dim1Label = "Kg/Mtr";
                dim2Label = "Length";
                dim3Label = "N/A";
                if (requiredDimParts.length >= 2) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing EXTRUSION dimension: " + e.getMessage());
                    }
                }
                break;

            default:
                // Generic parsing - try 3 parts first, then 2
                if (requiredDimParts.length >= 3) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        reqDim3 = new BigDecimal(requiredDimParts[2].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing 3-part dimension: " + e.getMessage());
                    }
                } else if (requiredDimParts.length >= 2) {
                    try {
                        reqDim1 = new BigDecimal(requiredDimParts[0].trim());
                        reqDim2 = new BigDecimal(requiredDimParts[1].trim());
                        hasDimensionFilter = true;
                    } catch (NumberFormatException e) {
                        System.out.println("   ⚠️ Error parsing 2-part dimension: " + e.getMessage());
                    }
                }
        }

        System.out.println("   📐 Parsed Dimensions:");
        System.out.println("      - " + dim1Label + " (Dim1): " + reqDim1);
        System.out.println("      - " + dim2Label + " (Dim2): " + reqDim2);
        if (reqDim3.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("      - " + dim3Label + " (Dim3): " + reqDim3);
        }
        System.out.println("   📐 Dimension Filter Active: " + hasDimensionFilter);

        BigDecimal requiredQtyKg = dto.getRequiredQuantityKg() != null ? dto.getRequiredQuantityKg() : BigDecimal.ZERO;

        final BigDecimal finalReqDim1 = reqDim1;
        final BigDecimal finalReqDim2 = reqDim2;
        final BigDecimal finalReqDim3 = reqDim3;
        final boolean finalHasDimensionFilter = hasDimensionFilter;
        final String finalProductCategory = productCategory;

        // ═══════════════════════════════════════════════════════════════════════════════════
        // STEP 1: FILTER BY UNIT + ITEM GROUP = RM + ITEM DESCRIPTION
        // ═══════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n   🔍 STEP 1: Initial Filtering");
        System.out.println("      - Unit: " + dto.getUnit());
        System.out.println("      - Item Group: RAW MATERIAL");
        System.out.println("      - Item Description: " + dto.getItemDescription());

        List<StockSummaryEntity> stockList = stockSummaryRepository.findAll();

        List<StockSummaryEntity> filteredStocks = stockList.stream()
                .filter(stock -> stock.getUnit() != null && stock.getUnit().trim().equalsIgnoreCase(dto.getUnit().trim()))
                .filter(stock -> stock.getItemGroup() != null && stock.getItemGroup().trim().equalsIgnoreCase("RAW MATERIAL"))
                .filter(stock -> stock.getItemDescription() != null && stock.getItemDescription().trim().equalsIgnoreCase(dto.getItemDescription().trim()))
                .collect(Collectors.toList());

        System.out.println("      → Filtered count: " + filteredStocks.size());

        // ═══════════════════════════════════════════════════════════════════════════════════
        // STEP 2: CATEGORIZE & APPLY DIMENSION RULES
        // ═══════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n   🔍 STEP 2: Categorizing stocks and applying dimension rules");
        System.out.println("      Storage Priority: End Piece → Loose Piece → Warehouse");

        // Storage Priority Order for CUT
        List<String> storePriority = List.of("End Piece", "Loose Piece", "Warehouse");

        // Categorized stocks
        List<java.util.Map<String, Object>> exactMatchStocks = new ArrayList<>();   // Case 1 & 2
        List<java.util.Map<String, Object>> higherDimStocks = new ArrayList<>();     // Case 3
        List<java.util.Map<String, Object>> doNotRecommendStocks = new ArrayList<>(); // Case 4

        for (StockSummaryEntity stock : filteredStocks) {
            String storeRaw = stock.getStore() != null ? stock.getStore().trim() : "";
            String storageAreaRaw = stock.getStorageArea() != null ? stock.getStorageArea().trim() : "";

            // Determine store type
            String storeType = "Warehouse";
            String combinedStore = (storeRaw + " " + storageAreaRaw).toLowerCase();
            if (combinedStore.contains("end") && combinedStore.contains("piece")) {
                storeType = "End Piece";
            } else if (combinedStore.contains("loose") && combinedStore.contains("piece")) {
                storeType = "Loose Piece";
            }

            // Parse stock dimension
            String stockDimension = stock.getDimension() != null ? stock.getDimension().trim() : "";
            String[] stockDimParts = stockDimension.split("[Xx×\\s]+");

            BigDecimal stockDim1 = BigDecimal.ZERO;
            BigDecimal stockDim2 = BigDecimal.ZERO;
            BigDecimal stockDim3 = BigDecimal.ZERO;
            boolean stockHasValidDimension = false;

            if (stockDimParts.length >= 3) {
                try {
                    stockDim1 = new BigDecimal(stockDimParts[0].trim());
                    stockDim2 = new BigDecimal(stockDimParts[1].trim());
                    stockDim3 = new BigDecimal(stockDimParts[2].trim());
                    stockHasValidDimension = true;
                } catch (NumberFormatException e) { /* ignore */ }
            } else if (stockDimParts.length >= 2) {
                try {
                    stockDim1 = new BigDecimal(stockDimParts[0].trim());
                    stockDim2 = new BigDecimal(stockDimParts[1].trim());
                    stockHasValidDimension = true;
                } catch (NumberFormatException e) { /* ignore */ }
            }

            // Create stock info map
            java.util.Map<String, Object> stockInfo = new java.util.LinkedHashMap<>();
            stockInfo.put("stockId", stock.getId());
            stockInfo.put("store", storeRaw);
            stockInfo.put("storeType", storeType);
            stockInfo.put("storageArea", storageAreaRaw);
            stockInfo.put("rack", stock.getRackColumnShelfNumber());
            stockInfo.put("itemDescription", stock.getItemDescription());
            stockInfo.put("productCategory", stock.getProductCategory());
            stockInfo.put("brand", stock.getBrand());
            stockInfo.put("grade", stock.getGrade());
            stockInfo.put("temper", stock.getTemper());
            stockInfo.put("dimension", stockDimension);
            stockInfo.put("dim1", stockDim1);
            stockInfo.put("dim2", stockDim2);
            stockInfo.put("dim3", stockDim3);
            stockInfo.put("quantityKg", stock.getQuantityKg());
            stockInfo.put("quantityNo", stock.getQuantityNo());
            stockInfo.put("grnNumbers", stock.getGrnNumbers());
            stockInfo.put("itemGroup", stock.getItemGroup());

            // Apply dimension rules
            if (!finalHasDimensionFilter || !stockHasValidDimension) {
                // No dimension filter - treat as exact match
                stockInfo.put("matchType", "EXACT");
                exactMatchStocks.add(stockInfo);
                System.out.println("      ✅ Stock ID: " + stock.getId() + " | Store: " + storeType + " | No dim filter - EXACT MATCH");
                continue;
            }

            // Check dimension matching based on product category
            boolean dim1Match = stockDim1.compareTo(finalReqDim1) == 0;  // Thickness/Diameter must MATCH
            boolean dim2Match = stockDim2.compareTo(finalReqDim2) == 0;
            boolean dim3Match = finalReqDim3.compareTo(BigDecimal.ZERO) == 0 || stockDim3.compareTo(finalReqDim3) == 0;
            boolean dim2Greater = stockDim2.compareTo(finalReqDim2) > 0;
            boolean dim3Greater = finalReqDim3.compareTo(BigDecimal.ZERO) == 0 || stockDim3.compareTo(finalReqDim3) > 0;
            boolean dim2Less = stockDim2.compareTo(finalReqDim2) < 0;
            boolean dim3Less = finalReqDim3.compareTo(BigDecimal.ZERO) > 0 && stockDim3.compareTo(finalReqDim3) < 0;

            boolean allDimensionsMatch = dim1Match && dim2Match && dim3Match;
            boolean hasHigherDimensions = dim1Match && (dim2Greater || dim2Match) && (dim3Greater || dim3Match) && (dim2Greater || dim3Greater);
            boolean hasSmallerDimension = dim1Match && (dim2Less || dim3Less);

            stockInfo.put("dim1Match", dim1Match);
            stockInfo.put("allDimensionsMatch", allDimensionsMatch);
            stockInfo.put("hasHigherDimensions", hasHigherDimensions);
            stockInfo.put("hasSmallerDimension", hasSmallerDimension);

            // CASE 4: DO NOT RECOMMEND - Dim1 matches but Dim2 OR Dim3 is smaller
            if (dim1Match && hasSmallerDimension && !allDimensionsMatch) {
                stockInfo.put("matchType", "DO_NOT_RECOMMEND");
                doNotRecommendStocks.add(stockInfo);
                System.out.println("      ❌ Stock ID: " + stock.getId() + " | Store: " + storeType +
                        " | Dim: " + stockDimension + " | DO NOT RECOMMEND (smaller dim2/dim3)");
                continue;
            }

            // Dim1 (Thickness/Diameter) MUST match
            if (!dim1Match) {
                System.out.println("      ❌ Stock ID: " + stock.getId() + " | Store: " + storeType +
                        " | Dim: " + stockDimension + " | SKIP (Dim1 mismatch: " + stockDim1 + " vs " + finalReqDim1 + ")");
                continue;
            }

            // CASE 1 & 2: EXACT MATCH
            if (allDimensionsMatch) {
                stockInfo.put("matchType", "EXACT");
                exactMatchStocks.add(stockInfo);
                System.out.println("      ✅ Stock ID: " + stock.getId() + " | Store: " + storeType +
                        " | Dim: " + stockDimension + " | EXACT MATCH");
                continue;
            }

            // CASE 3: HIGHER DIMENSIONS
            if (hasHigherDimensions) {
                stockInfo.put("matchType", "HIGHER");
                higherDimStocks.add(stockInfo);
                System.out.println("      ✅ Stock ID: " + stock.getId() + " | Store: " + storeType +
                        " | Dim: " + stockDimension + " | HIGHER DIMENSIONS");
                continue;
            }

            System.out.println("      ⚠️ Stock ID: " + stock.getId() + " | Store: " + storeType +
                    " | Dim: " + stockDimension + " | NO CATEGORY");
        }

        System.out.println("\n   📦 Categorization Summary:");
        System.out.println("      - Exact Match: " + exactMatchStocks.size());
        System.out.println("      - Higher Dimensions: " + higherDimStocks.size());
        System.out.println("      - Do Not Recommend: " + doNotRecommendStocks.size());

        // ═══════════════════════════════════════════════════════════════════════════════════
        // STEP 3: SORT BY STORAGE PRIORITY AND FIFO (GRN Timestamp)
        // ═══════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n   🔍 STEP 3: Sorting by storage priority and fetching bundles");

        // Sort by store priority
        Comparator<java.util.Map<String, Object>> storePriorityComparator = (a, b) -> {
            String storeA = (String) a.get("storeType");
            String storeB = (String) b.get("storeType");
            int indexA = storePriority.indexOf(storeA);
            int indexB = storePriority.indexOf(storeB);
            if (indexA == -1) indexA = 999;
            if (indexB == -1) indexB = 999;
            return Integer.compare(indexA, indexB);
        };

        // Sort exact match stocks by priority, then by dimension (ascending W×L product for smallest usable piece first - FRD Rule)
        Comparator<java.util.Map<String, Object>> dimensionComparator = (a, b) -> {
            BigDecimal dim2A = (BigDecimal) a.get("dim2");
            BigDecimal dim3A = (BigDecimal) a.get("dim3");
            BigDecimal dim2B = (BigDecimal) b.get("dim2");
            BigDecimal dim3B = (BigDecimal) b.get("dim3");
            // Compute W×L product for each: if dim3 is zero treat as 1 (for 2-part dimensions like COIL, ROUND ROD)
            BigDecimal productA = dim3A.compareTo(BigDecimal.ZERO) > 0 ? dim2A.multiply(dim3A) : dim2A;
            BigDecimal productB = dim3B.compareTo(BigDecimal.ZERO) > 0 ? dim2B.multiply(dim3B) : dim2B;
            return productA.compareTo(productB);
        };

        exactMatchStocks.sort(storePriorityComparator.thenComparing(dimensionComparator));
        higherDimStocks.sort(storePriorityComparator.thenComparing(dimensionComparator));

        // Fetch bundles for all stocks
        java.util.function.BiConsumer<java.util.Map<String, Object>, List<java.util.Map<String, Object>>> fetchBundles = (stockInfo, bundleList) -> {
            Long stockId = (Long) stockInfo.get("stockId");
            String grnNumbersStr = (String) stockInfo.get("grnNumbers");

            if (grnNumbersStr == null || grnNumbersStr.isBlank()) return;

            // Parse GRN numbers
            List<String> grnList = new ArrayList<>();
            try {
                String grnJson = grnNumbersStr.trim();
                if (grnJson.startsWith("[") && grnJson.endsWith("]")) {
                    grnJson = grnJson.substring(1, grnJson.length() - 1);
                    if (!grnJson.isBlank()) {
                        String[] grnArray = grnJson.split(",");
                        for (String grn : grnArray) {
                            String cleanGrn = grn.trim().replace("\"", "");
                            if (!cleanGrn.isBlank()) {
                                grnList.add(cleanGrn);
                            }
                        }
                    }
                }
            } catch (Exception e) { /* ignore */ }

            for (String grnNumber : grnList) {
                try {
                    List<GrnLineItemEntity> lineItems = grnLineItemRepository.findByGrnNumber(grnNumber);
                    java.util.Map<String, Object> grnResponse = grnService.getGrnBundleDetailsByGrnNumber(grnNumber);
                    Long grnTimestamp = null;

                    if (Boolean.TRUE.equals(grnResponse.get("success"))) {
                        @SuppressWarnings("unchecked")
                        java.util.Map<String, Object> grnMetadata = (java.util.Map<String, Object>) grnResponse.get("data");
                        grnTimestamp = grnMetadata.get("timestamp") != null ? ((Number) grnMetadata.get("timestamp")).longValue() : null;
                    }

                    for (GrnLineItemEntity lineItem : lineItems) {
                        if (lineItem.getItemDescription() != null &&
                            lineItem.getItemDescription().trim().equalsIgnoreCase(dto.getItemDescription().trim())) {

                            java.util.Map<String, Object> bundleMap = new java.util.LinkedHashMap<>();
                            bundleMap.put("bundleId", lineItem.getId());
                            bundleMap.put("grnNumber", grnNumber);
                            bundleMap.put("grnTimestamp", grnTimestamp);
                            bundleMap.put("slNo", lineItem.getSlNo());
                            bundleMap.put("itemDescription", lineItem.getItemDescription());
                            bundleMap.put("productCategory", lineItem.getProductCategory());
                            bundleMap.put("brand", lineItem.getBrand());
                            bundleMap.put("grade", lineItem.getGrade());
                            bundleMap.put("temper", lineItem.getTemper());
                            bundleMap.put("quantityKg", lineItem.getWeightmentQuantityKg() != null ? lineItem.getWeightmentQuantityKg() : BigDecimal.ZERO);
                            bundleMap.put("quantityNo", lineItem.getWeightmentQuantityNo() != null ? lineItem.getWeightmentQuantityNo() : 0);
                            bundleMap.put("store", lineItem.getCurrentStore() != null ? lineItem.getCurrentStore() : stockInfo.get("store"));
                            bundleMap.put("storeType", stockInfo.get("storeType"));
                            bundleMap.put("storageArea", lineItem.getStorageArea() != null ? lineItem.getStorageArea() : stockInfo.get("storageArea"));
                            bundleMap.put("rack", lineItem.getRackColumnBinNumber() != null ? lineItem.getRackColumnBinNumber() : stockInfo.get("rack"));
                            bundleMap.put("qrCode", lineItem.getQrCode());
                            bundleMap.put("qrCodeImageUrl", lineItem.getQrCodeImageUrl());
                            bundleMap.put("dimension", stockInfo.get("dimension"));
                            bundleMap.put("matchType", stockInfo.get("matchType"));
                            bundleMap.put("stockId", stockId);

                            bundleList.add(bundleMap);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("      ⚠️ Error fetching GRN " + grnNumber + ": " + e.getMessage());
                }
            }
        };

        // Collect all bundles
        List<java.util.Map<String, Object>> exactMatchBundles = new ArrayList<>();
        List<java.util.Map<String, Object>> higherDimBundles = new ArrayList<>();

        for (java.util.Map<String, Object> stock : exactMatchStocks) {
            fetchBundles.accept(stock, exactMatchBundles);
        }
        for (java.util.Map<String, Object> stock : higherDimStocks) {
            fetchBundles.accept(stock, higherDimBundles);
        }

        // Sort bundles by GRN Timestamp (FIFO - earliest first)
        Comparator<java.util.Map<String, Object>> fifoComparator = (a, b) -> {
            Long tsA = a.get("grnTimestamp") != null ? ((Number) a.get("grnTimestamp")).longValue() : Long.MAX_VALUE;
            Long tsB = b.get("grnTimestamp") != null ? ((Number) b.get("grnTimestamp")).longValue() : Long.MAX_VALUE;
            return tsA.compareTo(tsB);
        };

        // Sort by store priority first, then FIFO
        Comparator<java.util.Map<String, Object>> bundleSorter = (a, b) -> {
            String storeA = (String) a.get("storeType");
            String storeB = (String) b.get("storeType");
            int indexA = storePriority.indexOf(storeA);
            int indexB = storePriority.indexOf(storeB);
            if (indexA == -1) indexA = 999;
            if (indexB == -1) indexB = 999;
            int storeCmp = Integer.compare(indexA, indexB);
            if (storeCmp != 0) return storeCmp;
            return fifoComparator.compare(a, b);
        };

        exactMatchBundles.sort(bundleSorter);
        higherDimBundles.sort(bundleSorter);

        System.out.println("      - Exact Match Bundles: " + exactMatchBundles.size());
        System.out.println("      - Higher Dim Bundles: " + higherDimBundles.size());

        // ═══════════════════════════════════════════════════════════════════════════════════
        // STEP 4: APPLY CUT SELECTION LOGIC
        // ═══════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n   ═══════════════════════════════════════════════════════════════════════");
        System.out.println("   ║ STEP 4: APPLYING CUT SELECTION LOGIC                                ║");
        System.out.println("   ═══════════════════════════════════════════════════════════════════════");

        java.util.function.Function<java.util.Map<String, Object>, BigDecimal> getBundleQtyKg = bundle -> {
            Object qty = bundle.get("quantityKg");
            return qty instanceof BigDecimal ? (BigDecimal) qty :
                   (qty instanceof Number ? BigDecimal.valueOf(((Number) qty).doubleValue()) : BigDecimal.ZERO);
        };

        List<java.util.Map<String, Object>> selectedBundles = new ArrayList<>();
        String storageType = "";
        String selectionReason = "";
        BigDecimal totalSelectedQtyKg = BigDecimal.ZERO;
        List<String> usedStores = new ArrayList<>();
        boolean selectionDone = false;

        // Calculate totals
        BigDecimal totalExactMatchQty = exactMatchBundles.stream().map(getBundleQtyKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalHigherDimQty = higherDimBundles.stream().map(getBundleQtyKg).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal grandTotalQty = totalExactMatchQty.add(totalHigherDimQty);

        System.out.println("   📊 Required Qty: " + requiredQtyKg + " KG");
        System.out.println("   📊 Exact Match Available: " + totalExactMatchQty + " KG");
        System.out.println("   📊 Higher Dim Available: " + totalHigherDimQty + " KG");
        System.out.println("   📊 Grand Total: " + grandTotalQty + " KG");

        // ═══════════════════════════════════════════════════════════════════════════════════
        // CASE 1: EXACT DIMENSION MATCH & SUFFICIENT QUANTITY
        // ═══════════════════════════════════════════════════════════════════════════════════
        if (!selectionDone && totalExactMatchQty.compareTo(requiredQtyKg) >= 0) {
            System.out.println("\n   🔍 CASE 1: Exact Dimension Match & Sufficient Quantity");

            for (java.util.Map<String, Object> bundle : exactMatchBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                selectedBundles.add(bundle);
                totalSelectedQtyKg = totalSelectedQtyKg.add(bundleQty);

                String bundleStore = (String) bundle.get("storeType");
                if (bundleStore != null && !usedStores.contains(bundleStore)) {
                    usedStores.add(bundleStore);
                }

                if (totalSelectedQtyKg.compareTo(requiredQtyKg) >= 0) {
                    break;
                }
            }

            storageType = String.join(" + ", usedStores);
            selectionReason = "CASE 1: Exact dimension match (" + dto.getDimension() + ") with sufficient quantity → Selected " +
                    selectedBundles.size() + " bundle(s) = " + totalSelectedQtyKg + " KG from " + storageType + " (FIFO)";
            selectionDone = true;
            System.out.println("      ✅ " + selectionReason);
        }

        // ═══════════════════════════════════════════════════════════════════════════════════
        // CASE 2: EXACT DIMENSION MATCH BUT QUANTITY INSUFFICIENT
        // ═══════════════════════════════════════════════════════════════════════════════════
        if (!selectionDone && !exactMatchBundles.isEmpty() && totalExactMatchQty.compareTo(requiredQtyKg) < 0) {
            System.out.println("\n   🔍 CASE 2: Exact Dimension Match but Quantity Insufficient");
            System.out.println("      Exact Match: " + totalExactMatchQty + " KG < Required: " + requiredQtyKg + " KG");

            // Add all exact match bundles first
            for (java.util.Map<String, Object> bundle : exactMatchBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                selectedBundles.add(bundle);
                totalSelectedQtyKg = totalSelectedQtyKg.add(bundleQty);

                String bundleStore = (String) bundle.get("storeType");
                if (bundleStore != null && !usedStores.contains(bundleStore)) {
                    usedStores.add(bundleStore);
                }
            }

            // Then add higher dimension bundles for remaining
            if (totalSelectedQtyKg.compareTo(requiredQtyKg) < 0 && !higherDimBundles.isEmpty()) {
                System.out.println("      Adding higher dimension bundles for remaining...");

                for (java.util.Map<String, Object> bundle : higherDimBundles) {
                    BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                    selectedBundles.add(bundle);
                    totalSelectedQtyKg = totalSelectedQtyKg.add(bundleQty);

                    String bundleStore = (String) bundle.get("storeType");
                    if (bundleStore != null && !usedStores.contains(bundleStore)) {
                        usedStores.add(bundleStore);
                    }

                    if (totalSelectedQtyKg.compareTo(requiredQtyKg) >= 0) {
                        break;
                    }
                }
            }

            storageType = String.join(" + ", usedStores);
            selectionReason = "CASE 2: Exact match insufficient (" + totalExactMatchQty + " KG) + Higher dim bundles → Total " +
                    totalSelectedQtyKg + " KG from " + storageType + " (FIFO)";
            selectionDone = true;
            System.out.println("      ✅ " + selectionReason);
        }

        // ═══════════════════════════════════════════════════════════════════════════════════
        // CASE 3: DIMENSION NOT MATCHING BUT HIGHER SIZE AVAILABLE
        // ═══════════════════════════════════════════════════════════════════════════════════
        if (!selectionDone && exactMatchBundles.isEmpty() && totalHigherDimQty.compareTo(requiredQtyKg) >= 0) {
            System.out.println("\n   🔍 CASE 3: No Exact Match but Higher Size Available");

            for (java.util.Map<String, Object> bundle : higherDimBundles) {
                BigDecimal bundleQty = getBundleQtyKg.apply(bundle);
                selectedBundles.add(bundle);
                totalSelectedQtyKg = totalSelectedQtyKg.add(bundleQty);

                String bundleStore = (String) bundle.get("storeType");
                if (bundleStore != null && !usedStores.contains(bundleStore)) {
                    usedStores.add(bundleStore);
                }

                if (totalSelectedQtyKg.compareTo(requiredQtyKg) >= 0) {
                    break;
                }
            }

            storageType = String.join(" + ", usedStores);
            selectionReason = "CASE 3: No exact match → Selected higher dimension stock = " +
                    totalSelectedQtyKg + " KG from " + storageType + " (Ascending dimension, FIFO)";
            selectionDone = true;
            System.out.println("      ✅ " + selectionReason);
        }

        // ═══════════════════════════════════════════════════════════════════════════════════
        // PARTIAL SELECTION - Not enough stock
        // ═══════════════════════════════════════════════════════════════════════════════════
        if (!selectionDone && grandTotalQty.compareTo(BigDecimal.ZERO) > 0) {
            System.out.println("\n   ⚠️ PARTIAL: Not enough suitable stock available");

            // Add all available bundles
            for (java.util.Map<String, Object> bundle : exactMatchBundles) {
                selectedBundles.add(bundle);
                totalSelectedQtyKg = totalSelectedQtyKg.add(getBundleQtyKg.apply(bundle));
                String bundleStore = (String) bundle.get("storeType");
                if (bundleStore != null && !usedStores.contains(bundleStore)) {
                    usedStores.add(bundleStore);
                }
            }
            for (java.util.Map<String, Object> bundle : higherDimBundles) {
                selectedBundles.add(bundle);
                totalSelectedQtyKg = totalSelectedQtyKg.add(getBundleQtyKg.apply(bundle));
                String bundleStore = (String) bundle.get("storeType");
                if (bundleStore != null && !usedStores.contains(bundleStore)) {
                    usedStores.add(bundleStore);
                }
            }

            storageType = usedStores.isEmpty() ? "PARTIAL" : String.join(" + ", usedStores);
            selectionReason = "PARTIAL SELECTION: Only " + totalSelectedQtyKg + " KG available (Required: " + requiredQtyKg + " KG)";
            selectionDone = true;
        }

        // ═══════════════════════════════════════════════════════════════════════════════════
        // CASE 4: NO SUITABLE STOCK (DO NOT RECOMMEND)
        // ═══════════════════════════════════════════════════════════════════════════════════
        if (!selectionDone || selectedBundles.isEmpty()) {
            System.out.println("\n   ❌ NO SUITABLE STOCK AVAILABLE");
            storageType = "NO_STOCK_AVAILABLE";

            if (!doNotRecommendStocks.isEmpty()) {
                selectionReason = "CASE 4: DO NOT RECOMMEND - All available stock has smaller dimensions than required. " +
                        doNotRecommendStocks.size() + " stock(s) rejected.";
            } else {
                selectionReason = "No suitable stock found matching dimension criteria.";
            }
        }

        // Final sort by FIFO
        selectedBundles.sort(fifoComparator);

        System.out.println("\n   ═══════════════════════════════════════════════════════════════════════");
        System.out.println("   ║ SELECTION COMPLETE                                                  ║");
        System.out.println("   ═══════════════════════════════════════════════════════════════════════");
        System.out.println("   📦 Storage Type: " + storageType);
        System.out.println("   📦 Selected Bundles: " + selectedBundles.size());
        System.out.println("   📦 Total Selected Qty: " + totalSelectedQtyKg + " KG");
        System.out.println("   📋 Reason: " + selectionReason);

        // Build detailed bundle breakdown
        List<java.util.Map<String, Object>> bundleBreakdown = new ArrayList<>();
        int totalSelectedQtyNo = 0;

        for (int i = 0; i < selectedBundles.size(); i++) {
            java.util.Map<String, Object> bundle = selectedBundles.get(i);
            BigDecimal qty = getBundleQtyKg.apply(bundle);

            java.util.Map<String, Object> bundleInfo = new java.util.LinkedHashMap<>();
            bundleInfo.put("slNo", i + 1);
            bundleInfo.put("bundleId", bundle.get("bundleId"));
            bundleInfo.put("quantityKg", qty);
            bundleInfo.put("quantityNo", bundle.get("quantityNo"));
            bundleInfo.put("grnRefNo", bundle.get("grnNumber"));
            bundleInfo.put("grnTimestamp", bundle.get("grnTimestamp"));
            bundleInfo.put("store", bundle.get("store"));
            bundleInfo.put("storeType", bundle.get("storeType"));
            bundleInfo.put("storageArea", bundle.get("storageArea"));
            bundleInfo.put("rack", bundle.get("rack"));
            bundleInfo.put("itemDescription", bundle.get("itemDescription"));
            bundleInfo.put("dimension", bundle.get("dimension"));
            bundleInfo.put("matchType", bundle.get("matchType"));
            bundleInfo.put("brand", bundle.get("brand"));
            bundleInfo.put("grade", bundle.get("grade"));
            bundleInfo.put("temper", bundle.get("temper"));
            bundleInfo.put("qrCode", bundle.get("qrCode"));

            bundleBreakdown.add(bundleInfo);

            Object qtyNoObj = bundle.get("quantityNo");
            if (qtyNoObj instanceof Number) {
                totalSelectedQtyNo += ((Number) qtyNoObj).intValue();
            }

            System.out.println("   [" + (i + 1) + "] Bundle ID: " + bundle.get("bundleId") +
                             " | Qty: " + qty + " KG | GRN: " + bundle.get("grnNumber") +
                             " | Dim: " + bundle.get("dimension") +
                             " | Type: " + bundle.get("matchType") +
                             " | Store: " + bundle.get("storeType"));
        }

        // Build selection result
        java.util.Map<String, Object> selectionResult = new java.util.LinkedHashMap<>();
        selectionResult.put("storageType", storageType);
        selectionResult.put("selectionReason", selectionReason);
        selectionResult.put("selectedBundles", selectedBundles);
        selectionResult.put("bundleBreakdown", bundleBreakdown);
        selectionResult.put("totalSelectedQuantityKg", totalSelectedQtyKg);
        selectionResult.put("totalSelectedQuantityNo", totalSelectedQtyNo);
        selectionResult.put("totalBundlesSelected", selectedBundles.size());
        selectionResult.put("usedStores", usedStores);
        selectionResult.put("requiredDimension", java.util.Map.of(
                "dim1", finalReqDim1,
                "dim2", finalReqDim2,
                "dim3", finalReqDim3,
                "dim1Label", dim1Label,
                "dim2Label", dim2Label,
                "dim3Label", dim3Label,
                "raw", requiredDimensionStr
        ));
        selectionResult.put("availabilitySummary", java.util.Map.of(
                "exactMatchQty", totalExactMatchQty,
                "higherDimQty", totalHigherDimQty,
                "grandTotal", grandTotalQty,
                "exactMatchBundles", exactMatchBundles.size(),
                "higherDimBundles", higherDimBundles.size(),
                "doNotRecommendStocks", doNotRecommendStocks.size()
        ));

        // Shortfall Alert — when total selected < required (FRD FIFOCUT012)
        if (totalSelectedQtyKg.compareTo(requiredQtyKg) < 0) {
            BigDecimal shortfallKg = requiredQtyKg.subtract(totalSelectedQtyKg);
            java.util.Map<String, Object> shortfallAlert = new java.util.LinkedHashMap<>();
            shortfallAlert.put("hasShortfall", true);
            shortfallAlert.put("shortfallQuantityKg", shortfallKg);
            shortfallAlert.put("allocatedQuantityKg", totalSelectedQtyKg);
            shortfallAlert.put("requiredQuantityKg", requiredQtyKg);
            shortfallAlert.put("message", "Insufficient stock. Shortfall: " + shortfallKg + " Kg. Consider raising a Purchase Indent for the remaining quantity.");
            selectionResult.put("shortfallAlert", shortfallAlert);
            System.out.println("   ⚠️ SHORTFALL ALERT: Required=" + requiredQtyKg + " KG | Allocated=" + totalSelectedQtyKg + " KG | Shortfall=" + shortfallKg + " KG");
        }

        // Build response
        java.util.Map<String, Object> responseData = new java.util.LinkedHashMap<>();
        responseData.put("unit", dto.getUnit());
        responseData.put("itemDescription", dto.getItemDescription());
        responseData.put("productCategory", dto.getProductCategory());
        responseData.put("soNumber", dto.getSoNumber());
        responseData.put("lineNumber", dto.getLineNumber());
        responseData.put("orderType", dto.getOrderType());
        responseData.put("requiredQuantityKg", dto.getRequiredQuantityKg());
        responseData.put("requiredQuantityNo", dto.getRequiredQuantityNo());
        responseData.put("dimension", dto.getDimension());
        responseData.put("storePriority", storePriority);
        responseData.put("selectionResult", selectionResult);
        responseData.put("stocksSummary", java.util.Map.of(
                "exactMatchStocks", exactMatchStocks.size(),
                "higherDimStocks", higherDimStocks.size(),
                "doNotRecommendStocks", doNotRecommendStocks.size()
        ));

        System.out.println("\n✅ [PickList-CUT] Selection complete - Storage Type: " + storageType);

        // ═══════════════════════════════════════════════════════════════════════════════════
        // AUTO SAVE PICKLIST
        // ═══════════════════════════════════════════════════════════════════════════════════
        System.out.println("\n💾 [PickList-CUT] Auto-saving pick list...");
        try {
            PickListDTOs pickListDto = new PickListDTOs();
            pickListDto.setUnit(dto.getUnit());
            pickListDto.setSoNumber(dto.getSoNumber());
            pickListDto.setLineNumber(dto.getLineNumber());
            pickListDto.setItemDescription(dto.getItemDescription());
            pickListDto.setOrderType(dto.getOrderType());
            pickListDto.setNextProcess(dto.getNextProcess());
            pickListDto.setStorageType(storageType);
            pickListDto.setSelectionReason(selectionReason);
            pickListDto.setTotalSelectedQuantityKg(totalSelectedQtyKg);
            pickListDto.setTotalBundlesSelected(selectedBundles.size());

            // Convert selected bundles to SelectedBundleDTO list
            List<PickListDTOs.SelectedBundleDTO> bundleDtos = new ArrayList<>();
            for (java.util.Map<String, Object> bundle : selectedBundles) {
                PickListDTOs.SelectedBundleDTO bundleDto = new PickListDTOs.SelectedBundleDTO();
                bundleDto.setBundleId(parseLong(bundle.get("bundleId")));
                bundleDto.setSlNo(parseInteger(bundle.get("slNo")));
                bundleDto.setGrnRefNo(parseString(bundle.get("grnNumber")));
                bundleDto.setGrnTimestamp(parseLong(bundle.get("grnTimestamp")));
                bundleDto.setStore(parseString(bundle.get("store")));
                bundleDto.setStorageArea(parseString(bundle.get("storageArea")));
                bundleDto.setRack(parseString(bundle.get("rack")));
                bundleDto.setItemDescription(parseString(bundle.get("itemDescription")));
                bundleDto.setProductCategory(parseString(bundle.get("productCategory")));
                bundleDto.setBrand(parseString(bundle.get("brand")));
                bundleDto.setGrade(parseString(bundle.get("grade")));
                bundleDto.setTemper(parseString(bundle.get("temper")));
                bundleDto.setQuantityKg(parseBigDecimal(bundle.get("quantityKg")));
                bundleDto.setQuantityNo(parseInteger(bundle.get("quantityNo")));
                bundleDto.setQrCode(parseString(bundle.get("qrCode")));
                bundleDto.setQrCodeImageUrl(parseString(bundle.get("qrCodeImageUrl")));
                bundleDtos.add(bundleDto);
            }
            pickListDto.setSelectedBundles(bundleDtos);

            // Set store, storageArea, rack from first bundle
            if (!selectedBundles.isEmpty()) {
                java.util.Map<String, Object> firstBundle = selectedBundles.getFirst();
                pickListDto.setStore(parseString(firstBundle.get("store")));
                pickListDto.setStorageArea(parseString(firstBundle.get("storageArea")));
                pickListDto.setRackColumnShelfNumber(parseString(firstBundle.get("rack")));
                pickListDto.setBrand(parseString(firstBundle.get("brand")));
                pickListDto.setProductCategory(parseString(firstBundle.get("productCategory")));
                pickListDto.setRetrievalQuantityKg(totalSelectedQtyKg);
                pickListDto.setRetrievalQuantityNo(totalSelectedQtyNo);
            }

            // Save using existing method
            java.util.Map<String, Object> saveResult = savePickListWithBundles(List.of(pickListDto));
            System.out.println("   ✅ Auto-save complete: " + saveResult.get("message"));

            responseData.put("autoSaved", true);
            responseData.put("saveResult", saveResult);

        } catch (Exception e) {
            System.out.println("   ❌ Auto-save failed: " + e.getMessage());
            e.printStackTrace();
            responseData.put("autoSaved", false);
            responseData.put("autoSaveError", e.getMessage());
        }

        return java.util.Map.of(
                "success", true,
                "message", "Pick list generated and saved successfully for CUT order",
                "data", responseData
        );
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> savePickListWithBundles(List<PickListDTOs> dtos) {
        System.out.println("\n📋 [SavePickListBundles] Starting save for " + dtos.size() + " entries");

        List<SalesOrderSchedulerEntity> savedEntities = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (PickListDTOs dto : dtos) {
            try {
                System.out.println("   📦 Processing SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber());

                SalesOrderSchedulerEntity scheduler = repository.findBySoNumberAndLineNumber(
                        dto.getSoNumber(), dto.getLineNumber()
                );

                if (scheduler == null) {
                    String error = "Scheduler not found for SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber();
                    System.out.println("   ❌ " + error);
                    errors.add(error);
                    continue;
                }

                // Convert selected bundles to JSON
                String selectedBundlesJson = null;
                if (dto.getSelectedBundles() != null && !dto.getSelectedBundles().isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        selectedBundlesJson = mapper.writeValueAsString(dto.getSelectedBundles());
                        System.out.println("      📦 Bundles count: " + dto.getSelectedBundles().size());
                    } catch (Exception e) {
                        System.out.println("      ⚠️ Error converting bundles to JSON: " + e.getMessage());
                    }
                }

                // Get first bundle details for backward compatibility
                String store = dto.getStore();
                String storageArea = dto.getStorageArea();
                String rackColumnShelfNumber = dto.getRackColumnShelfNumber();
                String brand = dto.getBrand();
                String productCategory = dto.getProductCategory();
                BigDecimal retrievalQuantityKg = dto.getRetrievalQuantityKg();
                Integer retrievalQuantityNo = dto.getRetrievalQuantityNo();

                if (dto.getSelectedBundles() != null && !dto.getSelectedBundles().isEmpty()) {
                    PickListDTOs.SelectedBundleDTO firstBundle = dto.getSelectedBundles().get(0);
                    if (store == null) store = firstBundle.getStore();
                    if (storageArea == null) storageArea = firstBundle.getStorageArea();
                    if (rackColumnShelfNumber == null) rackColumnShelfNumber = firstBundle.getRack();
                    if (brand == null) brand = firstBundle.getBrand();
                    if (productCategory == null) productCategory = firstBundle.getProductCategory();

                    if (retrievalQuantityKg == null) {
                        retrievalQuantityKg = dto.getTotalSelectedQuantityKg();
                    }
                    if (retrievalQuantityNo == null) {
                        retrievalQuantityNo = dto.getSelectedBundles().stream()
                                .map(b -> b.getQuantityNo() != null ? b.getQuantityNo() : 0)
                                .reduce(0, Integer::sum);
                    }
                }

                // Build PickListEntityScheduler
                PickListEntityScheduler pickList = PickListEntityScheduler.builder()
                        .unit(dto.getUnit())
                        .soNumber(dto.getSoNumber())
                        .lineNumber(dto.getLineNumber())
                        .nextProcess(dto.getNextProcess())
                        .orderType(dto.getOrderType())
                        .productCategory(productCategory)
                        .itemDescription(dto.getItemDescription())
                        .brand(brand)
                        .retrievalQuantityKg(retrievalQuantityKg)
                        .retrievalQuantityNo(retrievalQuantityNo)
                        .storageArea(storageArea)
                        .store(store)
                        .rackColumnShelfNumber(rackColumnShelfNumber)
                        .storageType(dto.getStorageType())
                        .selectionReason(dto.getSelectionReason())
                        .totalSelectedQuantityKg(dto.getTotalSelectedQuantityKg())
                        .totalBundlesSelected(dto.getTotalBundlesSelected())
                        .selectedBundlesJson(selectedBundlesJson)
                        .build();

                scheduler.setPickList(pickList);

                // Set retrieval status to IN_PROGRESS when picklist is saved
                scheduler.setRetrievalStatus("IN PROGRESS");

                SalesOrderSchedulerEntity saved = repository.save(scheduler);
                savedEntities.add(saved);

                System.out.println("      ✅ Saved - StorageType: " + dto.getStorageType() +
                                   " | Bundles: " + dto.getTotalBundlesSelected() +
                                   " | TotalQty: " + dto.getTotalSelectedQuantityKg() +
                                   " | RetrievalStatus: IN PROGRESS");

            } catch (Exception e) {
                String error = "Error saving SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber() + " - " + e.getMessage();
                System.out.println("   ❌ " + error);
                errors.add(error);
            }
        }

        System.out.println("\n✅ [SavePickListBundles] Complete - Saved: " + savedEntities.size() + " | Errors: " + errors.size());

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("success", errors.isEmpty());
        response.put("message", errors.isEmpty() ? "Pick list saved successfully" : "Pick list saved with some errors");
        response.put("savedCount", savedEntities.size());
        response.put("errorCount", errors.size());
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }
        response.put("data", savedEntities);

        return response;
    }

    // ========== HELPER METHODS FOR SAFE PARSING ==========

    /**
     * Compare two strings (case-insensitive, handles nulls and empty strings)
     * Returns true if both are null/empty OR if they match (ignoring case and whitespace)
     */
    private boolean matchStrings(String str1, String str2) {
        // Normalize: null and empty are treated as equivalent
        String s1 = (str1 == null || str1.trim().isEmpty()) ? null : str1.trim().toLowerCase();
        String s2 = (str2 == null || str2.trim().isEmpty()) ? null : str2.trim().toLowerCase();

        // Both null/empty = match
        if (s1 == null && s2 == null) return true;

        // One null, one not = no match
        if (s1 == null || s2 == null) return false;

        // Compare normalized strings
        return s1.equals(s2);
    }

    private Long parseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer parseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof Number) return BigDecimal.valueOf(((Number) value).doubleValue());
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private String parseString(Object value) {
        if (value == null) return null;
        return value.toString();
    }

    @Override
    public java.util.Map<String, Object> getPickListBundleDetails(String soNumber, String lineNumber) {
        System.out.println("\n📋 [GetPickListDetails] Fetching details for SO: " + soNumber + " | Line: " + lineNumber);

        try {
            // Find scheduler by SO and Line
            SalesOrderSchedulerEntity scheduler = repository.findBySoNumberAndLineNumber(soNumber, lineNumber);

            if (scheduler == null) {
                System.out.println("   ❌ Scheduler not found");
                return java.util.Map.of(
                        "success", false,
                        "message", "Scheduler not found for SO: " + soNumber + " | Line: " + lineNumber,
                        "data", new ArrayList<>()
                );
            }

            PickListEntityScheduler pickList = scheduler.getPickList();

            if (pickList == null) {
                System.out.println("   ❌ PickList not found");
                return java.util.Map.of(
                        "success", false,
                        "message", "PickList not generated for SO: " + soNumber + " | Line: " + lineNumber,
                        "data", new ArrayList<>()
                );
            }

            System.out.println("   ✅ Found PickList ID: " + pickList.getId());

            // Parse selectedBundlesJson
            List<java.util.Map<String, Object>> bundleDetails = new ArrayList<>();

            if (pickList.getSelectedBundlesJson() != null && !pickList.getSelectedBundlesJson().isEmpty()) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.core.type.TypeReference<List<java.util.Map<String, Object>>> typeRef =
                            new com.fasterxml.jackson.core.type.TypeReference<>() {};
                    List<java.util.Map<String, Object>> bundles = mapper.readValue(pickList.getSelectedBundlesJson(), typeRef);

                    System.out.println("   📦 Found " + bundles.size() + " bundles");

                    for (java.util.Map<String, Object> bundle : bundles) {
                        java.util.Map<String, Object> detail = new java.util.LinkedHashMap<>();

                        // STORE
                        detail.put("store", parseString(bundle.get("store")));

                        // STORAGE AREA
                        detail.put("storageArea", parseString(bundle.get("storageArea")));

                        // RACK COLUMN & BIN
                        detail.put("rackColumnBin", parseString(bundle.get("rack")));

                        // Retrieval Quantity (Kg)
                        detail.put("retrievalQuantityKg", parseBigDecimal(bundle.get("quantityKg")));

                        // Retrieval Quantity (No)
                        detail.put("retrievalQuantityNo", parseInteger(bundle.get("quantityNo")));

                        // Batch number (GRN Ref No)
                        String grnRefNo = parseString(bundle.get("grnRefNo"));
                        detail.put("batchNumber", grnRefNo);

                        // Date of inward - fetch from GRN
                        String dateOfInward = null;
                        if (grnRefNo != null) {
                            var grnOpt = grnService.getGrnBundleDetailsByGrnNumber(grnRefNo);
                            if (grnOpt != null && Boolean.TRUE.equals(grnOpt.get("success"))) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> grnData = (java.util.Map<String, Object>) grnOpt.get("data");
                                if (grnData != null && grnData.get("timestamp") != null) {
                                    Object timestamp = grnData.get("timestamp");
                                    if (timestamp instanceof Number) {
                                        long ts = ((Number) timestamp).longValue();
                                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                                        dateOfInward = sdf.format(new java.util.Date(ts));
                                    } else if (timestamp instanceof String) {
                                        dateOfInward = (String) timestamp;
                                    }
                                }
                            }
                        }
                        detail.put("dateOfInward", dateOfInward);

                        // Additional fields
                        detail.put("bundleId", parseLong(bundle.get("bundleId")));
                        detail.put("itemDescription", parseString(bundle.get("itemDescription")));
                        detail.put("dimension", parseString(bundle.get("dimension")));
                        detail.put("brand", parseString(bundle.get("brand")));
                        detail.put("grade", parseString(bundle.get("grade")));
                        detail.put("temper", parseString(bundle.get("temper")));
                        detail.put("qrCode", parseString(bundle.get("qrCode")));
                        detail.put("qrCodeImageUrl", parseString(bundle.get("qrCodeImageUrl")));

                        bundleDetails.add(detail);

                        System.out.println("      - Store: " + detail.get("store") +
                                           " | Area: " + detail.get("storageArea") +
                                           " | Rack: " + detail.get("rackColumnBin") +
                                           " | Dim: " + detail.get("dimension") +
                                           " | Qty: " + detail.get("retrievalQuantityKg") + " KG" +
                                           " | Batch: " + detail.get("batchNumber"));
                    }
                } catch (Exception e) {
                    System.out.println("   ❌ Error parsing bundles JSON: " + e.getMessage());
                }
            }

            // Build summary
            BigDecimal totalQtyKg = bundleDetails.stream()
                    .map(b -> b.get("retrievalQuantityKg") != null ? (BigDecimal) b.get("retrievalQuantityKg") : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            int totalQtyNo = bundleDetails.stream()
                    .map(b -> b.get("retrievalQuantityNo") != null ? (Integer) b.get("retrievalQuantityNo") : 0)
                    .reduce(0, Integer::sum);

            java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "PickList bundle details fetched successfully");
            response.put("soNumber", soNumber);
            response.put("lineNumber", lineNumber);
            response.put("itemDescription", pickList.getItemDescription());
            response.put("dimension", scheduler.getDimension());
            response.put("productCategory", scheduler.getProductCategory());
            response.put("brand", scheduler.getBrand());
            response.put("grade", scheduler.getGrade());
            response.put("temper", scheduler.getTemper());
            response.put("storageType", pickList.getStorageType());
            response.put("selectionReason", pickList.getSelectionReason());
            response.put("totalBundles", bundleDetails.size());
            response.put("totalQuantityKg", totalQtyKg);
            response.put("totalQuantityNo", totalQtyNo);
            response.put("retrievalStatus", scheduler.getRetrievalStatus());
            response.put("bundles", bundleDetails);

            System.out.println("   ✅ Returning " + bundleDetails.size() + " bundle details");

            return response;

        } catch (Exception e) {
            System.out.println("   ❌ Error: " + e.getMessage());
            return java.util.Map.of(
                    "success", false,
                    "message", "Error fetching picklist details: " + e.getMessage(),
                    "data", new ArrayList<>()
            );
        }
    }

    @Override
    @Transactional
    public java.util.Map<String, Object> saveStockTransferWithEntries(List<WarehouseStockTransferSchedulerDTO> dtos) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║            📦 STOCK TRANSFER SAVE - SALES ORDER SCHEDULER                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");
        System.out.println("┌──────────────────────────────────────────────────────────────────────────────┐");
        System.out.println("│ BATCH PROCESSING INFO                                                       │");
        System.out.println("├──────────────────────────────────────────────────────────────────────────────┤");
        System.out.println("│ Total DTOs to process: " + dtos.size());
        System.out.println("└──────────────────────────────────────────────────────────────────────────────┘");

        List<SalesOrderSchedulerEntity> savedEntities = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int dtoIndex = 0; dtoIndex < dtos.size(); dtoIndex++) {
            WarehouseStockTransferSchedulerDTO dto = dtos.get(dtoIndex);
            try {
                System.out.println("\n╔══════════════════════════════════════════════════════════════════════════════╗");
                System.out.println("║ 📦 DTO [" + (dtoIndex + 1) + "/" + dtos.size() + "] - STOCK TRANSFER PROCESSING                              ║");
                System.out.println("╠══════════════════════════════════════════════════════════════════════════════╣");
                System.out.println("║ SO Number      : " + dto.getSoNumber());
                System.out.println("║ Line Number    : " + dto.getLineNumber());
                System.out.println("║ Next Process   : " + dto.getNextProcess());
                System.out.println("║ Item Description: " + dto.getItemDescription());
                System.out.println("║ Unit           : " + dto.getUnit());
                System.out.println("║ Brand          : " + dto.getBrand());
                System.out.println("║ Grade          : " + dto.getGrade());
                System.out.println("║ Temper         : " + dto.getTemper());
                System.out.println("║ Required Qty   : " + dto.getRequiredQuantityKg() + " KG | " + dto.getRequiredQuantityNo() + " NO");
                System.out.println("║ Weighment Qty  : " + dto.getWeighmentQuantityKg() + " KG | " + dto.getWeighmentQuantityNo() + " NO");
                System.out.println("║ Returnable Qty : " + dto.getReturnableQuantityKg() + " KG | " + dto.getReturnableQuantityNo() + " NO");
                System.out.println("║ Generate QR    : " + dto.getGenerateQr());
                System.out.println("║ QR Code        : " + (dto.getQrCode() != null ? dto.getQrCode().substring(0, Math.min(50, dto.getQrCode().length())) + "..." : "null"));
                System.out.println("║ Retrieval Entries: " + (dto.getRetrievalEntries() != null ? dto.getRetrievalEntries().size() : 0));
                System.out.println("╚══════════════════════════════════════════════════════════════════════════════╝");

                // Find scheduler by SO + Line number
                System.out.println("\n   🔍 STEP 1: Finding Scheduler by SO + Line...");
                SalesOrderSchedulerEntity scheduler = repository.findBySoNumberAndLineNumber(
                        dto.getSoNumber(), dto.getLineNumber()
                );

                if (scheduler == null) {
                    String error = "Scheduler not found for SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber();
                    System.out.println("   ❌ " + error);
                    errors.add(error);
                    continue;
                }

                System.out.println("   ✅ Found Scheduler:");
                System.out.println("      - ID: " + scheduler.getId());
                System.out.println("      - Order Type: " + scheduler.getOrderType());
                System.out.println("      - Product Category: " + scheduler.getProductCategory());

                // Convert retrieval entries to JSON (like picklist selectedBundlesJson)
                String retrievalEntriesJson = null;
                if (dto.getRetrievalEntries() != null && !dto.getRetrievalEntries().isEmpty()) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        retrievalEntriesJson = mapper.writeValueAsString(dto.getRetrievalEntries());
                        System.out.println("      📦 Retrieval entries count: " + dto.getRetrievalEntries().size());
                    } catch (Exception e) {
                        System.out.println("      ⚠️ Error converting retrieval entries to JSON: " + e.getMessage());
                    }
                }

                // Build retrieval entry entities from DTO (for OneToMany relationship)
                List<WarehouseStockRetrievalEntityScheduler> retrievalEntityList = new ArrayList<>();
                if (dto.getRetrievalEntries() != null) {
                    for (WarehouseStockRetrievalEntrySchedulerDTO entryDto : dto.getRetrievalEntries()) {
                        WarehouseStockRetrievalEntityScheduler entry = WarehouseStockRetrievalEntityScheduler.builder()
                                .sourceBundleId(entryDto.getBundleId())
                                .sourceStockSummaryId(entryDto.getStockSummaryId())
                                .store(entryDto.getStore())
                                .storageArea(entryDto.getStorageArea())
                                .rackColumnBin(entryDto.getRackColumnBin())
                                .retrievalQuantityKg(entryDto.getRetrievalQuantityKg())
                                .retrievalQuantityNo(entryDto.getRetrievalQuantityNo())
                                .batchNumber(entryDto.getBatchNumber())
                                .dateOfInward(entryDto.getDateOfInward())
                                .dimension(entryDto.getDimension())
                                .qrCodeRM(entryDto.getQrCodeRM())
                                .scanQrCode(entryDto.getScanQrCode())
                                .retrievedQuantityKg(entryDto.getRetrievedQuantityKg())
                                .retrievedQuantityNo(entryDto.getRetrievedQuantityNo())
                                .weighedQuantityKg(entryDto.getWeighedQuantityKg())
                                .weighedQuantityNo(entryDto.getWeighedQuantityNo())
                                .generatePrintQrFG(entryDto.getGeneratePrintQrFG())
                                .returnableQuantityKg(entryDto.getReturnableQuantityKg())
                                .returnableQuantityNo(entryDto.getReturnableQuantityNo())
                                .build();
                        retrievalEntityList.add(entry);

                        System.out.println("         📋 Entry: BundleId=" + entryDto.getBundleId() +
                                           " | StockSummaryId=" + entryDto.getStockSummaryId() +
                                           " | Store=" + entryDto.getStore() +
                                           " | Rack=" + entryDto.getRackColumnBin() +
                                           " | Dimension=" + entryDto.getDimension() +
                                           " | RetrievalQty=" + entryDto.getRetrievalQuantityKg() + " KG");
                    }
                }

                // Build WarehouseStockTransferEntityScheduler (like PickListEntityScheduler)
                WarehouseStockTransferEntityScheduler stockTransfer = WarehouseStockTransferEntityScheduler.builder()
                        .soNumber(dto.getSoNumber())
                        .lineNumber(dto.getLineNumber())
                        .nextProcess(dto.getNextProcess())
                        .requiredQuantityKg(dto.getRequiredQuantityKg())
                        .requiredQuantityNo(dto.getRequiredQuantityNo())
                        .weighmentQuantityKg(dto.getWeighmentQuantityKg())
                        .weighmentQuantityNo(dto.getWeighmentQuantityNo())
                        .returnableQuantityKg(dto.getReturnableQuantityKg())
                        .returnableQuantityNo(dto.getReturnableQuantityNo())
                        .itemDescription(dto.getItemDescription())
                        .brand(dto.getBrand())
                        .grade(dto.getGrade())
                        .temper(dto.getTemper())
                        .unit(dto.getUnit())
                        .generateQr(dto.getGenerateQr())
                        .qrCode(dto.getQrCode())
                        .retrievalEntriesJson(retrievalEntriesJson)
                        .retrievalEntries(retrievalEntityList)
                        .build();

                // Set parent reference for each retrieval entry
                for (WarehouseStockRetrievalEntityScheduler entry : retrievalEntityList) {
                    entry.setStockTransfer(stockTransfer);
                }

                // Set stockTransfer to scheduler (like pickList)
                scheduler.setStockTransfer(stockTransfer);

                // Set retrieval status to COMPLETED
                scheduler.setRetrievalStatus("COMPLETED");
                System.out.println("      ✅ Retrieval Status set to: COMPLETED");

                // Update next process if provided
                if (dto.getNextProcess() != null && !dto.getNextProcess().isBlank()) {
                    scheduler.setNextProcess(dto.getNextProcess());
                }

                // ========== ORDER STOCK PROCESSING & STORAGE LOGIC ==========
                System.out.println("\n      🔄 [StockProcessing] Starting Order Stock Processing...");

                // Process each retrieval entry
                for (int entryIndex = 0; entryIndex < retrievalEntityList.size(); entryIndex++) {
                    WarehouseStockRetrievalEntityScheduler entry = retrievalEntityList.get(entryIndex);
                    String grnRefNo = entry.getBatchNumber();

                    // Use weighed quantity (actual quantity after weighing) instead of retrieval quantity
                    // Weighed qty is what actually gets transferred, retrieval qty is what was picked
                    BigDecimal weighedQtyKg = entry.getWeighedQuantityKg();
                    Integer weighedQtyNo = entry.getWeighedQuantityNo();
                    BigDecimal retrievalQtyKg = entry.getRetrievalQuantityKg(); // For logging only
                    Integer retrievalQtyNo = entry.getRetrievalQuantityNo();    // For logging only
                    String entryDimension = entry.getDimension(); // Get dimension from entry

                    System.out.println("\n         ┌─────────────────────────────────────────────────────────────");
                    System.out.println("         │ 📦 ENTRY [" + (entryIndex + 1) + "/" + retrievalEntityList.size() + "]");
                    System.out.println("         ├─────────────────────────────────────────────────────────────");
                    System.out.println("         │ GRN Ref No (batchNumber): " + grnRefNo);
                    System.out.println("         │ Store: " + entry.getStore());
                    System.out.println("         │ Storage Area: " + entry.getStorageArea());
                    System.out.println("         │ Rack/Bin: " + entry.getRackColumnBin());
                    System.out.println("         │ Dimension: " + entryDimension);
                    System.out.println("         │ Retrieval Qty (picked): " + retrievalQtyKg + " KG | " + retrievalQtyNo + " NO");
                    System.out.println("         │ Weighed Qty (actual): " + weighedQtyKg + " KG | " + weighedQtyNo + " NO");
                    System.out.println("         │ Date of Inward: " + entry.getDateOfInward());
                    System.out.println("         └─────────────────────────────────────────────────────────────");

                    if (grnRefNo == null || grnRefNo.isEmpty()) {
                        System.out.println("         ⚠️ SKIPPING - No GRN Ref No provided");
                        continue;
                    }

                    // Get source location details from entry
                    String sourceStore = entry.getStore();
                    String sourceStorageArea = entry.getStorageArea();
                    String sourceRack = entry.getRackColumnBin();

                    StockSummaryEntity sourceStockForCopy = null;
                    StockSummaryEntity exactSourceStock = null;
                    StockSummaryBundleEntity sourceBundleCopy = null;

                    // ═══════════════════════════════════════════════════════════════════
                    // STEP 1: REMOVE GRN FROM SOURCE STOCK SUMMARY (EXACT MATCH ONLY)
                    // ═══════════════════════════════════════════════════════════════════
                    try {
                        System.out.println("\n         📍 STEP 1: Removing GRN from Source Stock Summary...");
                        System.out.println("         │ Searching for GRN: " + grnRefNo);
                        System.out.println("         │ Source Location (from entry):");
                        System.out.println("         │    - Store       : '" + sourceStore + "'");
                        System.out.println("         │    - StorageArea : '" + sourceStorageArea + "'");
                        System.out.println("         │    - Rack        : '" + sourceRack + "'");

                        List<StockSummaryEntity> sourceStocks = stockSummaryRepository.findByGrnNumberContaining(grnRefNo);
                        System.out.println("         │ Found " + sourceStocks.size() + " stock entries containing this GRN");

                        // First, find the exact source stock that matches the entry location
                        for (StockSummaryEntity sourceStock : sourceStocks) {
                            System.out.println("         │");
                            System.out.println("         │ 🔍 Stock Entry Found:");
                            System.out.println("         │    - Stock ID: " + sourceStock.getId());
                            System.out.println("         │    - Unit: " + sourceStock.getUnit());
                            System.out.println("         │    - Store: " + sourceStock.getStore());
                            System.out.println("         │    - Storage Area: " + sourceStock.getStorageArea());
                            System.out.println("         │    - Rack: " + sourceStock.getRackColumnShelfNumber());
                            System.out.println("         │    - Item: " + sourceStock.getItemDescription());
                            System.out.println("         │    - GRNs: " + sourceStock.getGrnNumbers());

                            // Check if this stock matches the source location from entry
                            boolean storeMatch = (sourceStore == null && sourceStock.getStore() == null) ||
                                    (sourceStore != null && sourceStore.equalsIgnoreCase(sourceStock.getStore()));
                            boolean areaMatch = (sourceStorageArea == null && sourceStock.getStorageArea() == null) ||
                                    (sourceStorageArea != null && sourceStorageArea.equalsIgnoreCase(sourceStock.getStorageArea()));
                            boolean rackMatch = (sourceRack == null && sourceStock.getRackColumnShelfNumber() == null) ||
                                    (sourceRack != null && sourceRack.equalsIgnoreCase(sourceStock.getRackColumnShelfNumber()));

                            System.out.println("         │    - Store Match     : " + storeMatch);
                            System.out.println("         │    - StorageArea Match: " + areaMatch);
                            System.out.println("         │    - Rack Match      : " + rackMatch);

                            if (storeMatch && areaMatch && rackMatch) {
                                exactSourceStock = sourceStock;
                                sourceStockForCopy = sourceStock;
                                System.out.println("         │    ✅ EXACT SOURCE MATCH FOUND!");
                            } else {
                                System.out.println("         │    ⏭️ SKIPPING - Not the source stock");
                            }
                        }

                        // Remove GRN only from exact source stock
                        if (exactSourceStock != null) {
                            System.out.println("         │");
                            System.out.println("         │ 🎯 Processing EXACT SOURCE STOCK (ID: " + exactSourceStock.getId() + ")");

                            String grnNumbers = exactSourceStock.getGrnNumbers();
                            System.out.println("         │    - GRNs BEFORE: " + grnNumbers);

                            if (grnNumbers != null && !grnNumbers.isEmpty()) {
                                grnNumbers = grnNumbers.replace("\"" + grnRefNo + "\"", "");
                                grnNumbers = grnNumbers.replace(",,", ",");
                                grnNumbers = grnNumbers.replace("[,", "[");
                                grnNumbers = grnNumbers.replace(",]", "]");
                                if (grnNumbers.equals("[]") || grnNumbers.equals("[\"\"]")) {
                                    grnNumbers = null;
                                }
                                exactSourceStock.setGrnNumbers(grnNumbers);
                                stockSummaryRepository.save(exactSourceStock);

                                System.out.println("         │    - GRNs AFTER: " + grnNumbers);
                                System.out.println("         │    ✅ GRN " + grnRefNo + " REMOVED from Stock ID: " + exactSourceStock.getId());
                            } else {
                                System.out.println("         │    ⚠️ No GRNs to remove");
                            }
                        } else {
                            System.out.println("         │");
                            System.out.println("         │ ⚠️ No exact source stock found matching the entry location");
                            System.out.println("         │    Will use first available stock for copy if needed");
                            if (!sourceStocks.isEmpty()) {
                                sourceStockForCopy = sourceStocks.get(0);
                            }
                        }

                        if (sourceStocks.isEmpty()) {
                            System.out.println("         │ ⚠️ No stock entries found with GRN: " + grnRefNo);
                        }

                        System.out.println("         │ ✅ STEP 1 COMPLETED SUCCESSFULLY");

                    } catch (Exception step1Ex) {
                        System.out.println("         │ ❌ STEP 1 FAILED: " + step1Ex.getMessage());
                        step1Ex.printStackTrace();
                        throw new RuntimeException("STEP 1 Failed - GRN removal from source: " + step1Ex.getMessage(), step1Ex);
                    }

                    // ═══════════════════════════════════════════════════════════════════
                    // STEP 1.5: DELETE BUNDLE FROM SOURCE AND COPY BUNDLE DETAILS
                    // ═══════════════════════════════════════════════════════════════════
                    try {
                        System.out.println("\n         📍 STEP 1.5: Processing Bundle from Source Stock...");

                        Long sourceBundleId = entry.getSourceBundleId();
                        Long sourceStockSummaryId = entry.getSourceStockSummaryId();

                        System.out.println("         │ Bundle ID from entry: " + sourceBundleId);
                        System.out.println("         │ Stock Summary ID from entry: " + sourceStockSummaryId);

                    if (sourceBundleId != null) {
                        // Find bundle by ID
                        Optional<StockSummaryBundleEntity> bundleOpt = stockSummaryBundleRepository.findById(sourceBundleId);
                        if (bundleOpt.isPresent()) {
                            StockSummaryBundleEntity sourceBundle = bundleOpt.get();
                            System.out.println("         │");
                            System.out.println("         │ ✅ FOUND Bundle by ID: " + sourceBundleId);
                            System.out.println("         │    - GRN Number: " + sourceBundle.getGrnNumber());
                            System.out.println("         │    - Item: " + sourceBundle.getItemDescription());
                            System.out.println("         │    - Dimension: " + sourceBundle.getDimension());
                            System.out.println("         │    - Qty: " + sourceBundle.getWeightmentQuantityKg() + " KG | " + sourceBundle.getWeightmentQuantityNo() + " NO");
                            System.out.println("         │    - Heat No: " + sourceBundle.getHeatNo());
                            System.out.println("         │    - Lot No: " + sourceBundle.getLotNo());
                            System.out.println("         │    - Test Certificate: " + sourceBundle.getTestCertificate());

                            // Copy bundle details for later use
                            sourceBundleCopy = StockSummaryBundleEntity.builder()
                                    .grnNumber(sourceBundle.getGrnNumber())
                                    .grnId(sourceBundle.getGrnId())
                                    .slNo(sourceBundle.getSlNo())
                                    .itemDescription(sourceBundle.getItemDescription())
                                    .productCategory(sourceBundle.getProductCategory())
                                    .sectionNumber(sourceBundle.getSectionNumber())
                                    .brand(sourceBundle.getBrand())
                                    .grade(sourceBundle.getGrade())
                                    .temper(sourceBundle.getTemper())
                                    .dimension(sourceBundle.getDimension())
                                    .weighment(sourceBundle.getWeighment())
                                    .uomNetWeight(sourceBundle.getUomNetWeight())
                                    .uomNo(sourceBundle.getUomNo())
                                    .materialAcceptance(sourceBundle.getMaterialAcceptance())
                                    .poNumber(sourceBundle.getPoNumber())
                                    .heatNo(sourceBundle.getHeatNo())
                                    .lotNo(sourceBundle.getLotNo())
                                    .testCertificate(sourceBundle.getTestCertificate())
                                    .userId(sourceBundle.getUserId())
                                    .unitId(sourceBundle.getUnitId())
                                    .createdBy(sourceBundle.getCreatedBy())
                                    .build();

                            // Delete source bundle
                            stockSummaryBundleRepository.delete(sourceBundle);
                            System.out.println("         │    🗑️ Bundle DELETED from Source Stock");
                        } else {
                            System.out.println("         │ ⚠️ Bundle not found with ID: " + sourceBundleId);
                        }
                    } else if (grnRefNo != null) {
                        // Try to find bundle by GRN number
                        System.out.println("         │");
                        System.out.println("         │ 🔍 Searching bundle by GRN Number: " + grnRefNo);

                        List<StockSummaryBundleEntity> bundles = null;

                        // First try with exact source stock if available
                        if (exactSourceStock != null) {
                            bundles = stockSummaryBundleRepository.findByStockSummaryIdAndGrnNumber(
                                    exactSourceStock.getId(), grnRefNo);
                            System.out.println("         │    - Searching by StockSummaryId=" + exactSourceStock.getId() + " and GRN=" + grnRefNo);
                            System.out.println("         │    - Found " + bundles.size() + " bundle(s)");
                        }

                        // If not found, search by GRN only
                        if (bundles == null || bundles.isEmpty()) {
                            bundles = stockSummaryBundleRepository.findByGrnNumber(grnRefNo);
                            System.out.println("         │    - Searching by GRN only: " + grnRefNo);
                            System.out.println("         │    - Found " + bundles.size() + " bundle(s)");
                        }

                        if (!bundles.isEmpty()) {
                            // Find EXACT matching bundle based on entry details (store, area, rack, dimension)
                            System.out.println("         │");
                            System.out.println("         │ 🎯 Finding EXACT bundle match from " + bundles.size() + " bundles...");
                            System.out.println("         │    Match Criteria from Entry:");
                            System.out.println("         │       - Store       : '" + sourceStore + "'");
                            System.out.println("         │       - StorageArea : '" + sourceStorageArea + "'");
                            System.out.println("         │       - Rack        : '" + sourceRack + "'");
                            System.out.println("         │       - Dimension   : '" + entryDimension + "'");

                            StockSummaryBundleEntity matchedBundle = null;

                            for (StockSummaryBundleEntity bundle : bundles) {
                                System.out.println("         │");
                                System.out.println("         │    📦 Checking Bundle ID: " + bundle.getId());
                                System.out.println("         │       - Store       : '" + bundle.getCurrentStore() + "'");
                                System.out.println("         │       - StorageArea : '" + bundle.getStorageArea() + "'");
                                System.out.println("         │       - Rack        : '" + bundle.getRackColumnBinNumber() + "'");
                                System.out.println("         │       - Dimension   : '" + bundle.getDimension() + "'");

                                // Match criteria: Store, StorageArea, Rack, Dimension
                                boolean storeMatch = matchStrings(sourceStore, bundle.getCurrentStore());
                                boolean areaMatch = matchStrings(sourceStorageArea, bundle.getStorageArea());
                                boolean rackMatch = matchStrings(sourceRack, bundle.getRackColumnBinNumber());
                                boolean dimMatch = matchStrings(entryDimension, bundle.getDimension());

                                System.out.println("         │       - Store Match     : " + storeMatch);
                                System.out.println("         │       - StorageArea Match: " + areaMatch);
                                System.out.println("         │       - Rack Match      : " + rackMatch);
                                System.out.println("         │       - Dimension Match : " + dimMatch);

                                if (storeMatch && areaMatch && rackMatch && dimMatch) {
                                    matchedBundle = bundle;
                                    System.out.println("         │       ✅ EXACT MATCH FOUND!");
                                    break;
                                } else {
                                    System.out.println("         │       ⏭️ NOT a match - skipping");
                                }
                            }

                            // If no exact match found, try matching with fewer criteria
                            if (matchedBundle == null) {
                                System.out.println("         │");
                                System.out.println("         │ ⚠️ No exact match found. Trying partial match (Rack + Dimension)...");

                                for (StockSummaryBundleEntity bundle : bundles) {
                                    boolean rackMatch = matchStrings(sourceRack, bundle.getRackColumnBinNumber());
                                    boolean dimMatch = matchStrings(entryDimension, bundle.getDimension());

                                    if (rackMatch && dimMatch) {
                                        matchedBundle = bundle;
                                        System.out.println("         │    ✅ Partial match found - Bundle ID: " + bundle.getId());
                                        break;
                                    }
                                }
                            }

                            // If still no match, use first bundle as fallback
                            if (matchedBundle == null && !bundles.isEmpty()) {
                                matchedBundle = bundles.get(0);
                                System.out.println("         │");
                                System.out.println("         │ ⚠️ No match found - using first bundle as fallback (ID: " + matchedBundle.getId() + ")");
                            }

                            if (matchedBundle != null) {
                                System.out.println("         │");
                                System.out.println("         │ ✅ SELECTED Bundle ID: " + matchedBundle.getId());
                                System.out.println("         │    - GRN Number: " + matchedBundle.getGrnNumber());
                                System.out.println("         │    - Dimension: " + matchedBundle.getDimension());
                                System.out.println("         │    - Qty: " + matchedBundle.getWeightmentQuantityKg() + " KG");
                                System.out.println("         │    - Heat No: " + matchedBundle.getHeatNo());
                                System.out.println("         │    - Lot No: " + matchedBundle.getLotNo());
                                System.out.println("         │    - Test Certificate: " + matchedBundle.getTestCertificate());
                                System.out.println("         │    - Current Store: " + matchedBundle.getCurrentStore());
                                System.out.println("         │    - Storage Area: " + matchedBundle.getStorageArea());
                                System.out.println("         │    - Rack: " + matchedBundle.getRackColumnBinNumber());

                                // Copy bundle details
                                sourceBundleCopy = StockSummaryBundleEntity.builder()
                                        .grnNumber(matchedBundle.getGrnNumber())
                                        .grnId(matchedBundle.getGrnId())
                                        .slNo(matchedBundle.getSlNo())
                                        .itemDescription(matchedBundle.getItemDescription())
                                        .productCategory(matchedBundle.getProductCategory())
                                        .sectionNumber(matchedBundle.getSectionNumber())
                                        .brand(matchedBundle.getBrand())
                                        .grade(matchedBundle.getGrade())
                                        .temper(matchedBundle.getTemper())
                                        .dimension(matchedBundle.getDimension())
                                        .weighment(matchedBundle.getWeighment())
                                        .weightmentQuantityKg(matchedBundle.getWeightmentQuantityKg())
                                        .weightmentQuantityNo(matchedBundle.getWeightmentQuantityNo())
                                        .uomNetWeight(matchedBundle.getUomNetWeight())
                                        .uomNo(matchedBundle.getUomNo())
                                        .materialAcceptance(matchedBundle.getMaterialAcceptance())
                                        .poNumber(matchedBundle.getPoNumber())
                                        .heatNo(matchedBundle.getHeatNo())
                                        .lotNo(matchedBundle.getLotNo())
                                        .testCertificate(matchedBundle.getTestCertificate())
                                        .userId(matchedBundle.getUserId())
                                        .unitId(matchedBundle.getUnitId())
                                        .createdBy(matchedBundle.getCreatedBy())
                                        .qrCodeUrl(matchedBundle.getQrCodeUrl())
                                        .build();

                                // Delete ONLY the matched bundle
                                stockSummaryBundleRepository.delete(matchedBundle);
                                System.out.println("         │    🗑️ Bundle DELETED (ID: " + matchedBundle.getId() + ")");
                            } else {
                                System.out.println("         │ ⚠️ No bundle found to process");
                            }
                        } else {
                            System.out.println("         │ ⚠️ No bundles found with GRN: " + grnRefNo);
                        }
                    } else {
                        System.out.println("         │ ⚠️ No bundle ID or GRN provided - skipping bundle processing");
                    }

                        System.out.println("         │ ✅ STEP 1.5 COMPLETED SUCCESSFULLY");
                        System.out.println("         │    - sourceBundleCopy is " + (sourceBundleCopy != null ? "SET" : "NULL"));

                    } catch (Exception step15Ex) {
                        System.out.println("         │ ❌ STEP 1.5 FAILED: " + step15Ex.getMessage());
                        step15Ex.printStackTrace();
                        throw new RuntimeException("STEP 1.5 Failed - Bundle processing: " + step15Ex.getMessage(), step15Ex);
                    }

                    // ═══════════════════════════════════════════════════════════════════
                    // STEP 2: ADD TO DESTINATION STOCK SUMMARY
                    // Based on Order Type: FULL → Dispatch, CUT → Production
                    // ═══════════════════════════════════════════════════════════════════
                    try {
                        String orderType = scheduler.getOrderType();

                        // Determine target store based on order type
                        String targetStore;
                        String itemGroup;
                        if ("FULL".equalsIgnoreCase(orderType)) {
                            targetStore = "Dispatch";
                        itemGroup = "FG";  // Finished Goods
                    } else {
                        targetStore = "Production";
                        itemGroup = "SFG"; // Semi Finished Goods
                    }
                    String targetStorageArea = "Common";
                    String targetRackBin = "Common";

                    System.out.println("\n         📍 STEP 2: Adding to " + targetStore + " Stock Summary...");
                    System.out.println("         │ Order Type: " + orderType);
                    System.out.println("         │ Target Store: " + targetStore + " (based on Order Type: " + orderType + ")");
                    System.out.println("         │ Item Group: " + itemGroup + " (" + ("SFG".equals(itemGroup) ? "Semi Finished Goods" : "Finished Goods") + ")");
                    System.out.println("         │ Target Storage Area: " + targetStorageArea);
                    System.out.println("         │ Target Rack/Bin: " + targetRackBin);
                    System.out.println("         │ GRN Number to add: " + grnRefNo);
                    System.out.println("         │");
                    System.out.println("         │ Checking for existing match...");
                    System.out.println("         │    - Unit: " + scheduler.getUnit());
                    System.out.println("         │    - Item Desc: " + scheduler.getItemDescription());
                    System.out.println("         │    - Item Group: " + itemGroup);
                    System.out.println("         │    - Store: " + targetStore);
                    System.out.println("         │    - Storage Area: " + targetStorageArea);
                    System.out.println("         │    - Rack/Bin: " + targetRackBin);

                    Optional<StockSummaryEntity> existingOpt = stockSummaryRepository.findExactMatch(
                            scheduler.getUnit(),
                            scheduler.getItemDescription(),
                            itemGroup,
                            targetStore,
                            targetStorageArea,
                            targetRackBin
                    );

                    if (existingOpt.isPresent()) {
                        StockSummaryEntity existing = existingOpt.get();
                        BigDecimal existingQty = existing.getQuantityKg() != null ? existing.getQuantityKg() : BigDecimal.ZERO;
                        Integer existingQtyNo = existing.getQuantityNo() != null ? existing.getQuantityNo() : 0;

                        // Use weighed quantity (actual quantity) for stock update
                        BigDecimal qtyToAdd = weighedQtyKg != null ? weighedQtyKg : BigDecimal.ZERO;
                        Integer qtyNoToAdd = weighedQtyNo != null ? weighedQtyNo : 0;

                        BigDecimal newQty = existingQty.add(qtyToAdd);
                        Integer newQtyNo = existingQtyNo + qtyNoToAdd;

                        // Get dimension: Entry > Scheduler (priority order)
                        String dimensionToUse = entryDimension != null && !entryDimension.isEmpty() ? entryDimension :
                                                scheduler.getDimension();

                        System.out.println("         │");
                        System.out.println("         │ ✅ EXISTING STOCK FOUND - UPDATING");
                        System.out.println("         │    - Stock ID: " + existing.getId());
                        System.out.println("         │    - Qty BEFORE: " + existingQty + " KG | " + existingQtyNo + " NO");
                        System.out.println("         │    - Adding (weighed qty): " + qtyToAdd + " KG | " + qtyNoToAdd + " NO");
                        System.out.println("         │    - Qty AFTER: " + newQty + " KG | " + newQtyNo + " NO");
                        System.out.println("         │    - Dimension (from entry): " + dimensionToUse);

                        existing.setQuantityKg(newQty);
                        existing.setQuantityNo(newQtyNo);

                        // Update dimension if provided
                        if (dimensionToUse != null && !dimensionToUse.isEmpty()) {
                            existing.setDimension(dimensionToUse);
                        }

                        String existingGrns = existing.getGrnNumbers();
                        String grnsBefore = existingGrns;
                        if (existingGrns == null || existingGrns.isEmpty() || existingGrns.equals("[]")) {
                            existingGrns = "[\"" + grnRefNo + "\"]";
                        } else {
                            if (!existingGrns.contains("\"" + grnRefNo + "\"")) {
                                existingGrns = existingGrns.replace("]", ",\"" + grnRefNo + "\"]");
                            }
                        }
                        existing.setGrnNumbers(existingGrns);

                        System.out.println("         │    - GRNs BEFORE: " + grnsBefore);
                        System.out.println("         │    - GRNs AFTER: " + existingGrns);

                        StockSummaryEntity savedExisting = stockSummaryRepository.save(existing);
                        System.out.println("         │    ✅ SAVED Successfully");

                        // ═══════════════════════════════════════════════════════════════════
                        // STEP 2.5: SAVE BUNDLE TO DESTINATION STOCK with Weighment Quantity
                        // ═══════════════════════════════════════════════════════════════════
                        if (sourceBundleCopy != null) {
                            System.out.println("         │");
                            System.out.println("         │ 📦 STEP 2.5: Saving Bundle to Destination Stock...");

                            // Get weighment quantity from entry (input)
                            BigDecimal weighmentQtyKg = entry.getWeighedQuantityKg() != null ?
                                    entry.getWeighedQuantityKg() : retrievalQtyKg;
                            Integer weighmentQtyNo = entry.getWeighedQuantityNo() != null ?
                                    entry.getWeighedQuantityNo() : retrievalQtyNo;

                            System.out.println("         │    - Original Bundle Qty: " + sourceBundleCopy.getWeightmentQuantityKg() + " KG");
                            System.out.println("         │    - New Weighment Qty: " + weighmentQtyKg + " KG | " + weighmentQtyNo + " NO");

                            StockSummaryBundleEntity destinationBundle = StockSummaryBundleEntity.builder()
                                    .stockSummary(savedExisting)
                                    .grnNumber(sourceBundleCopy.getGrnNumber())
                                    .grnId(sourceBundleCopy.getGrnId())
                                    .slNo(sourceBundleCopy.getSlNo())
                                    .itemDescription(sourceBundleCopy.getItemDescription())
                                    .productCategory(sourceBundleCopy.getProductCategory())
                                    .sectionNumber(sourceBundleCopy.getSectionNumber())
                                    .brand(sourceBundleCopy.getBrand())
                                    .grade(sourceBundleCopy.getGrade())
                                    .temper(sourceBundleCopy.getTemper())
                                    .dimension(entryDimension != null ? entryDimension : sourceBundleCopy.getDimension())
                                    .weighment(sourceBundleCopy.getWeighment())
                                    .weightmentQuantityKg(weighmentQtyKg)  // Use weighment quantity from input
                                    .weightmentQuantityNo(weighmentQtyNo)  // Use weighment quantity from input
                                    .uomNetWeight(sourceBundleCopy.getUomNetWeight())
                                    .uomNo(sourceBundleCopy.getUomNo())
                                    .materialAcceptance(sourceBundleCopy.getMaterialAcceptance())
                                    .currentStore(targetStore)
                                    .storageArea(targetStorageArea)
                                    .rackColumnBinNumber(targetRackBin)
                                    .poNumber(sourceBundleCopy.getPoNumber())
                                    .heatNo(sourceBundleCopy.getHeatNo())
                                    .lotNo(sourceBundleCopy.getLotNo())
                                    .testCertificate(sourceBundleCopy.getTestCertificate())
                                    .userId(sourceBundleCopy.getUserId())
                                    .unitId(sourceBundleCopy.getUnitId())
                                    .status("TRANSFERRED")
                                    .transferType("STOCK_TRANSFER")
                                    .build();

                            StockSummaryBundleEntity savedBundle = stockSummaryBundleRepository.save(destinationBundle);
                            System.out.println("         │    ✅ Bundle SAVED to Destination - ID: " + savedBundle.getId());
                            System.out.println("         │       - Linked to Stock Summary ID: " + savedExisting.getId());
                            System.out.println("         │       - Weighment Qty: " + weighmentQtyKg + " KG");
                        }

                    } else {
                        BigDecimal itemPrice = null;
                        if (sourceStockForCopy != null) {
                            itemPrice = sourceStockForCopy.getItemPrice();
                        }

                        // Get dimension: Entry > Scheduler (priority order)
                        String dimensionToUse = entryDimension != null && !entryDimension.isEmpty() ? entryDimension :
                                                scheduler.getDimension();

                        // Use weighed quantity (actual quantity) for new stock
                        BigDecimal qtyToAdd = weighedQtyKg != null ? weighedQtyKg : BigDecimal.ZERO;
                        Integer qtyNoToAdd = weighedQtyNo != null ? weighedQtyNo : 0;

                        System.out.println("         │");
                        System.out.println("         │ 🆕 NO EXISTING STOCK - CREATING NEW");
                        System.out.println("         │    - Unit: " + scheduler.getUnit());
                        System.out.println("         │    - Store: " + targetStore);
                        System.out.println("         │    - Storage Area: " + targetStorageArea);
                        System.out.println("         │    - Rack/Bin: " + targetRackBin);
                        System.out.println("         │    - Product Category: " + scheduler.getProductCategory());
                        System.out.println("         │    - Item Description: " + scheduler.getItemDescription());
                        System.out.println("         │    - Brand: " + scheduler.getBrand());
                        System.out.println("         │    - Grade: " + scheduler.getGrade());
                        System.out.println("         │    - Temper: " + scheduler.getTemper());
                        System.out.println("         │    - Dimension (from entry): " + dimensionToUse);
                        System.out.println("         │    - Quantity (weighed): " + qtyToAdd + " KG | " + qtyNoToAdd + " NO");
                        System.out.println("         │    - Item Price: " + itemPrice);
                        System.out.println("         │    - Item Group: " + itemGroup);
                        System.out.println("         │    - GRNs: [\"" + grnRefNo + "\"]");

                        StockSummaryEntity newStock = StockSummaryEntity.builder()
                                .unit(scheduler.getUnit())
                                .store(targetStore)
                                .storageArea(targetStorageArea)
                                .rackColumnShelfNumber(targetRackBin)
                                .productCategory(scheduler.getProductCategory())
                                .itemDescription(scheduler.getItemDescription())
                                .brand(scheduler.getBrand())
                                .grade(scheduler.getGrade())
                                .temper(scheduler.getTemper())
                                .dimension(dimensionToUse)
                                .quantityKg(qtyToAdd)
                                .quantityNo(qtyNoToAdd)
                                .itemPrice(itemPrice)
                                .itemGroup(itemGroup)
                                .grnNumbers("[\"" + grnRefNo + "\"]")
                                .pickListLocked(false)
                                .reprintQr(false)
                                .build();

                        StockSummaryEntity savedStock = stockSummaryRepository.save(newStock);
                        System.out.println("         │    ✅ CREATED with ID: " + savedStock.getId());

                        // ═══════════════════════════════════════════════════════════════════
                        // STEP 2.5: SAVE BUNDLE TO NEW DESTINATION STOCK with Weighment Quantity
                        // ═══════════════════════════════════════════════════════════════════
                        if (sourceBundleCopy != null) {
                            System.out.println("         │");
                            System.out.println("         │ 📦 STEP 2.5: Saving Bundle to New Destination Stock...");

                            // Get weighment quantity from entry (input)
                            BigDecimal weighmentQtyKg = entry.getWeighedQuantityKg() != null ?
                                    entry.getWeighedQuantityKg() : retrievalQtyKg;
                            Integer weighmentQtyNo = entry.getWeighedQuantityNo() != null ?
                                    entry.getWeighedQuantityNo() : retrievalQtyNo;

                            System.out.println("         │    - Original Bundle Qty: " + sourceBundleCopy.getWeightmentQuantityKg() + " KG");
                            System.out.println("         │    - New Weighment Qty: " + weighmentQtyKg + " KG | " + weighmentQtyNo + " NO");

                            StockSummaryBundleEntity destinationBundle = StockSummaryBundleEntity.builder()
                                    .stockSummary(savedStock)
                                    .grnNumber(sourceBundleCopy.getGrnNumber())
                                    .grnId(sourceBundleCopy.getGrnId())
                                    .slNo(sourceBundleCopy.getSlNo())
                                    .itemDescription(sourceBundleCopy.getItemDescription())
                                    .productCategory(sourceBundleCopy.getProductCategory())
                                    .sectionNumber(sourceBundleCopy.getSectionNumber())
                                    .brand(sourceBundleCopy.getBrand())
                                    .grade(sourceBundleCopy.getGrade())
                                    .temper(sourceBundleCopy.getTemper())
                                    .dimension(entryDimension != null ? entryDimension : sourceBundleCopy.getDimension())
                                    .weighment(sourceBundleCopy.getWeighment())
                                    .weightmentQuantityKg(weighmentQtyKg)  // Use weighment quantity from input
                                    .weightmentQuantityNo(weighmentQtyNo)  // Use weighment quantity from input
                                    .uomNetWeight(sourceBundleCopy.getUomNetWeight())
                                    .uomNo(sourceBundleCopy.getUomNo())
                                    .materialAcceptance(sourceBundleCopy.getMaterialAcceptance())
                                    .currentStore(targetStore)
                                    .storageArea(targetStorageArea)
                                    .rackColumnBinNumber(targetRackBin)
                                    .poNumber(sourceBundleCopy.getPoNumber())
                                    .heatNo(sourceBundleCopy.getHeatNo())
                                    .lotNo(sourceBundleCopy.getLotNo())
                                    .testCertificate(sourceBundleCopy.getTestCertificate())
                                    .userId(sourceBundleCopy.getUserId())
                                    .unitId(sourceBundleCopy.getUnitId())
                                    .status("TRANSFERRED")
                                    .transferType("STOCK_TRANSFER")
                                    .build();

                            StockSummaryBundleEntity savedBundle = stockSummaryBundleRepository.save(destinationBundle);
                            System.out.println("         │    ✅ Bundle SAVED to New Stock - ID: " + savedBundle.getId());
                            System.out.println("         │       - Linked to Stock Summary ID: " + savedStock.getId());
                            System.out.println("         │       - Weighment Qty: " + weighmentQtyKg + " KG");
                        }
                    }

                    System.out.println("         │");
                    System.out.println("         │ ✅ STEP 2 & STEP 2.5 COMPLETED SUCCESSFULLY");

                    } catch (Exception step2Ex) {
                        System.out.println("         │ ❌ STEP 2/2.5 FAILED: " + step2Ex.getMessage());
                        step2Ex.printStackTrace();
                        throw new RuntimeException("STEP 2/2.5 Failed - Destination stock update: " + step2Ex.getMessage(), step2Ex);
                    }

                    System.out.println("         └─────────────────────────────────────────────────────────────");
                } // end of for loop

                System.out.println("\n      ══════════════════════════════════════════════════════════════════");
                System.out.println("      ✅ [StockProcessing] ORDER STOCK PROCESSING COMPLETE");
                System.out.println("      ══════════════════════════════════════════════════════════════════");
                // ========== END ORDER STOCK PROCESSING ==========

                // ========== PRODUCTION SCHEDULE INSERT (if nextProcess is "MARKING AND CUTTING") ==========
                System.out.println("\n      🏭 [ProductionSchedule] Checking if needs to insert...");
                System.out.println("         Next Process: " + dto.getNextProcess());

                String nextProcessUpper = dto.getNextProcess() != null ? dto.getNextProcess().toUpperCase() : "";

                if (nextProcessUpper.contains("MARKING & CUTTING") || nextProcessUpper.equals("MARKING AND CUTTING")) {

                    System.out.println("      📋 INSERTING TO PRODUCTION SCHEDULE");

                    try {
                        com.indona.invento.entities.ProductionScheduleEntity prodSchedule =
                            com.indona.invento.entities.ProductionScheduleEntity.builder()
                                .nextProcess(dto.getNextProcess())
                                .machineName(null)  // Keep empty as per requirement
                                .soNumber(dto.getSoNumber())
                                .lineNumber(dto.getLineNumber())
                                .unit(dto.getUnit())
                                .customerCode(null)  // Will be fetched from scheduler if needed
                                .customerName(null)  // Will be fetched from scheduler if needed
                                .packing(scheduler.getPacking())
                                .orderType(scheduler.getOrderType())
                                .productCategory(scheduler.getProductCategory())
                                .itemDescription(scheduler.getItemDescription())
                                .rmQuantityKg(dto.getWeighmentQuantityKg())  // RM Quantity = weighmentQuantityKg
                                .brand(dto.getBrand())
                                .grade(dto.getGrade())
                                .temper(dto.getTemper())
                                .dimension(scheduler.getDimension())
                                .requiredQuantityKg(dto.getRequiredQuantityKg())
                                .requiredQuantityNo(dto.getRequiredQuantityNo())
                                .targetBladeSpeed(null)  // Keep empty
                                .targetFeed(null)  // Keep empty
                                .targetDispatchDate(scheduler.getTargetDateOfDispatch())
                                .status("PENDING")  // Default status is PENDING
                                .build();

                        com.indona.invento.entities.ProductionScheduleEntity savedProd =
                            productionScheduleRepository.save(prodSchedule);

                        System.out.println("         ✅ PRODUCTION SCHEDULE INSERTED - ID: " + savedProd.getId());
                        System.out.println("            - SO: " + savedProd.getSoNumber());
                        System.out.println("            - Line: " + savedProd.getLineNumber());
                        System.out.println("            - Next Process: " + savedProd.getNextProcess());
                        System.out.println("            - Status: " + savedProd.getStatus());
                        System.out.println("            - RM Quantity (KG): " + savedProd.getRmQuantityKg());
                        System.out.println("            - Required Qty: " + savedProd.getRequiredQuantityKg() + " KG");

                    } catch (Exception e) {
                        System.out.println("         ⚠️ Error inserting to Production Schedule: " + e.getMessage());
                        // Don't fail the entire operation if production schedule insert fails
                    }
                } else {
                    System.out.println("      ⏭️ Next Process is not 'MARKING AND CUTTING' - skipping Production Schedule insert");
                }
                // ========== END PRODUCTION SCHEDULE INSERT ==========

                // ========== PACKING SCHEDULE INSERT (if nextProcess is "DISPATCH" and packing is true) ==========
                System.out.println("\n      📦 [PackingSchedule] Checking if needs to insert...");
                System.out.println("         Next Process: " + dto.getNextProcess());
                System.out.println("         Packing: " + scheduler.getPacking());

                boolean isDispatch = nextProcessUpper.contains("DISPATCH");
                Boolean packingFlag = scheduler.getPacking();

                if (isDispatch && Boolean.TRUE.equals(packingFlag)) {
                    System.out.println("      📋 INSERTING TO PACKING SCHEDULE (NextProcess=DISPATCH & Packing=true)");

                    try {
                        // Check if already exists
                        PackingEntityScheduler existingPacking = packingSchedulerRepository.findBySoNumberAndLineNumber(
                                dto.getSoNumber(), dto.getLineNumber());

                        if (existingPacking != null) {
                            System.out.println("         ⚠️ Packing Schedule already exists - ID: " + existingPacking.getId() + " - SKIPPING");
                        } else {
                            PackingEntityScheduler packingSchedule = PackingEntityScheduler.builder()
                                    .soNumber(dto.getSoNumber())
                                    .lineNumber(dto.getLineNumber())
                                    .unit(dto.getUnit())
                                    .customerCode(scheduler.getCustomerCode())
                                    .customerName(scheduler.getCustomerName())
                                    .orderType(scheduler.getOrderType())
                                    .productCategory(scheduler.getProductCategory())
                                    .itemDescription(dto.getItemDescription())
                                    .brand(dto.getBrand())
                                    .grade(dto.getGrade())
                                    .temper(dto.getTemper())
                                    .dimension(scheduler.getDimension())
                                    .quantityKg(dto.getWeighmentQuantityKg())
                                    .uomKg(scheduler.getUomKg())
                                    .quantityNo(dto.getWeighmentQuantityNo())
                                    .uomNo(scheduler.getUomNo())
                                    .targetDateOfDispatch(scheduler.getTargetDateOfDispatch())
                                    .packingInstructions(null)  // Initially null
                                    .packingStatus("PENDING")   // Default status
                                    .build();

                            PackingEntityScheduler savedPacking = packingSchedulerRepository.save(packingSchedule);

                            System.out.println("         ✅ PACKING SCHEDULE INSERTED - ID: " + savedPacking.getId());
                            System.out.println("            - SO: " + savedPacking.getSoNumber());
                            System.out.println("            - Line: " + savedPacking.getLineNumber());
                            System.out.println("            - Order Type: " + savedPacking.getOrderType());
                            System.out.println("            - Item: " + savedPacking.getItemDescription());
                            System.out.println("            - Qty: " + savedPacking.getQuantityKg() + " KG | " + savedPacking.getQuantityNo() + " NO");
                            System.out.println("            - Status: " + savedPacking.getPackingStatus());
                            System.out.println("            - Target Dispatch: " + savedPacking.getTargetDateOfDispatch());
                        }

                    } catch (Exception e) {
                        System.out.println("         ⚠️ Error inserting to Packing Schedule: " + e.getMessage());
                        e.printStackTrace();
                        // Don't fail the entire operation if packing schedule insert fails
                    }
                } else {
                    if (!isDispatch) {
                        System.out.println("      ⏭️ Next Process is not 'DISPATCH' - skipping Packing Schedule insert");
                    } else if (!Boolean.TRUE.equals(packingFlag)) {
                        System.out.println("      ⏭️ Packing is FALSE/NULL - skipping Packing Schedule insert");
                    }
                }
                // ========== END PACKING SCHEDULE INSERT ==========

                SalesOrderSchedulerEntity saved = repository.save(scheduler);
                savedEntities.add(saved);

                System.out.println("\n      💾 SCHEDULER SAVED:");
                System.out.println("         - Scheduler ID: " + saved.getId());
                System.out.println("         - SO Number: " + saved.getSoNumber());
                System.out.println("         - Line Number: " + saved.getLineNumber());
                System.out.println("         - Retrieval Status: " + saved.getRetrievalStatus());
                System.out.println("         - Weighment Qty: " + dto.getWeighmentQuantityKg() + " KG");
                System.out.println("         - Returnable Qty: " + dto.getReturnableQuantityKg() + " KG");
                System.out.println("         - Total Retrieval Entries: " + retrievalEntityList.size());

            } catch (Exception e) {
                String error = "Error saving SO: " + dto.getSoNumber() + " | Line: " + dto.getLineNumber() + " - " + e.getMessage();
                System.out.println("\n   ❌ ERROR: " + error);
                e.printStackTrace();
                errors.add(error);
            }
        }

        System.out.println("\n══════════════════════════════════════════════════════════════════════════");
        System.out.println("✅ [SaveStockTransfer] COMPLETE");
        System.out.println("   - Total Saved: " + savedEntities.size());
        System.out.println("   - Total Errors: " + errors.size());
        System.out.println("══════════════════════════════════════════════════════════════════════════\n");

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();
        response.put("success", errors.isEmpty());
        response.put("message", errors.isEmpty() ? "Stock transfer saved successfully" : "Stock transfer saved with some errors");
        response.put("savedCount", savedEntities.size());
        response.put("errorCount", errors.size());
        if (!errors.isEmpty()) {
            response.put("errors", errors);
        }
        response.put("data", savedEntities);

        return response;
    }

    @Override
    public java.util.Map<String, Object> getStockTransferRetrievalEntries(String soNumber, String lineNumber) {
        System.out.println("\n🔍 [StockTransfer-Entries] Fetching retrieval entries for SO: " + soNumber + " | Line: " + lineNumber);

        java.util.Map<String, Object> response = new java.util.LinkedHashMap<>();

        try {
            // Get scheduler by SO and Line number
            SalesOrderSchedulerEntity scheduler = repository.findBySoNumberAndLineNumber(soNumber, lineNumber);

            if (scheduler == null) {
                System.out.println("❌ Scheduler not found for SO: " + soNumber + " | Line: " + lineNumber);
                response.put("success", false);
                response.put("message", "Scheduler not found for this SO and Line number");
                response.put("data", null);
                return response;
            }

            System.out.println("✅ Scheduler found → ID: " + scheduler.getId());

            // Get stock transfer
            WarehouseStockTransferEntityScheduler stockTransfer = scheduler.getStockTransfer();

            if (stockTransfer == null) {
                System.out.println("❌ Stock transfer not found for this scheduler");
                response.put("success", false);
                response.put("message", "No stock transfer data found for this SO and Line number");
                response.put("data", null);
                return response;
            }

            System.out.println("✅ Stock transfer found → ID: " + stockTransfer.getId());

            // Get retrieval entries
            java.util.List<WarehouseStockRetrievalEntityScheduler> retrievalEntries = stockTransfer.getRetrievalEntries();

            if (retrievalEntries == null || retrievalEntries.isEmpty()) {
                System.out.println("⚠️ No retrieval entries found");
                response.put("success", true);
                response.put("message", "No retrieval entries found for this stock transfer");
                response.put("totalCount", 0);
                response.put("data", new java.util.ArrayList<>());
                return response;
            }

            System.out.println("✅ Found " + retrievalEntries.size() + " retrieval entries");

            // Convert to response format
            java.util.List<java.util.Map<String, Object>> entriesList = new java.util.ArrayList<>();

            for (WarehouseStockRetrievalEntityScheduler entry : retrievalEntries) {
                java.util.Map<String, Object> entryMap = new java.util.LinkedHashMap<>();
                entryMap.put("id", entry.getId());
                entryMap.put("store", entry.getStore());
                entryMap.put("storageArea", entry.getStorageArea());
                entryMap.put("rackColumnBin", entry.getRackColumnBin());
                entryMap.put("retrievalQuantityKg", entry.getRetrievalQuantityKg());
                entryMap.put("retrievalQuantityNo", entry.getRetrievalQuantityNo());
                entryMap.put("batchNumber", entry.getBatchNumber());
                entryMap.put("dateOfInward", entry.getDateOfInward());
                entryMap.put("qrCodeRM", entry.getQrCodeRM());
                entryMap.put("scanQrCode", entry.getScanQrCode());
                entryMap.put("retrievedQuantityKg", entry.getRetrievedQuantityKg());
                entryMap.put("retrievedQuantityNo", entry.getRetrievedQuantityNo());
                entryMap.put("weighedQuantityKg", entry.getWeighedQuantityKg());
                entryMap.put("weighedQuantityNo", entry.getWeighedQuantityNo());
                entryMap.put("generatePrintQrFG", entry.getGeneratePrintQrFG());
                entryMap.put("returnableQuantityKg", entry.getReturnableQuantityKg());
                entryMap.put("returnableQuantityNo", entry.getReturnableQuantityNo());

                entriesList.add(entryMap);
                System.out.println("   ✅ Entry: Rack=" + entry.getRackColumnBin() + " | RetrievalQty=" + entry.getRetrievalQuantityKg() + " KG");
            }

            response.put("success", true);
            response.put("message", "Retrieval entries fetched successfully");
            response.put("soNumber", soNumber);
            response.put("lineNumber", lineNumber);
            response.put("totalCount", entriesList.size());
            response.put("data", entriesList);

            System.out.println("✅ [StockTransfer-Entries] Fetch complete\n");

        } catch (Exception e) {
            System.out.println("❌ Error fetching retrieval entries: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error fetching retrieval entries: " + e.getMessage());
            response.put("data", null);
        }

        return response;
    }

}

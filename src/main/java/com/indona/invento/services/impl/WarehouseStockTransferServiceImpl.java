package com.indona.invento.services.impl;

import com.indona.invento.dao.SalesOrderSchedulerRepository;
import com.indona.invento.dao.StockSummaryRepository;
import com.indona.invento.dao.WarehouseStockTransferRepository;
import com.indona.invento.dto.WarehouseStockReturnEntryDTO;
import com.indona.invento.dto.WarehouseStockReturnRequestDTO;
import com.indona.invento.dto.WarehouseStockTransferRequestDTO;
import com.indona.invento.dto.WarehouseStockRetrievalEntryDTO;
import com.indona.invento.entities.SalesOrderSchedulerEntity;
import com.indona.invento.entities.StockSummaryEntity;
import com.indona.invento.entities.WarehouseStockRetrievalEntity;
import com.indona.invento.entities.WarehouseStockTransferEntity;
import com.indona.invento.services.WarehouseStockTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseStockTransferServiceImpl implements WarehouseStockTransferService {

    private final WarehouseStockTransferRepository repository;

    private final StockSummaryRepository stockSummaryRepository;

    private final SalesOrderSchedulerRepository salesOrderSchedulerRepository;

    @Override
    public WarehouseStockTransferEntity processWarehouseTransfer(WarehouseStockTransferRequestDTO request) {
        WarehouseStockTransferEntity transfer = WarehouseStockTransferEntity.builder()
                .soNumber(request.getSoNumber())
                .lineNumber(request.getLineNumber())
                .nextProcess(request.getNextProcess())
                .requiredQuantityKg(request.getRequiredQuantityKg())
                .requiredQuantityNo(request.getRequiredQuantityNo())
                .weighmentQuantityKg(request.getWeighmentQuantityKg())
                .weighmentQuantityNo(request.getWeighmentQuantityNo())
                .returnableQuantityKg(request.getReturnableQuantityKg())
                .returnableQuantityNo(request.getReturnableQuantityNo())
                .itemDescription(request.getItemDescription())
                .brand(request.getBrand())
                .grade(request.getGrade())
                .temper(request.getTemper())
                .unit(request.getUnit())
                .generateQr(request.getGenerateQr())
                .build();

        transfer.setRetrievalEntries(
                request.getRetrievalEntries().stream()
                        .map(entry -> WarehouseStockRetrievalEntity.builder()
                                .retrievalQuantityKg(entry.getRetrievalQuantityKg())
                                .retrievalQuantityNo(entry.getRetrievalQuantityNo())
                                .stockTransfer(transfer)
                                .build())
                        .collect(Collectors.toList())
        );

        WarehouseStockTransferEntity saved = repository.save(transfer);
        System.out.println("✅ Transfer saved for SO: " + saved.getSoNumber());
        return saved;
    }
    @Override
    public List<StockSummaryEntity> processReturn(WarehouseStockReturnRequestDTO request) {
        System.out.println("\n========== /warehouse-stock-transfer/return API CALLED ==========");
        System.out.println("📦 Processing warehouse stock return (UPSERT)");
        System.out.println("   SO Number: " + request.getSoNumber());
        System.out.println("   Line Number: " + request.getLineNumber());
        System.out.println("   Return Entries: " + request.getReturnEntries().size());
        System.out.println("================================================================\n");

        List<StockSummaryEntity> savedList = new ArrayList<>();

        for (WarehouseStockReturnEntryDTO entry : request.getReturnEntries()) {
            String unit = entry.getUnit();
            String itemDesc = entry.getItemDescription();
            String store = "warehouse";
            String storageArea = "loose";
            String rack = "common";

            // 🔍 FIX #3: UPSERT — Check if matching stock entry already exists
            java.util.Optional<StockSummaryEntity> existingOpt = stockSummaryRepository
                    .findExactMatchWithoutItemGroup(unit, itemDesc, store, storageArea, rack);

            StockSummaryEntity stockEntry;
            if (existingOpt.isPresent()) {
                // ♻️ UPDATE existing entry — add quantity
                stockEntry = existingOpt.get();
                java.math.BigDecimal oldKg = stockEntry.getQuantityKg() != null
                        ? stockEntry.getQuantityKg() : java.math.BigDecimal.ZERO;
                int oldNo = stockEntry.getQuantityNo() != null ? stockEntry.getQuantityNo() : 0;

                java.math.BigDecimal addKg = entry.getWeighmentQuantityKg() != null
                        ? entry.getWeighmentQuantityKg() : java.math.BigDecimal.ZERO;

                stockEntry.setQuantityKg(oldKg.add(addKg));
                stockEntry.setQuantityNo(oldNo + 1);

                System.out.println("   ♻️ UPSERT UPDATE: " + itemDesc + " → existing ID " + stockEntry.getId());
                System.out.println("      Old Qty: " + oldKg + " KG → New Qty: " + stockEntry.getQuantityKg() + " KG");
            } else {
                // ✨ CREATE new entry
                stockEntry = StockSummaryEntity.builder()
                        .unit(unit)
                        .store(store)
                        .storageArea(storageArea)
                        .rackColumnShelfNumber(rack)
                        .itemDescription(itemDesc)
                        .brand(entry.getBrand())
                        .grade(entry.getGrade())
                        .temper(entry.getTemper())
                        .quantityKg(entry.getWeighmentQuantityKg())
                        .quantityNo(1)
                        .materialType("")
                        .pickListLocked(false)
                        .build();

                System.out.println("   ✨ UPSERT CREATE: " + itemDesc + " → new entry");
            }

            StockSummaryEntity saved = stockSummaryRepository.save(stockEntry);
            savedList.add(saved);
        }

        // 🔄 Update SalesOrderScheduler with completedTime
        try {
            String soNumber = request.getSoNumber();
            String lineNumber = request.getLineNumber();

            System.out.println("🔍 Looking for SalesOrderScheduler: SO=" + soNumber + " | Line=" + lineNumber);

            SalesOrderSchedulerEntity scheduler = salesOrderSchedulerRepository.findBySoNumberAndLineNumber(soNumber, lineNumber);

            if (scheduler != null) {
                LocalDateTime now = LocalDateTime.now();
                scheduler.setCompletedTime(now);

                System.out.println("✅ Found SalesOrderScheduler!");
                System.out.println("   Setting completedTime: " + now);

                salesOrderSchedulerRepository.save(scheduler);

                System.out.println("✅ SalesOrderScheduler updated with completedTime!");
            } else {
                System.out.println("⚠️  SalesOrderScheduler not found for SO: " + soNumber + " | Line: " + lineNumber);
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating SalesOrderScheduler: " + e.getMessage());
            e.printStackTrace();
        }

        return savedList;
    }

}


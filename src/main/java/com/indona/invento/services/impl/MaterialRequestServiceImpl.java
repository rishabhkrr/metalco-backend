package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.*;
import com.indona.invento.entities.*;
import com.indona.invento.services.MaterialRequestService;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialRequestServiceImpl implements MaterialRequestService {

    private final MaterialRequestHeaderRepository headerRepo;
    private final MaterialRequestSummaryHeaderRepository summaryRepo;
    private final StockSummaryRepository stockSummaryRepository;
    private final SOSchedulePickListRepository SOSchedulePickListRepository;
    private final StockTransferWarehouseRepository stockTransferWarehouseRepository;
    private final StockTransferWHReturnRepository stockTransferWHReturnRepository;
    private final SOSchedulePickListRepository soSchedulePickListRepository;

    private String generateMrNumber() {
        String prefix = "MEMR";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int count = headerRepo.countByDate(LocalDate.now()) + 1;
        String sequence = String.format("%03d", count);
        return prefix + datePart + sequence;
    }

    private String generateLineNumber() {
        String prefix = "LINE";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int count = headerRepo.countByDate(LocalDate.now()) + 1;
        String sequence = String.format("%03d", count);
        return prefix + datePart + sequence;
    }

    @Override
    public MaterialRequestHeader createMaterialRequest(MaterialRequestDTO dto) {
        String mrNumber = generateMrNumber();
        LocalDateTime now = LocalDateTime.now();

        // 🔹 Save Header
        MaterialRequestHeader header = MaterialRequestHeader.builder()
                .timestamp(now)
                .mrNumber(mrNumber)
                .unitCode(dto.getUnitCode())
                .unitName(dto.getUnitName())
                .requestingUnit(dto.getRequestingUnit())
                .requestingUnitUnitCode(dto.getRequestingUnitUnitCode())
                .deliveryAddress(dto.getDeliveryAddress())
                .build();

        // 🔹 Generate UNIQUE line numbers (per MR)
        List<MaterialRequestItem> items = new ArrayList<>();
        int lineCounter = 1;

        for (MaterialRequestItemDTO i : dto.getItems()) {

            // UNIQUE PER ITEM
            String lineNumber = "LINE" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyMMdd")) +
                    String.format("%03d", lineCounter++);

            MaterialRequestItem item = MaterialRequestItem.builder()
                    .itemDescription(i.getItemDescription())
                    .materialType(i.getMaterialType())
                    .productCategory(i.getProductCategory())
                    .brand(i.getBrand())
                    .grade(i.getGrade())
                    .temper(i.getTemper())
                    .requiredQuantity(i.getRequiredQuantity())
                    .uom(i.getUom())
                    .status("PENDING")
                    .lineNumber(lineNumber)       // ✔ unique per item
                    .materialRequestHeader(header)
                    .build();

            items.add(item);
        }

        header.setItems(items);
        headerRepo.save(header);

        // 🔹 Save Summary
        MaterialRequestSummaryHeader summary = MaterialRequestSummaryHeader.builder()
                .timestamp(now)
                .mrNumber(mrNumber)
                .unitCode(dto.getUnitCode())
                .unitName(dto.getUnitName())
                .requestingUnit(dto.getRequestingUnit())
                .requestingUnitUnitCode(dto.getRequestingUnitUnitCode())
                .deliveryAddress(dto.getDeliveryAddress())
                .Status("PENDING")
                .build();

        // ✅ Header items se line number reuse karo
        List<MaterialRequestSummaryItem> summaryItems = items.stream().map(item ->
                MaterialRequestSummaryItem.builder()
                        .itemDescription(item.getItemDescription())
                        .materialType(item.getMaterialType())
                        .productCategory(item.getProductCategory())
                        .brand(item.getBrand())
                        .grade(item.getGrade())
                        .temper(item.getTemper())
                        .requiredQuantity(item.getRequiredQuantity())
                        .uom(item.getUom())
                        .status(item.getStatus())
                        .lineNumber(item.getLineNumber())   // ✔ SAME UNIQUE line number
                        .summaryHeader(summary)
                        .build()
        ).toList();

        summary.setItems(summaryItems);
        summaryRepo.save(summary);

        return header;
    }


    @Override
    public List<MaterialRequestSummaryResponseDTO> getAllSummaries() {
        List<MaterialRequestSummaryHeader> headers = summaryRepo.findAll();

        return headers.stream().map(header -> {
            List<MaterialRequestSummaryItemDTO> itemDTOs = header.getItems().stream().map(item ->
                    new MaterialRequestSummaryItemDTO(
                            item.getItemDescription(),
                            item.getMaterialType(),
                            item.getProductCategory(),
                            item.getBrand(),
                            item.getGrade(),
                            item.getTemper(),
                            item.getRequiredQuantity(),
                            item.getUom(),
                            item.getStatus(),
                            item.getLineNumber()
                    )
            ).toList();

            return new MaterialRequestSummaryResponseDTO(
                    header.getId(),
                    header.getMrNumber(),
                    header.getUnitCode(),
                    header.getUnitName(),
                    header.getRequestingUnit(),
                    header.getDeliveryAddress(),
                    header.getStatus(),
                    header.getRequestingUnitUnitCode(),
                    header.getTimestamp(),
                    itemDTOs
            );
        }).toList();
    }

    @Override
    public List<MaterialTransferScheduleDto> getAllSummaryFromMaterialRequest(String mrNumber, String lineNumber) {
        String normalizedMr = (mrNumber == null) ? null : mrNumber.trim();
        String normalizedLine = (lineNumber == null) ? null : lineNumber.trim();

        List<MaterialRequestSummaryHeader> headers;
        if (normalizedMr == null || normalizedMr.isEmpty()) {
            headers = summaryRepo.findAll();
        } else {
            headers = summaryRepo.findByMrNumberAndLineNumberWithItems(normalizedMr, normalizedLine);
        }

        // load pick-list entries once and index them for quick lookup
        List<SOSchedulePickListEntity> pickEntries = soSchedulePickListRepository.findAll();

        Map<String, SOSchedulePickListEntity> pickLookup = new HashMap<>();

        for (SOSchedulePickListEntity p : pickEntries) {
            String compositeKey = makeKey(p.getMrNumber(), p.getUnitName(), p.getItemDescription(), p.getLineNumber());
            if (compositeKey != null) pickLookup.putIfAbsent(compositeKey, p);
        }

        return headers.stream()
                .filter(Objects::nonNull)
                .flatMap(header -> {
                    List<MaterialRequestSummaryItem> items = header.getItems();
                    if (items == null || items.isEmpty()) {
                        return Stream.empty();
                    }

                    return items.stream().map(item -> {
                        MaterialTransferScheduleDto dto = new MaterialTransferScheduleDto();
                        dto.setId(header.getId());
                        dto.setMrNumber(header.getMrNumber());
                        dto.setUnitCode(header.getUnitCode());
                        dto.setUnitName(header.getUnitName());
                        dto.setRequestingUnit(header.getRequestingUnit());
                        dto.setDeliveryAddress(header.getDeliveryAddress());
                        dto.setStatus(item.getStatus());
                        dto.setTimestamp(header.getTimestamp());
                        dto.setItemDescription(item.getItemDescription());
                        dto.setMaterialType(item.getMaterialType());
                        dto.setProductCategory(item.getProductCategory());
                        dto.setBrand(item.getBrand());
                        dto.setGrade(item.getGrade());
                        dto.setTemper(item.getTemper());
                        dto.setRequiredQuantity(item.getRequiredQuantity());
                        dto.setUom(item.getUom());
                        dto.setItemStatus(item.getStatus());
                        dto.setLineNumber(item.getLineNumber());
                        dto.setRequestingUnitUnitCode(header.getRequestingUnitUnitCode());

                        // --- Simple lookup from stockSummary based on unit + itemDescription + store = 'Warehouse'
                        String unitName = header.getUnitCode();
                        String itemDesc = item.getItemDescription();

                        List<StockSummaryEntity> stocks = Collections.emptyList();
                        if (unitName != null && itemDesc != null) {
                            stocks = stockSummaryRepository.findByUnitAndItemDescriptionAndStore(unitName, itemDesc, "Warehouse");
                        }

                        // Sum retrieval quantities from stock rows
                        BigDecimal retrievalKgSum = BigDecimal.ZERO;
                        int retrievalNoSum = 0;
                        if (stocks != null && !stocks.isEmpty()) {
                            for (StockSummaryEntity s : stocks) {
                                BigDecimal qKg = s.getQuantityKg() == null ? BigDecimal.ZERO : s.getQuantityKg();
                                retrievalKgSum = retrievalKgSum.add(qKg);
                                int qNo = s.getQuantityNo() == null ? 0 : s.getQuantityNo();
                                retrievalNoSum += qNo;
                            }
                        }

                        dto.setRetrievalQuantityKg(retrievalKgSum);
                        dto.setRetrievalQuantityNo(retrievalNoSum);

                        // compute lookupUnit robustly (prefer unitName but fallback to unitCode)
                        String lookupUnit = header.getUnitName() != null ? header.getUnitName().trim() :
                                (header.getUnitCode() != null ? header.getUnitCode().trim() : null);

                        if (lookupUnit != null && itemDesc != null) {
                            stocks = stockSummaryRepository.findByUnitAndItemDescriptionAndStore(lookupUnit, itemDesc, "Warehouse");
                        }
                        log.debug("stocks fetched (unit='{}', item='{}') -> size={}", lookupUnit, itemDesc, stocks == null ? 0 : stocks.size());

                        BigDecimal requiredKg = item.getRequiredQuantity() == null ? BigDecimal.ZERO : BigDecimal.valueOf(item.getRequiredQuantity());

                        String storageArea = null;
                        if (stocks != null && !stocks.isEmpty()) {
                            stocks.forEach(s -> log.debug("stock id={} qtyKg={} rack='{}'", s.getId(), s.getQuantityKg(), s.getRackColumnShelfNumber()));

                            StockSummaryEntity bestFit = stocks.stream()
                                    .filter(Objects::nonNull)
                                    .filter(s -> s.getQuantityKg() != null)
                                    .filter(s -> s.getQuantityKg().compareTo(requiredKg) >= 0)
                                    .min(Comparator.comparing(s -> s.getQuantityKg().subtract(requiredKg)))
                                    .orElse(null);

                            if (bestFit == null) {
                                bestFit = stocks.stream()
                                        .filter(Objects::nonNull)
                                        .filter(s -> s.getQuantityKg() != null)
                                        .max(Comparator.comparing(StockSummaryEntity::getQuantityKg))
                                        .orElse(null);
                            }

                            if (bestFit != null) {
                                log.debug("bestFit chosen id={} qty={} rack='{}'", bestFit.getId(), bestFit.getQuantityKg(), bestFit.getRackColumnShelfNumber());
                                // primary
                                storageArea = bestFit.getRackColumnShelfNumber();
                                // fallback: any non-null rack value in stocks
                                if (storageArea == null || storageArea.isBlank()) {
                                    storageArea = stocks.stream()
                                            .map(StockSummaryEntity::getRackColumnShelfNumber)
                                            .filter(Objects::nonNull)
                                            .filter(s -> !s.isBlank())
                                            .findFirst()
                                            .orElse(null);
                                }
                            } else {
                                log.debug("no bestFit found even after fallback max search");
                            }
                        } else {
                            log.debug("no stock rows found for unit='{}' item='{}'", lookupUnit, itemDesc);
                        }

                        dto.setStorageArea(storageArea);


                        String lookupKeyByMr = makeKey(header.getMrNumber(), header.getUnitName(), itemDesc, item.getLineNumber());

                        SOSchedulePickListEntity pickMatch = null;
                        if (lookupKeyByMr != null && pickLookup.containsKey(lookupKeyByMr)) {
                            pickMatch = pickLookup.get(lookupKeyByMr);
                        }

                        // If pick-list match found, overwrite dto values with pick entity values (only when non-null)
                        if (pickMatch != null) {
                            applyPickToDto(pickMatch, dto, header);
                        }

                        return dto;
                    });
                })
                .collect(Collectors.toList());
    }

    /**
     * Copy fields from SOSchedulePickListEntity to DTO, but be cautious:
     * - Prefer non-null values from pick entity
     * - Keep some header-derived values (id, mrNumber, unitCode) intact unless pick explicitly provides them
     */
    private void applyPickToDto(SOSchedulePickListEntity pick, MaterialTransferScheduleDto dto, MaterialRequestSummaryHeader header) {
        // Status fields
        if (pick.getItemStatus() != null) {
            dto.setStatus(pick.getItemStatus());
            dto.setItemStatus(pick.getItemStatus());
        }

        // Quantities
        if (pick.getRetrievalQuantityKg() != null) {
            dto.setRetrievalQuantityKg(pick.getRetrievalQuantityKg());
        }
        if (pick.getRetrievalQuantityNo() != null) {
            dto.setRetrievalQuantityNo(pick.getRetrievalQuantityNo());
        }

        // Storage area / location
        if (pick.getStorageArea() != null) {
            dto.setStorageArea(pick.getStorageArea());
        }

        // Item attributes - override when present in pick
        if (pick.getBrand() != null) dto.setBrand(pick.getBrand());
        if (pick.getGrade() != null) dto.setGrade(pick.getGrade());
        if (pick.getTemper() != null) dto.setTemper(pick.getTemper());
        if (pick.getProductCategory() != null) dto.setProductCategory(pick.getProductCategory());
        if (pick.getMaterialType() != null) dto.setMaterialType(pick.getMaterialType());
        if (pick.getUom() != null) dto.setUom(pick.getUom());
        if (pick.getId() != null) dto.setId(pick.getId());

        // Required quantity override only if pick has a value
        if (pick.getRequiredQuantity() != null) dto.setRequiredQuantity(pick.getRequiredQuantity());

        if (pick.getLineNumber() != null) dto.setLineNumber(pick.getLineNumber());

    }

    private String makeKey(String mrNumber, String unitName, String itemDescription, String lineNumber) {
        boolean hasMr = mrNumber != null && !mrNumber.isBlank();
        boolean hasUnit = unitName != null && !unitName.isBlank();
        boolean hasDesc = itemDescription != null && !itemDescription.isBlank();
        boolean hasLine = lineNumber != null && !lineNumber.isBlank();

        // if nothing meaningful, return null
        if (!hasMr && !hasUnit && !hasDesc) return null;

        StringBuilder sb = new StringBuilder();
        if (hasMr) sb.append(mrNumber.trim().toLowerCase());
        sb.append("|");
        if (hasUnit) sb.append(unitName.trim().toLowerCase());
        sb.append("|");
        if (hasDesc) sb.append(itemDescription.trim().toLowerCase());
        sb.append("|");
        if (hasLine) sb.append(lineNumber.trim());

        return sb.toString();
    }

    @Override
    public List<Map<String, Object>> findPickListRackDetails(String unit, String itemDescription, String store) {
        if (unit == null || itemDescription == null) {
            return Collections.emptyList();
        }

        String effectiveStore = (store == null || store.isBlank()) ? "warehouse" : store.trim();

        return stockSummaryRepository.findByUnitAndItemDescriptionAndStore(unit.trim(), itemDescription.trim(), effectiveStore)
                .stream()
                .map(stock -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("rackColumnShelfNumber", stock.getRackColumnShelfNumber());
                    map.put("quantityKg", stock.getQuantityKg());
                    map.put("quantityNo", stock.getQuantityNo());
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public SOSchedulePickListEntity saveIUMaterial(SOSchedulePickListEntity entity) {
        return SOSchedulePickListRepository.save(entity);
    }

    @Override
    @Transactional
    public StockTransferWarehouseDto saveStockTransferWarehouse(StockTransferWarehouseDto dto) {
        StockTransferWarehouseEntity entity = mapDtoToEntity(dto);

        StockTransferWarehouseEntity saved = stockTransferWarehouseRepository.save(entity);

        return mapEntityToDto(saved);
    }

    @Transactional
    @Override
    public StockTransferWHReturnEntity saveStockTransfer(StockTransferWHReturnDto dto, Long SOSchedulePickListId) {
        StockTransferWHReturnEntity entity = new StockTransferWHReturnEntity();
        entity.setMrNumber(dto.getMrNumber());
        entity.setLineNumber(dto.getLineNumber());

        if (dto.getReturnEntries() != null) {
            for (ReturnEntryDto r : dto.getReturnEntries()) {
                ReturnEntryEntity entry = mapEntryDtoToEntity(r);
                entity.addReturnEntry(entry);
            }
        }

        if (SOSchedulePickListId != null) {
            Optional<SOSchedulePickListEntity> pickListOpt = SOSchedulePickListRepository.findById(SOSchedulePickListId);

            pickListOpt.ifPresent(pickList -> {
                pickList.setItemStatus("COMPLETED");
                pickList.setStatus("COMPLETED");

                SOSchedulePickListRepository.save(pickList);
            });
        }

        return stockTransferWHReturnRepository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferWarehouseDto getStockTransferWarehouseById(Long id) {
        StockTransferWarehouseEntity entity = stockTransferWarehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock Transfer Warehouse not found with id: " + id));

        return mapEntityToDto(entity);
    }

    @Override
    public SOSchedulePickListEntity getById(Long id) {
        return SOSchedulePickListRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("IU Material not found for id: " + id));
    }

    @Override
    public SOSchedulePickListEntity updateIUMaterial(Long id, SOSchedulePickListEntity updated) {

        SOSchedulePickListEntity existing = SOSchedulePickListRepository.findById(id).orElse(null);

        if (existing == null) {
            return null;
        }

        existing.setBrand(updated.getBrand());
        existing.setDeliveryAddress(updated.getDeliveryAddress());
        existing.setGrade(updated.getGrade());
        existing.setItemDescription(updated.getItemDescription());
        if(existing.getItemStatus().equalsIgnoreCase("COMPLETED")){
            updated.setItemStatus("COMPLETED");
        }else{
            existing.setItemStatus(updated.getItemStatus());
        }
        existing.setLineNumber(updated.getLineNumber());
        existing.setMaterialType(updated.getMaterialType());
        existing.setMrNumber(updated.getMrNumber());
        existing.setNextProcess(updated.getNextProcess());
        existing.setProductCategory(updated.getProductCategory());
        existing.setRequestingUnit(updated.getRequestingUnit());
        existing.setRequiredQuantity(updated.getRequiredQuantity());
        existing.setRetrievalQuantityKg(updated.getRetrievalQuantityKg());
        existing.setRetrievalQuantityNo(updated.getRetrievalQuantityNo());
        if(existing.getStatus().equalsIgnoreCase("COMPLETED")){
            updated.setStatus("COMPLETED");
        }else{
            existing.setStatus(updated.getStatus());
        }
        existing.setStorageArea(updated.getStorageArea());
        existing.setTemper(updated.getTemper());
        existing.setUnitCode(updated.getUnitCode());
        existing.setUnitName(updated.getUnitName());
        existing.setUom(updated.getUom());
        existing.setWeighmentQuantityKg(updated.getWeighmentQuantityKg());
        existing.setWeighmentQuantityNo(updated.getWeighmentQuantityNo());
        existing.setReturnableQuantityNo(updated.getReturnableQuantityNo());
        existing.setReturnableQuantityKg(updated.getReturnableQuantityKg());
        existing.setGenerateQr(updated.getGenerateQr());
        existing.setQrCode(updated.getQrCode());
        existing.setGeneratedQrImage(updated.getGeneratedQrImage());

        return SOSchedulePickListRepository.save(existing);
    }

    @Override
    public List<Map<String, Object>> getQrDetails(String mrNumber, String lineNumber, String itemDescription) {

        List<SOSchedulePickListEntity> entities;

        if (itemDescription != null && !itemDescription.trim().isEmpty()) {
            entities = soSchedulePickListRepository
                    .findByMrNumberAndLineNumberAndItemDescription(
                            mrNumber.trim(), lineNumber.trim(), itemDescription.trim());
        } else {
            entities = soSchedulePickListRepository
                    .findByMrNumberAndLineNumber(mrNumber.trim(), lineNumber.trim());
        }

        if (entities == null || entities.isEmpty()) {
            return List.of();
        }

        return entities.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("unitName", e.getUnitName());
            map.put("unitCode", e.getUnitCode());
            map.put("uom", e.getUom());
            map.put("materialType", e.getMaterialType());
            map.put("weightmentQtyKg", e.getWeighmentQuantityKg());
            return map;
        }).toList();
    }

    private StockTransferWarehouseEntity mapDtoToEntity(StockTransferWarehouseDto dto) {
        StockTransferWarehouseEntity e = new StockTransferWarehouseEntity();

        e.setId(dto.getId());
        e.setMrNumber(dto.getMrNumber());
        e.setLineNumber(dto.getLineNumber());

        e.setRequiredQuantity(dto.getRequiredQuantity());
        e.setRetrievalQuantityKg(dto.getRetrievalQuantityKg());
        e.setRetrievalQuantityNo(dto.getRetrievalQuantityNo());

        e.setWeighmentQuantityKg(dto.getWeighmentQuantityKg());
        e.setWeighmentQuantityNo(dto.getWeighmentQuantityNo());

        e.setReturnableQuantityKg(dto.getReturnableQuantityKg());
        e.setReturnableQuantityNo(dto.getReturnableQuantityNo());

        e.setItemDescription(dto.getItemDescription());
        e.setBrand(dto.getBrand());
        e.setGrade(dto.getGrade());
        e.setTemper(dto.getTemper());
        e.setUnit(dto.getUnit());
        e.setNextProcess(dto.getNextProcess());

        e.setGenerateQr(dto.getGenerateQr());
        e.setQrCode(dto.getQrCode());
        e.setGeneratedQrImage(dto.getGeneratedQrImage());

        // map retrieval entries and set bi-directional relation
        if (dto.getRetrievalQtyDto() != null) {
            for (StockTransferWHRetrievalQtyDto entryDto : dto.getRetrievalQtyDto()) {
                StockTransferWHRetrievalQtyEntry entry = new StockTransferWHRetrievalQtyEntry();
                entry.setRetrievalQuantityKg(entryDto.getRetrievalQuantityKg());
                entry.setRetrievalQuantityNo(entryDto.getRetrievalQuantityNo());

                e.addRetrievalEntry(entry);
            }
        }

        return e;
    }

    private StockTransferWarehouseDto mapEntityToDto(StockTransferWarehouseEntity e) {
        StockTransferWarehouseDto dto = new StockTransferWarehouseDto();
        dto.setId(e.getId());
        dto.setMrNumber(e.getMrNumber());
        dto.setLineNumber(e.getLineNumber());

        dto.setRequiredQuantity(e.getRequiredQuantity());
        dto.setRetrievalQuantityKg(e.getRetrievalQuantityKg());
        dto.setRetrievalQuantityNo(e.getRetrievalQuantityNo());

        dto.setWeighmentQuantityKg(e.getWeighmentQuantityKg());
        dto.setWeighmentQuantityNo(e.getWeighmentQuantityNo());

        dto.setReturnableQuantityKg(e.getReturnableQuantityKg());
        dto.setReturnableQuantityNo(e.getReturnableQuantityNo());

        dto.setItemDescription(e.getItemDescription());
        dto.setBrand(e.getBrand());
        dto.setGrade(e.getGrade());
        dto.setTemper(e.getTemper());
        dto.setUnit(e.getUnit());
        dto.setNextProcess(e.getNextProcess());

        dto.setGenerateQr(e.getGenerateQr());
        dto.setQrCode(e.getQrCode());
        dto.setGeneratedQrImage(e.getGeneratedQrImage());

        if (e.getRetrievalQtyEntries() != null) {
            List<StockTransferWHRetrievalQtyDto> entries = e.getRetrievalQtyEntries().stream().map(ent -> {
                StockTransferWHRetrievalQtyDto ed = new StockTransferWHRetrievalQtyDto();
                ed.setRetrievalQuantityKg(ent.getRetrievalQuantityKg());
                ed.setRetrievalQuantityNo(ent.getRetrievalQuantityNo());
                return ed;
            }).collect(Collectors.toList());
            dto.setRetrievalQtyDto(entries);
        }

        return dto;
    }

    private ReturnEntryEntity mapEntryDtoToEntity(ReturnEntryDto dto) {
        ReturnEntryEntity e = new ReturnEntryEntity();
        e.setSlNo(dto.getSlNo());
        e.setReturnStore(dto.getReturnStore());
        e.setWeighmentQuantityKg(dto.getWeighmentQuantityKg());
        e.setItemDescription(dto.getItemDescription());
        e.setBrand(dto.getBrand());
        e.setGrade(dto.getGrade());
        e.setTemper(dto.getTemper());
        e.setUnit(dto.getUnit());
        return e;
    }

    @Override
    public void deleteAllMaterialRequests() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🗑️  DELETE ALL MATERIAL REQUESTS    ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = headerRepo.count();
            System.out.println("📊 Total material requests before deletion: " + totalCount);

            headerRepo.deleteAll();

            long afterCount = headerRepo.count();
            System.out.println("✅ All material requests deleted successfully!");
            System.out.println("📊 Total material requests after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all material requests: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all material requests: " + e.getMessage());
        }
    }
}

package com.indona.invento.services.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.indona.invento.dao.*;
import com.indona.invento.dto.*;
import com.indona.invento.entities.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.indona.invento.services.StockTransferService;
import com.indona.invento.services.GrnLineItemService;
import com.indona.invento.services.AuditLogService;

import jakarta.transaction.Transactional;

@Service
public class StockTransferServiceImpl implements StockTransferService {

    @Autowired
    private StockTransferRepository stocktransferRepository;

    @Autowired
    private PicklistRepository pickRepo;

    @Autowired
    private StockinRepository stockinRepo;

    @Autowired
    private TransferSkuDetailsRepository skuTrnRepo;

    @Autowired
    private AdjustmentRepository adjRepo;

    @Autowired
    private SkusRepository skuRepo;

    @Autowired
    private GrnLineItemService grnLineItemService;

    @Autowired
    private GRNRepository grnRepository;

    @Autowired
    private GrnLineItemRepository grnLineItemRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Override
    public List<StockTransferEntity> getAllStockTransfers(String search, Pageable pageable) {
        List<StockTransferEntity> transfers = stocktransferRepository.findByDeleteFlagOrderByDateTimeDesc(0, search, pageable);
        populateMissingFieldsFromGRN(transfers);
        return transfers;
    }

    @Override
    public List<StockTransferWithLineItemsDto> getAllStockTransfersAll() {
        List<StockTransferEntity> transfers = stocktransferRepository.findByDeleteFlagOrderByDateTimeDesc(0);

        return transfers.stream().map(transfer -> {
                    // Fetch line items for this GRN
                    List<GrnLineItemDto> lineItems = grnLineItemRepository.findByGrnNumber(transfer.getGrnRefNumber())
                            .stream()
                            .map(this::mapToDto)
                            .collect(Collectors.toList());

                    // ✅ Extract unitId, userId, unitCode from first item (if exists)
                    String unitId = null;
                    String userId = null;
                    String unitCode = null;
                    if (!lineItems.isEmpty()) {
                        GrnLineItemDto firstItem = lineItems.get(0);
                        unitId = firstItem.getUnitId();
                        userId = firstItem.getUserId();
                        unitCode = transfer.getUnit(); // or firstItem.getUnitCode() if available in entity
                    }

                    // ✅ Group by itemDescription + poNumber
                    Map<String, List<GrnLineItemDto>> grouped = lineItems.stream()
                            .collect(Collectors.groupingBy(item -> {
                                if (item.getPoNumber() == null || item.getPoNumber().isBlank()) {
                                    return item.getItemDescription();
                                } else {
                                    return item.getItemDescription() + "|" + item.getPoNumber();
                                }
                            }));

                    // ✅ Build merged lines
                    List<MergedLineDto> mergedLines = grouped.entrySet().stream().map(entry -> {
                        List<GrnLineItemDto> groupItems = entry.getValue();

                        BigDecimal totalKg = groupItems.stream()
                                .map(i -> i.getWeightmentQuantityKg() != null ? i.getWeightmentQuantityKg() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                        Integer totalNo = groupItems.stream()
                                .map(i -> i.getWeightmentQuantityNo() != null ? i.getWeightmentQuantityNo() : 0)
                                .reduce(0, Integer::sum);

                        GrnLineItemDto first = groupItems.get(0);

                        return MergedLineDto.builder()
                                .itemDescription(first.getItemDescription())
                                .poNumber(first.getPoNumber())
                                .slNo(first.getSlNo())
                                .productCategory(first.getProductCategory())
                                .sectionNumber(first.getSectionNumber())
                                .brand(first.getBrand())
                                .grade(first.getGrade())
                                .temper(first.getTemper())
                                .totalQuantityKg(totalKg)
                                .totalQuantityNo(totalNo)
                                .numberOfBundles(groupItems.size())
                                .mergedItems(groupItems)
                                .build();
                    }).collect(Collectors.toList());

                    // ✅ Calculate overall totals
                    BigDecimal totalKg = lineItems.stream()
                            .map(item -> item.getWeightmentQuantityKg() != null ? item.getWeightmentQuantityKg() : BigDecimal.ZERO)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    Integer totalNo = lineItems.stream()
                            .map(item -> item.getWeightmentQuantityNo() != null ? item.getWeightmentQuantityNo() : 0)
                            .reduce(0, Integer::sum);

                    Integer bundleCount = lineItems.size();


                    return StockTransferWithLineItemsDto.builder()
                            .id(transfer.getId())
                            .transferNumber(transfer.getTransferNumber())
                            .transferType(transfer.getTransferType())
                            .transferQuantity(transfer.getTransferQuantity())
                            .grnRefNumber(transfer.getGrnRefNumber())
                            .invoiceNumber(transfer.getInvoiceNumber())
                            .unit(transfer.getUnit())
                            .unitId(unitId)        // ✅ new field
                            .userId(userId)        // ✅ new field
                            .unitCode(unitCode)    // ✅ new field
                            .itemDescription(transfer.getItemDescription())
                            .sectionNumber(transfer.getSectionNumber())
                            .productCategory(transfer.getProductCategory())
                            .brand(transfer.getBrand())
                            .grade(transfer.getGrade())
                            .temper(transfer.getTemper())
                            .grnQuantityNetWeight(transfer.getGrnQuantityNetWeight())
                            .grnQuantityNetWeightUom(transfer.getGrnQuantityNetWeightUom())
                            .grnQuantityNo(transfer.getGrnQuantityNo())
                            .grnQuantityNoUom(transfer.getGrnQuantityNoUom())
                            .currentStore(transfer.getCurrentStore())
                            .storageArea(transfer.getStorageArea())
                            .rackColumnBinNumber(transfer.getRackColumnBinNumber())
                            .lineItems(lineItems)
                            .mergedLines(mergedLines)
                            .totalQuantityKg(totalKg)
                            .totalQuantityNo(totalNo)
                            .numberOfBundles(bundleCount)
                            .build();
                })
                .sorted(Comparator.comparing(StockTransferWithLineItemsDto::getTransferNumber, Comparator.nullsLast(String::compareTo))
                        .thenComparing(StockTransferWithLineItemsDto::getGrnRefNumber, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    @Override
    public List<StockTransferEntity> getStockTransferByStore(Long id, String search, Pageable pageable) {
        List<StockTransferEntity> transfers = stocktransferRepository.findByToStoreAndDeleteFlagOrderByDateTimeDesc(id,0, search, pageable);
        populateMissingFieldsFromGRN(transfers);
        return transfers;
    }

    @Override
    public List<StockTransferEntity> getStockTransferByWarehouse(Long id, String search, Pageable pageable) {
        List<StockTransferEntity> transfers = stocktransferRepository.findByFromStoreAndDeleteFlagOrderByDateTimeDesc(id,0, search, pageable);
        populateMissingFieldsFromGRN(transfers);
        return transfers;
    }

    @Override
    public List<StockTransferEntity> getStockTransferByFromAndTo(Long id, String search, Pageable pageable) {
        List<StockTransferEntity> transfers = stocktransferRepository.findByFromStoreOrToStoreAndDeleteFlagOrderByDateTimeDesc(id,id,0, search, pageable);
        populateMissingFieldsFromGRN(transfers);
        return transfers;
    }

    @Override
    public TransferSkuDto getStockTransferById(Long id) {
        if(id != null) {
            StockTransferEntity st = stocktransferRepository.findById(id).get();
            TransferSkuDto re = new TransferSkuDto();
            re.setFromStore(st.getFromStore());
            re.setToStore(st.getToStore());
            re.setTransferNumber(st.getTransferNumber());
            re.setTransferStage(st.getTransferStage());
            re.setTransferStage(re.getTransferStage());
            re.setTransferType(st.getTransferType());

            // Map GRN fields
            re.setGrnRefNumber(st.getGrnRefNumber());
            re.setInvoiceNumber(st.getInvoiceNumber());
            re.setUnit(st.getUnit());
            re.setItemDescription(st.getItemDescription());
            re.setSectionNumber(st.getSectionNumber());
            re.setProductCategory(st.getProductCategory());
            re.setBrand(st.getBrand());
            re.setGrade(st.getGrade());
            re.setTemper(st.getTemper());

            // Map GRN Quantities
            re.setGrnQuantityNetWeight(st.getGrnQuantityNetWeight());
            re.setGrnQuantityNetWeightUom(st.getGrnQuantityNetWeightUom());
            re.setGrnQuantityNo(st.getGrnQuantityNo());
            re.setGrnQuantityNoUom(st.getGrnQuantityNoUom());

            // Map Added Quantities
            re.setAddedQuantityNetWeight(st.getAddedQuantityNetWeight());
            re.setAddedQuantityNetWeightUom(st.getAddedQuantityNetWeightUom());
            re.setAddedQuantityNo(st.getAddedQuantityNo());
            re.setAddedQuantityNoUom(st.getAddedQuantityNoUom());
            re.setNumberOfBundles(st.getNumberOfBundles());

            // Map Warehouse Storage Fields
            re.setCurrentStore(st.getCurrentStore());
            re.setRecipientStore(st.getRecipientStore());
            re.setStorageArea(st.getStorageArea());
            re.setRackColumnBinNumber(st.getRackColumnBinNumber());

            List<StockTransferSkuEntity> skuEntities = skuTrnRepo.findAllByTransferNumber(st.getTransferNumber());
            List<TransferSkuDetailsDto> skuList = new ArrayList<>();

            for(StockTransferSkuEntity en:skuEntities) {
                TransferSkuDetailsDto rec = new TransferSkuDetailsDto();
                rec.setTransferQuantity(en.getTransferQuantity());
                rec.setTransferSkuCode(en.getTransferSkuCode());
                rec.setTransferSkuName(en.getTransferSkuName());
                rec.setDispatchQuantity(en.getDispatchQuantity());
                rec.setRecievedQuantity(en.getRecievedQuantity());
                skuList.add(rec);
            }

            re.setTransferSkuList(skuList);
            return re;
        }
        return null;
    }

    @Override
    public TransferSkuDto createStockTransfer(TransferSkuDto department) {
        StockTransferEntity st = new StockTransferEntity();
        // Get the list of TransferSKU objects
        List<TransferSkuDetailsDto> transferSkuList = department.getTransferSkuList();

        // Sum up the quantities of all SKUs in the list, converting String to Long
        long totalQuantity = transferSkuList.stream()
                .map(TransferSkuDetailsDto::getTransferQuantity)
                .mapToLong(Long::parseLong)
                .sum();

        st.setFromStore(department.getFromStore());
        st.setToStore(department.getToStore());
        st.setTransferNumber(department.getTransferNumber());
        st.setTransferQuantity(String.valueOf(totalQuantity));
        st.setTransferStage(department.getTransferStage());
        st.setTransferType(department.getTransferType());

        // Set GRN fields
        st.setGrnRefNumber(department.getGrnRefNumber());
        st.setInvoiceNumber(department.getInvoiceNumber());
        st.setUnit(department.getUnit());
        st.setItemDescription(department.getItemDescription());
        st.setSectionNumber(department.getSectionNumber());
        st.setProductCategory(department.getProductCategory());
        st.setBrand(department.getBrand());
        st.setGrade(department.getGrade());
        st.setTemper(department.getTemper());

        // Set GRN Quantities
        st.setGrnQuantityNetWeight(department.getGrnQuantityNetWeight());
        st.setGrnQuantityNetWeightUom(department.getGrnQuantityNetWeightUom());
        st.setGrnQuantityNo(department.getGrnQuantityNo());
        st.setGrnQuantityNoUom(department.getGrnQuantityNoUom());

        // Set Added Quantities
        st.setAddedQuantityNetWeight(department.getAddedQuantityNetWeight());
        st.setAddedQuantityNetWeightUom(department.getAddedQuantityNetWeightUom());
        st.setAddedQuantityNo(department.getAddedQuantityNo());
        st.setAddedQuantityNoUom(department.getAddedQuantityNoUom());
        st.setNumberOfBundles(department.getNumberOfBundles());

        // Set Warehouse Storage Fields
        st.setCurrentStore(department.getCurrentStore());
        st.setRecipientStore(department.getRecipientStore());
        st.setStorageArea(department.getStorageArea());
        st.setRackColumnBinNumber(department.getRackColumnBinNumber());

        if (stocktransferRepository.existsByTransferNumber(department.getTransferNumber())) {
            return null;
        }
        if(stocktransferRepository.save(st).getId() != null) {
            for(TransferSkuDetailsDto item: transferSkuList) {
                StockTransferSkuEntity sku = new StockTransferSkuEntity();
                sku.setTransferNumber(department.getTransferNumber());
                sku.setTransferQuantity(item.getTransferQuantity());
                sku.setTransferSkuCode(item.getTransferSkuCode());
                sku.setTransferSkuName(item.getTransferSkuName());
                skuTrnRepo.save(sku);
            }
        }
        return department;
    }

    @Override
    @Transactional
    public TransferSkuDto updateStockTransfer(Long id, TransferSkuDto department) {
        Optional<StockTransferEntity> optionalSt = stocktransferRepository.findById(id);
        if (optionalSt.isPresent()) {
            StockTransferEntity st = optionalSt.get();

            // Update fields of existing StockTransferEntity
            st.setFromStore(department.getFromStore());
            st.setToStore(department.getToStore());
            st.setTransferNumber(department.getTransferNumber());
            st.setTransferStage(department.getTransferStage());
            st.setRecievingDate(department.getRecievingDate());
            // Calculate total transfer quantity
            long totalQuantity = department.getTransferSkuList().stream()
                    .mapToLong(item -> Long.parseLong(item.getTransferQuantity()))
                    .sum();
            st.setTransferQuantity(String.valueOf(totalQuantity));

            // Save updated StockTransferEntity
            stocktransferRepository.save(st);

            Iterator<StockTransferSkuEntity> iterator = skuTrnRepo.findAllByTransferNumber(st.getTransferNumber()).iterator();

            // Iterate through the transferSkuList and remove elements not present in skuList
            List<TransferSkuDetailsDto> skuList = department.getTransferSkuList();
            while (iterator.hasNext()) {
                StockTransferSkuEntity item = iterator.next();
                boolean found = false;

                for (TransferSkuDetailsDto sku : skuList) {
                    // Assuming a method to compare TransferSkuDetailsDto with StockTransferSkuEntity
                    if (item.getTransferSkuCode().equals(sku.getTransferSkuCode())) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    skuTrnRepo.deleteByTransferNumberAndTransferSkuCode(st.getTransferNumber(), item.getTransferSkuCode());
                    iterator.remove();
                }
            }
            // Process TransferSkuDetailsDto list
            for (TransferSkuDetailsDto item : department.getTransferSkuList()) {
                SkuEntity skuEntity = skuRepo.findBySkuCode(item.getTransferSkuCode());
                StockTransferSkuEntity sku = skuTrnRepo.findByTransferNumberAndTransferSkuCode(st.getTransferNumber(), item.getTransferSkuCode());
                if (sku == null) {
                    sku = new StockTransferSkuEntity();
                    sku.setTransferNumber(department.getTransferNumber());
                    sku.setTransferSkuCode(item.getTransferSkuCode());
                    sku.setTransferSkuName(item.getTransferSkuName());
                }

                // Update or save StockTransferSkuEntity
                sku.setRecievedQuantity(item.getRecievedQuantity());
                sku.setDispatchQuantity(item.getDispatchQuantity());
                skuTrnRepo.save(sku);

                if(item.getDispatchQuantity() != null) {
                    // Compare current and new quantities
                    if (!item.getDispatchQuantity().equals(item.getRecievedQuantity())) {
                        Long diff = Long.valueOf(item.getDispatchQuantity()) - Long.valueOf(item.getRecievedQuantity());
                        boolean existingAdj = adjRepo.existsByRefNo(department.getTransferNumber());
                        if (diff != 0) {

                            if(!existingAdj) {
                                AdjustmentEntity adj = new AdjustmentEntity();
                                adj.setCreatedBy("internal");
                                adj.setRefNo(department.getTransferNumber());
                                adj.setSkuId(skuEntity.getId());
                                adj.setSkuName(skuEntity.getSkuName());
                                adj.setQuantityDiff(diff);
                                if(diff>0) {
                                    adj.setType("shortage");
                                } else {
                                    adj.setType("excess");
                                }

                                adj.setStatus(-1L);
                                adj.setStoreId(department.getToStore());

                                // Adjust related entities if quantity changes
                                List<PicklistEntity> pickList = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(Long.valueOf(st.getTransferNumber()), Long.valueOf(st.getFromStore()), "out");
                                for (PicklistEntity pick : pickList) {
                                    if(pick.getSkuId().equals(adj.getSkuId()) && pick.getStockInId() != null) {
                                        adj.setStockId(pick.getStockInId());
                                    }
                                }

                                adjRepo.save(adj);
                            }
                        }

                    }
                }

            }

            // Check if transfer stage is "dispatched" and perform additional actions if needed
            if (department.getTransferStage().equalsIgnoreCase("dispatched")) {
                // Perform whatever action you need for dispatched stage
                updateStockTransfer(department);

                StockTransferEntity stg = stocktransferRepository.findById(id).get();
                stg.setDispatchDate(new Date());
                stocktransferRepository.save(stg);

            }

            return department;
        } else {
            return null; // Or throw an exception indicating department not found
        }
    }


    @Override
    public void deleteStockTransfer(Long id, String remark) {
        StockTransferEntity st = stocktransferRepository.findById(id).get();
        if(stocktransferRepository.existsById(id)) {
            List<PicklistEntity> pickList = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(Long.valueOf(st.getTransferNumber()), Long.valueOf(st.getToStore()), "out");
            if(pickList.size() > 0) {
                for(PicklistEntity entity : pickList) {
                    if(entity.getStockInId() != null) {
                        SkuEntity sku = skuRepo.findById(entity.getSkuId()).get();
                        StockTransferSkuEntity skuTrn = skuTrnRepo.findByTransferNumberAndTransferSkuCode(String.valueOf(st.getTransferNumber()), sku.getSkuCode());

                        if(stockinRepo.existsById(entity.getStockInId())) {
                            StockinEntity stockin = stockinRepo.findById(entity.getStockInId()).get();
                            if(st.getTransferStage().equalsIgnoreCase("requested") || st.getTransferStage().equalsIgnoreCase("picked") || st.getTransferStage().equalsIgnoreCase("packed")) {
                                stockin.setSkuHold(stockin.getSkuHold()-Long.valueOf(skuTrn.getTransferQuantity()));
                            } else {
                                stockin.setSkuQuantity(stockin.getSkuQuantity()+Long.valueOf(skuTrn.getRecievedQuantity()));
                            }

                            stockinRepo.save(stockin);
                        }
                    }
                }
            }
            st.setDeleteFlag(1);
            st.setDeleteRemark(remark);
            stocktransferRepository.save(st);
        }

    }

    private void updateStockTransfer(TransferSkuDto req) {
        List<PicklistEntity> pickList = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(Long.valueOf(req.getTransferNumber()), Long.valueOf(req.getFromStore()), "out");
        if(pickList.size() > 0) {
            for(PicklistEntity entity : pickList) {
                if(entity.getBinId() != null && entity.getStockInId() != null && entity.getQuantity() != 0L) {
                    SkuEntity sku = skuRepo.findById(entity.getSkuId()).get();
                    StockTransferSkuEntity skuTrn = skuTrnRepo
                            .findByTransferNumberAndTransferSkuCode(req.getTransferNumber(), sku.getSkuCode());
                    StockinEntity stockin = stockinRepo.getOne(entity.getStockInId());
                    if(Long.valueOf(skuTrn.getDispatchQuantity()) > Long.valueOf(skuTrn.getHoldQuantity())) {
                        stockin.setSkuHold(stockin.getSkuHold()-Long.valueOf(entity.getQuantity()));
                        stockin.setSkuQuantity(stockin.getSkuQuantity()-Long.valueOf(entity.getQuantity()));
                    } else {
                        stockin.setSkuHold(stockin.getSkuHold()-Long.valueOf(entity.getQuantity()));
                        stockin.setSkuQuantity(stockin.getSkuQuantity()-Long.valueOf(skuTrn.getDispatchQuantity()));
                    }

                    stockinRepo.save(stockin);
                }
            }
        }
    }

    private void updateAdjustmentTransfer(TransferSkuDto req) {
        List<PicklistEntity> pickList = pickRepo.findByRefNoAndStoreIdAndTypeOrderByDateTime(Long.valueOf(req.getTransferNumber()), Long.valueOf(req.getFromStore()), "out");
        if(pickList.size() > 0) {
            for(PicklistEntity entity : pickList) {
                StockinEntity stockin = stockinRepo.getOne(entity.getStockInId());
                stockin.setSkuHold(stockin.getSkuHold()-entity.getQuantity());
                stockin.setSkuQuantity(stockin.getSkuQuantity()-entity.getQuantity());
                stockinRepo.save(stockin);
            }
        }
    }
    @Override
    public List<StockTransferSummaryDto> getStockTransferSummaryDashboard() {
        List<StockTransferEntity> allTransfers = stocktransferRepository.findByDeleteFlagOrderByDateTimeDesc(0);
        List<StockTransferSummaryDto> summaries = new ArrayList<>();

        for (StockTransferEntity transfer : allTransfers) {
            // Fetch bundles for this transfer
            List<GrnLineItemDto> bundles = grnLineItemService.getLineItemsByTransferNumber(transfer.getTransferNumber());

            StockTransferSummaryDto summary = StockTransferSummaryDto.builder()
                    // Stock Transfer Basic Info
                    .id(transfer.getId())
                    .transferNumber(transfer.getTransferNumber())
                    .transferType(transfer.getTransferType())
                    .transferStage(transfer.getTransferStage())
                    .status(transfer.getStatus())
                    .dateTime(transfer.getDateTime())
                    .dispatchDate(transfer.getDispatchDate())
                    .recievingDate(transfer.getRecievingDate())
                    .createdBy(transfer.getCreatedBy())
                    // GRN Reference Data
                    .grnRefNumber(transfer.getGrnRefNumber())
                    .invoiceNumber(transfer.getInvoiceNumber())
                    .unit(transfer.getUnit())
                    .itemDescription(transfer.getItemDescription())
                    .sectionNumber(transfer.getSectionNumber())
                    .productCategory(transfer.getProductCategory())
                    .brand(transfer.getBrand())
                    .grade(transfer.getGrade())
                    .temper(transfer.getTemper())
                    // GRN Quantities
                    .grnQuantityNetWeight(transfer.getGrnQuantityNetWeight())
                    .grnQuantityNetWeightUom(transfer.getGrnQuantityNetWeightUom())
                    .grnQuantityNo(transfer.getGrnQuantityNo())
                    .grnQuantityNoUom(transfer.getGrnQuantityNoUom())
                    // Added Quantities
                    .addedQuantityNetWeight(transfer.getAddedQuantityNetWeight())
                    .addedQuantityNetWeightUom(transfer.getAddedQuantityNetWeightUom())
                    .addedQuantityNo(transfer.getAddedQuantityNo())
                    .addedQuantityNoUom(transfer.getAddedQuantityNoUom())
                    .numberOfBundles(transfer.getNumberOfBundles())
                    // Warehouse Storage Fields
                    .currentStore(transfer.getCurrentStore())
                    .recipientStore(transfer.getRecipientStore())
                    .storageArea(transfer.getStorageArea())
                    .rackColumnBinNumber(transfer.getRackColumnBinNumber())
                    // Store/Warehouse Info
                    .fromStore(transfer.getFromStore())
                    .toStore(transfer.getToStore())
                    // Bundles for this Stock Transfer (for "View Bundles" button)
                    .bundles(bundles)
                    .build();

            summaries.add(summary);
        }

        return summaries;
    }

    @Override
    @Transactional
    public SaveStockTransferResponseDto saveStockTransfer(SaveStockTransferRequestDto request) {
        // Fetch Stock Transfer
        StockTransferEntity stockTransfer = stocktransferRepository.findById(request.getStockTransferId())
                .orElseThrow(() -> new RuntimeException("Stock Transfer not found with ID: " + request.getStockTransferId()));

        // Update transfer stage to COMPLETED
        stockTransfer.setTransferStage(request.getTransferStage() != null ? request.getTransferStage() : "COMPLETED");
        StockTransferEntity savedTransfer = stocktransferRepository.save(stockTransfer);

        // Mark GRN as COMPLETED
        GRNEntity grn = null;
        if (request.getGrnId() != null) {
            grn = grnRepository.findById(request.getGrnId())
                    .orElseThrow(() -> new RuntimeException("GRN not found with ID: " + request.getGrnId()));
            grn.setBinStatus("COMPLETED");
            grnRepository.save(grn);
        }

        // Build and return response
        return SaveStockTransferResponseDto.builder()
                .stockTransferId(savedTransfer.getId())
                .transferNumber(savedTransfer.getTransferNumber())
                .transferStage(savedTransfer.getTransferStage())
                .grnId(grn != null ? grn.getId() : null)
                .grnRefNumber(grn != null ? grn.getGrnRefNumber() : null)
                .grnBinStatus(grn != null ? grn.getBinStatus() : null)
                .updatedAt(new Date())
                .success(true)
                .message("Stock Transfer saved successfully and GRN marked as COMPLETED")
                .build();
    }

    /**
     * Helper method to populate missing fields from GRN for stock transfers
     */
    private void populateMissingFieldsFromGRN(List<StockTransferEntity> transfers) {
        for (StockTransferEntity transfer : transfers) {
            if (transfer.getGrnRefNumber() != null && !transfer.getGrnRefNumber().isEmpty()) {
                Optional<GRNEntity> grnOpt = grnRepository.findByGrnRefNumber(transfer.getGrnRefNumber());
                if (grnOpt.isPresent()) {
                    GRNEntity grn = grnOpt.get();

                    // Populate missing GRN fields if they are null
                    if (transfer.getInvoiceNumber() == null) {
                        transfer.setInvoiceNumber(grn.getInvoiceNumber());
                    }
                    if (transfer.getUnit() == null) {
                        transfer.setUnit(grn.getUnit());
                    }

                    // Get first GRN item for item details and quantities
                    if (grn.getGrnItems() != null && !grn.getGrnItems().isEmpty()) {
                        GRNItemEntity firstItem = grn.getGrnItems().get(0);

                        if (transfer.getItemDescription() == null) {
                            transfer.setItemDescription(firstItem.getItemDescription());
                        }
                        if (transfer.getSectionNumber() == null) {
                            transfer.setSectionNumber(firstItem.getSectionNumber());
                        }
                        if (transfer.getProductCategory() == null) {
                            transfer.setProductCategory(firstItem.getItemDescription());
                        }
                        if (transfer.getGrade() == null) {
                            transfer.setGrade(firstItem.getGrade());
                        }
                        if (transfer.getTemper() == null) {
                            transfer.setTemper(firstItem.getTemper());
                        }

                        // Populate GRN Quantities if null
                        if (transfer.getGrnQuantityNetWeight() == null) {
                            transfer.setGrnQuantityNetWeight(firstItem.getReceivedNetWeight());
                        }
                        if (transfer.getGrnQuantityNetWeightUom() == null) {
                            transfer.setGrnQuantityNetWeightUom(firstItem.getUom());
                        }
                        if (transfer.getGrnQuantityNo() == null) {
                            transfer.setGrnQuantityNo(firstItem.getReceivedNo());
                        }
                        if (transfer.getGrnQuantityNoUom() == null) {
                            transfer.setGrnQuantityNoUom("NOS");
                        }

                        // Set transferQuantity based on GRN quantity
                        if (transfer.getTransferQuantity() == null && firstItem.getReceivedNetWeight() != null) {
                            transfer.setTransferQuantity(String.valueOf(firstItem.getReceivedNetWeight().intValue()));
                        }
                    }
                }
            }
        }
    }

    @Override
    public void deleteAllStockTransfers() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🗑️  DELETE ALL STOCK TRANSFERS      ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = stocktransferRepository.count();
            System.out.println("📊 Total stock transfers before deletion: " + totalCount);

            stocktransferRepository.deleteAll();

            long afterCount = stocktransferRepository.count();
            System.out.println("✅ All stock transfers deleted successfully!");
            System.out.println("📊 Total stock transfers after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all stock transfers: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all stock transfers: " + e.getMessage());
        }
    }

    private GrnLineItemDto mapToDto(GrnLineItemEntity entity) {
        return GrnLineItemDto.builder()
                .id(entity.getId())
                .grnNumber(entity.getGrnNumber())
                .grnId(entity.getGrnId())
                .unitId(entity.getUnitId())
                .userId(entity.getUserId())
                .stockTransferId(entity.getStockTransferId())
                .transferNumber(entity.getTransferNumber())
                .inputType(entity.getInputType())
                .slNo(entity.getSlNo())
                .itemDescription(entity.getItemDescription())
                .productCategory(entity.getProductCategory())
                .sectionNumber(entity.getSectionNumber())
                .brand(entity.getBrand())
                .grade(entity.getGrade())
                .temper(entity.getTemper())
                .weighment(entity.getWeighment())
                .weightmentQuantityKg(entity.getWeightmentQuantityKg())
                .uomNetWeight(entity.getUomNetWeight())
                .weightmentQuantityNo(entity.getWeightmentQuantityNo())
                .uomNo(entity.getUomNo())
                .materialAcceptance(entity.getMaterialAcceptance())
                .currentStore(entity.getCurrentStore())
                .recipientStore(entity.getRecipientStore())
                .storageArea(entity.getStorageArea())
                .rackColumnBinNumber(entity.getRackColumnBinNumber())
                .qrCode(entity.getQrCode())
                .qrCodeImageUrl(entity.getQrCodeImageUrl())
                .qrGenerated(entity.getQrGenerated())
                .status(entity.getStatus())
                .poNumber(entity.getPoNumber())
                .createdBy(entity.getCreatedBy())
                .createdDate(entity.getCreatedDate())
                .updatedBy(entity.getUpdatedBy())
                .updatedDate(entity.getUpdatedDate())
                .dimension(entity.getDimension())
                .build();
    }

}

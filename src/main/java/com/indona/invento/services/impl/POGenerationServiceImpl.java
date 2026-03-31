package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.POGenerationDTO;
import com.indona.invento.dto.POGenerationResponseDTO;
import com.indona.invento.entities.*;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.POGenerationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class POGenerationServiceImpl implements POGenerationService {

    private final POGenerationRepository repository;

    private final PORequestRepository poRequestRepository;

    private final POManagementApprovalRepository approvalRepository;

    private final GRNRepository grnRepository;

    private final GRNItemRepository grnItemRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    @Override
    public POGenerationEntity savePOGeneration(POGenerationDTO dto) {
        // ✅ Step 1: Prepare PO entity
        POGenerationEntity po = POGenerationEntity.builder()
                .supplierCode(dto.getSupplierCode())
                .supplierName(dto.getSupplierName())
                .poNumber(generateUniquePoNumber())
                .poGeneratedBy(dto.getPoGeneratedBy())
                .billingAddress(dto.getBillingAddress())
                .shippingAddress(dto.getShippingAddress())
                .poStatus("PO CREATED")
                .remarks(dto.getRemarks() != null ? dto.getRemarks() : "")
                .dispatchThrough(dto.getDispatchThrough())
                .otherReference(dto.getOtherReference())
                .pdflink(dto.getPdflink())
                .termsOfDelivery(dto.getTermsOfDelivery())
                .unit(dto.getUnit())
                .poValidity(dto.getPoValidity())
                .build();

        // ✅ Step 2: Prepare items + collect PR numbers in one pass
        List<String> prNumbersToDelete = new ArrayList<>();
        List<POGenerationItemEntity> items = new ArrayList<>();

        for (var i : dto.getItems()) {
            if (i.getPrNumber() != null && !i.getPrNumber().isBlank()) {
                prNumbersToDelete.add(i.getPrNumber());
            }

            items.add(POGenerationItemEntity.builder()
                    .prNumber(i.getPrNumber())
                    .prCreatedBy(i.getPrCreatedBy())
                    .unit(i.getUnit())
                    .deliveryAddress(i.getDeliveryAddress())
                    .sectionNo(i.getSectionNo())
                    .itemDescription(i.getItemDescription())
                    .productCategory(i.getProductCategory())
                    .brand(i.getBrand())
                    .grade(i.getGrade())
                    .temper(i.getTemper())
                    .requiredQuantity(i.getRequiredQuantity())
                    .uom(i.getUom())
                    .rmReceiptStatus("PENDING")
                    .prTypeAndReasonVerifiaction(i.getPrTypeAndReasonverification())
                    .poGeneration(po)
                    .build());
        }

        po.setItems(items);

        // ✅ Step 3: Save PO first
        POGenerationEntity savedPO = repository.save(po);

        // ✅ Step 4: Save PO Management Approval summary + items
        POManagementApprovalEntity approval = POManagementApprovalEntity.builder()
                .poNumber(savedPO.getPoNumber())
                .unit(items.isEmpty() ? null : items.get(0).getUnit())
                .orderDate(LocalDate.now())
                .productCategory(items.isEmpty() ? null : items.get(0).getProductCategory())
                .quantity(items.stream()
                        .mapToDouble(POGenerationItemEntity::getRequiredQuantity)
                        .sum())
                .supplier(savedPO.getSupplierName())
                .supplierLeadTime(21) // default or from supplier master
                .supplierMOQ(500)     // default or from supplier master
                .poGeneratedBy(savedPO.getPoGeneratedBy())
                .status("PO CREATED")
                .billingAddress(savedPO.getBillingAddress())
                .unit(dto.getUnit())

                .shippingAddress(savedPO.getShippingAddress())
                .remarks("-")
                .build();

        List<POManagementApprovalItemEntity> approvalItems = items.stream().map(i ->
                POManagementApprovalItemEntity.builder()
                        .prNumber(i.getPrNumber())
                        .prCreatedBy(i.getPrCreatedBy())
                        .unit(i.getUnit())
                        .deliveryAddress(i.getDeliveryAddress())
                        .sectionNo(i.getSectionNo())
                        .itemDescription(i.getItemDescription())
                        .productCategory(i.getProductCategory())
                        .brand(i.getBrand())
                        .grade(i.getGrade())
                        .temper(i.getTemper())
                        .requiredQuantity(i.getRequiredQuantity())
                        .uom(i.getUom())
                        .prTypeAndReasonVerifiaction(i.getPrTypeAndReasonVerifiaction())
                        .approval(approval)
                        .build()
        ).toList();

        approval.setItems(approvalItems);
        approvalRepository.save(approval);

        if (!prNumbersToDelete.isEmpty()) {
            List<PORequestEntity> prList = poRequestRepository.findByPrNumberIn(prNumbersToDelete);
            for (PORequestEntity pr : prList) {
                pr.setStatus("PO GENERATED");
            }
            poRequestRepository.saveAll(prList);
        }

        // Audit log for PO creation
        auditLogService.logAction("CREATE", "PURCHASE_ORDER", "POGeneration",
                savedPO.getId(), savedPO.getPoNumber(), null, savedPO.getPoStatus(),
                "PO " + savedPO.getPoNumber() + " created for supplier " + savedPO.getSupplierName(),
                savedPO.getPoGeneratedBy() != null ? savedPO.getPoGeneratedBy() : "SYSTEM",
                savedPO.getUnit());

        return savedPO;
    }

    @Override
    public List<POGenerationEntity> getPOsBySupplier(String supplierCode, String supplierName) {
        return repository.findBySupplierCodeOrSupplierName(supplierCode, supplierName);
    }

    @Override
    public POGenerationEntity getPOById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("POGeneration not found with ID: " + id));
    }

    private String generateUniquePoNumber() {
        String prefix = "MEPO";
        String datePart = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMM"));
        int sequence = 1;
        String poNumber;

        do {
            String sequencePart = String.format("%03d", sequence); // 001, 002, ...
            poNumber = prefix + datePart + sequencePart;
            sequence++;
        } while (repository.existsByPoNumber(poNumber));

        return poNumber;
    }

    @Override
    public POGenerationEntity getPOByPoNumber(String poNumber) {
        return repository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found with number: " + poNumber));
    }

    @Override
    public Page<POGenerationResponseDTO> getAllPOs(int page, int size) {
        Page<POGenerationEntity> pos = repository.findAll(PageRequest.of(page, size));

        return pos.map(po -> {
            double totalQty = po.getItems().stream()
                    .mapToDouble(item -> item.getRequiredQuantity() != null ? item.getRequiredQuantity() : 0.00)
                    .sum();

            // ---------- Fetch All GRNs for this PO ----------
            List<GRNEntity> grns = grnRepository.findByPoNumber(po.getPoNumber());

            // ----------- ITEM-WISE RECEIVED NET WEIGHT -----------
            List<POGenerationItemEntity> enrichedItems = po.getItems().stream().map(poItem -> {

                double receivedNet = 0.0;

                for (GRNEntity grn : grns) {
                    if (grn.getGrnItems() == null) continue;

                    for (GRNItemEntity grnItem : grn.getGrnItems()) {

                        if (grnItem.getItemDescription() != null &&
                                grnItem.getItemDescription().equalsIgnoreCase(poItem.getItemDescription())) {

                            if (grnItem.getReceivedNetWeight() != null) {
                                receivedNet += grnItem.getReceivedNetWeight();
                            }
                        }
                    }
                }

                // set only the received net weight
                // ---------- ITEM LEVEL RECEIPT STATUS ----------
                double requiredQty = poItem.getRequiredQuantity() != null ? poItem.getRequiredQuantity() : 0;
                double tolerance = requiredQty * 0.10;
                double minAllowed = requiredQty - tolerance;
                double maxAllowed = requiredQty + tolerance;

                String itemStatus;

                if (receivedNet == 0) {
                    itemStatus = "Not Received";
                }
                else if (receivedNet >= minAllowed && receivedNet <= maxAllowed) {
                    if (receivedNet == requiredQty) {
                        itemStatus = "Received";
                    } else {
                        itemStatus = "Partially Received (Within Tolerance)";
                    }
                }
                else if (receivedNet < minAllowed) {
                    itemStatus = "Partially Received";
                }
                else { // receivedNet > maxAllowed
                    itemStatus = "Received in Excess";
                }

                poItem.setRmReceiptStatus(itemStatus);    // store in DB
                poItem.setReceivedNetWeight(receivedNet);

                return poItem;
            }).toList();

            // ---------- SUMMARIES ----------
            double totalReceivedNet = enrichedItems.stream()
                    .mapToDouble(i -> i.getReceivedNetWeight() != null ? i.getReceivedNetWeight() : 0.0)
                    .sum();

            // ---------- PO-LEVEL RECEIPT STATUS ----------
            double poTolerance = totalQty * 0.10;
            double poMinAllowed = totalQty - poTolerance;
            double poMaxAllowed = totalQty + poTolerance;

            String poStatus;

            if (totalReceivedNet == 0) {
                poStatus = "Not Received";
            }
            else if (totalReceivedNet >= poMinAllowed && totalReceivedNet <= poMaxAllowed) {
                if (totalReceivedNet == totalQty) {
                    poStatus = "Received";
                } else {
                    poStatus = "Partially Received (Within Tolerance)";
                }
            }
            else if (totalReceivedNet < poMinAllowed) {
                poStatus = "Partially Received";
            }
            else {
                poStatus = "Received in Excess";
            }

            // SAVE PO LEVEL STATUS IN ENTITY
            po.setRmReceiptStatus(poStatus);
            repository.save(po);


            // ---------- Build Response ----------
            return POGenerationResponseDTO.builder()
                    .id(po.getId())
                    .supplierCode(po.getSupplierCode())
                    .supplierName(po.getSupplierName())
                    .timeStamp(po.getTimeStamp())
                    .poNumber(po.getPoNumber())
                    .poGeneratedBy(po.getPoGeneratedBy())
                    .billingAddress(po.getBillingAddress())
                    .shippingAddress(po.getShippingAddress())
                    .poStatus(po.getPoStatus())
                    .remarks(po.getRemarks())
                    .pdflink(po.getPdflink())
                    .dispatchThrough(po.getDispatchThrough())
                    .otherReference(po.getOtherReference())
                    .unit(po.getUnit())
                    .termsOfDelivery(po.getTermsOfDelivery())

                    // return enriched items
                    .items(enrichedItems)

                    .poQuantityKg(totalQty)
                    .rmReceivedQty(totalReceivedNet)
                    .rmReceiptStatus(poStatus)
                    .build();
        });
    }

    @Override
    public List<POGenerationResponseDTO> getAllPOsWithoutPagination() {
        List<POGenerationEntity> pos = repository.findAll();

        return pos.stream().map(po -> {
            double totalQty = po.getItems().stream()
                    .mapToDouble(item -> item.getRequiredQuantity() != null ? item.getRequiredQuantity() : 0.00)
                    .sum();

            List<GRNEntity> grns = grnRepository.findByPoNumber(po.getPoNumber());

            // ------------ ITEM-LEVEL RECEIPT STATUS ------------
            List<POGenerationItemEntity> enrichedItems = po.getItems().stream().map(poItem -> {

                double receivedNet = 0.0;

                for (GRNEntity grn : grns) {
                    if (grn.getGrnItems() == null) continue;
                    for (GRNItemEntity grnItem : grn.getGrnItems()) {
                        if (grnItem.getItemDescription() != null &&
                                grnItem.getItemDescription().equalsIgnoreCase(poItem.getItemDescription())) {

                            if (grnItem.getReceivedNetWeight() != null) {
                                receivedNet += grnItem.getReceivedNetWeight();
                            }
                        }
                    }
                }

                // ---- ITEM RECEIPT STATUS LOGIC ----
                double requiredQty = poItem.getRequiredQuantity() != null ? poItem.getRequiredQuantity() : 0;
                double tolerance = requiredQty * 0.10;
                double minAllowed = requiredQty - tolerance;
                double maxAllowed = requiredQty + tolerance;

                String itemStatus;

                if (receivedNet == 0) {
                    itemStatus = "PENDING";
                }
                else if (receivedNet >= minAllowed && receivedNet <= maxAllowed) {
                    if (receivedNet == requiredQty) {
                        itemStatus = "Received";
                    } else {
                        itemStatus = "Partially Received (Within Tolerance)";
                    }
                }
                else if (receivedNet < minAllowed) {
                    itemStatus = "Partially Received";
                }
                else {
                    itemStatus = "Received in Excess";
                }

                poItem.setRmReceiptStatus(itemStatus);
                poItem.setReceivedNetWeight(receivedNet);

                // attach SO Line No + Order Type
                if (poItem.getPrNumber() != null) {
                    poRequestRepository.findByPrNumber(poItem.getPrNumber())
                            .ifPresent(req -> {
                                poItem.setSoLineNumber(req.getSoNumberLineNumber());
                                poItem.setOrderType(req.getOrderType());
                            });
                }

                return poItem;

            }).toList();

            // ------------ PO-LEVEL RECEIPT STATUS ------------

            double totalReceivedNet = enrichedItems.stream()
                    .mapToDouble(i -> i.getReceivedNetWeight() != null ? i.getReceivedNetWeight() : 0.0)
                    .sum();

            double poTolerance = totalQty * 0.10;
            double poMinAllowed = totalQty - poTolerance;
            double poMaxAllowed = totalQty + poTolerance;

            String poStatus;

            if (totalReceivedNet == 0) {
                poStatus = "PENDING";
            }
            else if (totalReceivedNet >= poMinAllowed && totalReceivedNet <= poMaxAllowed) {
                if (totalReceivedNet == totalQty) {
                    poStatus = "Received";
                } else {
                    poStatus = "Partially Received (Within Tolerance)";
                }
            }
            else if (totalReceivedNet < poMinAllowed) {
                poStatus = "Partially Received";
            }
            else {
                poStatus = "Received in Excess";
            }

            // Save status back into Entity
            po.setRmReceiptStatus(poStatus);
            repository.save(po);

            // ------------ BUILD RESPONSE DTO ------------
            return POGenerationResponseDTO.builder()
                    .id(po.getId())
                    .supplierCode(po.getSupplierCode())
                    .supplierName(po.getSupplierName())
                    .timeStamp(po.getTimeStamp())
                    .poNumber(po.getPoNumber())
                    .poGeneratedBy(po.getPoGeneratedBy())
                    .billingAddress(po.getBillingAddress())
                    .shippingAddress(po.getShippingAddress())
                    .poStatus(po.getPoStatus())
                    .remarks(po.getRemarks())
                    .pdflink(po.getPdflink())
                    .dispatchThrough(po.getDispatchThrough())
                    .otherReference(po.getOtherReference())
                    .unit(po.getUnit())
                    .termsOfDelivery(po.getTermsOfDelivery())

                    .items(enrichedItems)
                    .poQuantityKg(totalQty)

                    .rmReceivedQty(totalReceivedNet)
                    .rmReceiptStatus(poStatus)
                    .poValidity(po.getPoValidity())

                    .build();
        }).toList();
    }

/*

    @Override
    public List<POGenerationResponseDTO> getAllPOsWithoutPagination() {
        List<POGenerationEntity> pos = repository.findAll();

        return pos.stream().map(po -> {
            int totalQty = po.getItems().stream()
                    .mapToInt(item -> item.getRequiredQuantity() != null ? item.getRequiredQuantity() : 0)
                    .sum();

            List<GRNEntity> grns = grnRepository.findByPoNumber(po.getPoNumber());

            // ------------ ITEM-LEVEL RECEIPT STATUS ------------
            List<POGenerationItemEntity> enrichedItems = po.getItems().stream().map(poItem -> {

                double receivedNet = 0.0;

                for (GRNEntity grn : grns) {
                    if (grn.getGrnItems() == null) continue;
                    for (GRNItemEntity grnItem : grn.getGrnItems()) {
                        if (grnItem.getItemDescription() != null &&
                                grnItem.getItemDescription().equalsIgnoreCase(poItem.getItemDescription())) {

                            if (grnItem.getReceivedNetWeight() != null) {
                                receivedNet += grnItem.getReceivedNetWeight();
                            }
                        }
                    }
                }

                // ---- ITEM RECEIPT STATUS LOGIC ----
                double requiredQty = poItem.getRequiredQuantity() != null ? poItem.getRequiredQuantity() : 0;
                double tolerance = requiredQty * 0.10;
                double minAllowed = requiredQty - tolerance;
                double maxAllowed = requiredQty + tolerance;

                String itemStatus;

                if (receivedNet == 0) {
                    itemStatus = "Not Received";
                }
                else if (receivedNet >= minAllowed && receivedNet <= maxAllowed) {
                    if (receivedNet == requiredQty) {
                        itemStatus = "Received";
                    } else {
                        itemStatus = "Partially Received (Within Tolerance)";
                    }
                }
                else if (receivedNet < minAllowed) {
                    itemStatus = "Partially Received";
                }
                else {
                    itemStatus = "Received in Excess";
                }

                poItem.setRmReceiptStatus(itemStatus);
                poItem.setReceivedNetWeight(receivedNet);

                // attach SO Line No + Order Type
                if (poItem.getPrNumber() != null) {
                    poRequestRepository.findByPrNumber(poItem.getPrNumber())
                            .ifPresent(req -> {
                                poItem.setSoLineNumber(req.getSoNumberLineNumber());
                                poItem.setOrderType(req.getOrderType());
                            });
                }

                return poItem;

            }).toList();

            // ------------ PO-LEVEL RECEIPT STATUS ------------

            double totalReceivedNet = enrichedItems.stream()
                    .mapToDouble(i -> i.getReceivedNetWeight() != null ? i.getReceivedNetWeight() : 0.0)
                    .sum();

            double poTolerance = totalQty * 0.10;
            double poMinAllowed = totalQty - poTolerance;
            double poMaxAllowed = totalQty + poTolerance;

            String poStatus;

            if (totalReceivedNet == 0) {
                poStatus = "Not Received";
            }
            else if (totalReceivedNet >= poMinAllowed && totalReceivedNet <= poMaxAllowed) {
                if (totalReceivedNet == totalQty) {
                    poStatus = "Received";
                } else {
                    poStatus = "Partially Received (Within Tolerance)";
                }
            }
            else if (totalReceivedNet < poMinAllowed) {
                poStatus = "Partially Received";
            }
            else {
                poStatus = "Received in Excess";
            }

            // Save status back into Entity
            po.setRmReceiptStatus(poStatus);
            repository.save(po);

            // ------------ BUILD RESPONSE DTO ------------
            return POGenerationResponseDTO.builder()
                    .id(po.getId())
                    .supplierCode(po.getSupplierCode())
                    .supplierName(po.getSupplierName())
                    .timeStamp(po.getTimeStamp())
                    .poNumber(po.getPoNumber())
                    .poGeneratedBy(po.getPoGeneratedBy())
                    .billingAddress(po.getBillingAddress())
                    .shippingAddress(po.getShippingAddress())
                    .poStatus(po.getPoStatus())
                    .remarks(po.getRemarks())
                    .pdflink(po.getPdflink())
                    .dispatchThrough(po.getDispatchThrough())
                    .otherReference(po.getOtherReference())
                    .unit(po.getUnit())
                    .termsOfDelivery(po.getTermsOfDelivery())

                    .items(enrichedItems)
                    .poQuantityKg(totalQty)

                    .rmReceivedQty(totalReceivedNet)
                    .rmReceiptStatus(poStatus)
                    .poValidity(po.getPoValidity())

                    .build();
        }).toList();
    }
*/


    @Override
    public POGenerationEntity updateRemarks(String poNumber, String remarks) {
        POGenerationEntity po = repository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));

        po.setRemarks(remarks);
        return repository.save(po);
    }
    @Transactional
    @Override
    public POGenerationEntity updatePOGeneration(Long id, POGenerationDTO dto) {
        POGenerationEntity po = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("PO not found with ID: " + id));

        // 🔹 Update basic fields if present
        if (dto.getSupplierCode() != null) po.setSupplierCode(dto.getSupplierCode());
        if (dto.getSupplierName() != null) po.setSupplierName(dto.getSupplierName());
        if (dto.getPoGeneratedBy() != null) po.setPoGeneratedBy(dto.getPoGeneratedBy());
        if (dto.getBillingAddress() != null) po.setBillingAddress(dto.getBillingAddress());
        if (dto.getShippingAddress() != null) po.setShippingAddress(dto.getShippingAddress());
        if (dto.getRemarks() != null) po.setRemarks(dto.getRemarks());
        if (dto.getDispatchThrough() != null) po.setDispatchThrough(dto.getDispatchThrough());
        if (dto.getOtherReference() != null) po.setOtherReference(dto.getOtherReference());
        if (dto.getPdflink() != null) po.setPdflink(dto.getPdflink());
        if (dto.getTermsOfDelivery() != null) po.setTermsOfDelivery(dto.getTermsOfDelivery());
        if(dto.getUnit()!=null) po.setUnit(dto.getUnit());

        // 🔹 Replace items if provided
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<String> prNumbersToDelete = new ArrayList<>();
            List<POGenerationItemEntity> updatedItems = new ArrayList<>();

            for (var i : dto.getItems()) {
                if (i.getPrNumber() != null && !i.getPrNumber().isBlank()) {
                    prNumbersToDelete.add(i.getPrNumber());
                }

                updatedItems.add(POGenerationItemEntity.builder()
                        .prNumber(i.getPrNumber())
                        .prCreatedBy(i.getPrCreatedBy())
                        .unit(i.getUnit())
                        .deliveryAddress(i.getDeliveryAddress())
                        .sectionNo(i.getSectionNo())
                        .itemDescription(i.getItemDescription())
                        .productCategory(i.getProductCategory())
                        .brand(i.getBrand())
                        .grade(i.getGrade())
                        .temper(i.getTemper())
                        .requiredQuantity(i.getRequiredQuantity())
                        .uom(i.getUom())
                        .prTypeAndReasonVerifiaction(i.getPrTypeAndReasonverification())
                        .poGeneration(po)
                        .build());
            }

            po.getItems().clear();
            po.getItems().addAll(updatedItems);

            // 🔹 Update PR status if needed
            if (!prNumbersToDelete.isEmpty()) {
                List<PORequestEntity> prList = poRequestRepository.findByPrNumberIn(prNumbersToDelete);
                for (PORequestEntity pr : prList) {
                    pr.setStatus("PO GENERATED");
                }
                poRequestRepository.saveAll(prList);
            }
        }

        return repository.save(po);
    }

    @Override
    public List<POGenerationEntity> getPOGenerationsBetweenDates(Date fromDate, Date toDate) {
        return repository.findByTimeStampBetween(fromDate, toDate);
    }


    public POGenerationEntity updatePOStatusAndItems(String poNumber) {
        POGenerationEntity po = repository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));

        // ✅ Update PO status
        po.setPoStatus("PO PLACED");

        // ✅ Update all item rmReceiptStatus to PENDING
        if (po.getItems() != null) {
            for (POGenerationItemEntity item : po.getItems()) {
                item.setRmReceiptStatus("PENDING");
            }
        }

        POGenerationEntity saved = repository.save(po);

        // Audit log for PO placement
        auditLogService.logAction("STATUS_CHANGE", "PURCHASE_ORDER", "POGeneration",
                saved.getId(), saved.getPoNumber(), "PO CREATED", "PO PLACED",
                "PO " + saved.getPoNumber() + " placed with supplier " + saved.getSupplierName(),
                "SYSTEM", saved.getUnit());

        return saved;
    }

    @Override
    public void deleteAllPOGenerations() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🗑️  DELETE ALL PO GENERATIONS       ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total PO generations before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All PO generations deleted successfully!");
            System.out.println("📊 Total PO generations after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all PO generations: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all PO generations: " + e.getMessage());
        }
    }
}


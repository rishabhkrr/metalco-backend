package com.indona.invento.services.impl;

import com.indona.invento.dao.*;
import com.indona.invento.dto.SalesOrderAddressCreditDTO;
import com.indona.invento.dto.SalesOrderDTO;
import com.indona.invento.dto.SalesOrderLineItemDTO;
import com.indona.invento.dto.SalesOrderLineItemDetailsDto;
import com.indona.invento.entities.*;
import com.indona.invento.services.AuditLogService;
import com.indona.invento.services.NotificationService;
import com.indona.invento.services.SalesOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;

    private final SalesAuthorityRepository salesAuthorityRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private CustomerMasterRepository customerMasterRepository;



    @Autowired
    private SalesOrderLineItemRepository lineItemRepository;

    @Autowired
    private SoSummaryRepository soSummaryRepository;


    @Autowired
    private PORequestRepository poRequestRepository;

    @Autowired
    private PurchaseFollowUpRepository followUpRepository;

    @Autowired
    private POGenerationRepository generationRepository;


    @Autowired
    private  PurchaseFollowUpV2Repository purchaseFollowUpV2Repository;

    @Autowired
    private final SalesOrderSchedulerRepository schedulerRepository;

    @Autowired
    private SchedulerPackingInstructionRepository schedulerPackingInstructionRepository;


    @Autowired
    private SalesOrderLineItemRepository salesOrderLineItemRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private NotificationService notificationService;


//    @Override
//    @Transactional
//    public SalesOrder createSalesOrder(SalesOrderDTO dto) {
//        // 🔹 Build SalesOrder
//        SalesOrder order = SalesOrder.builder()
//                .quotationNo(dto.getQuotationNo())
//                .userId(dto.getUserId())
//                .unit(dto.getUnit())
//                .unitCode(dto.getUnitCode())
//                .customerPoNo(dto.getCustomerPoNo())
//                .customerPoFile(dto.getCustomerPoFile())
//                .customerCode(dto.getCustomerCode())
//                .customerName(dto.getCustomerName())
//                .customerPhone(dto.getCustomerPhone())
//                .customerEmail(dto.getCustomerEmail())
//                .billingAddress(dto.getBillingAddress())
//                .shippingAddress(dto.getShippingAddress())
//                .marketingExecutiveName(dto.getMarketingExecutiveName())
//                .managementAuthority(dto.getManagementAuthority())
//                .packingRequired(dto.getPackingRequired())
//                .pdflink(dto.getPdflink())
//                .acknowledgementSent(dto.getAcknowledgementSent())
//                .approvalLinkSent(dto.getApprovalLinkSent())
//                .status(
//                        (dto.getCustomerPoNo() != null && !dto.getCustomerPoNo().isBlank()) &&
//                                (dto.getCustomerPoFile() != null && !dto.getCustomerPoFile().isBlank())
//                                ? "Active"
//                                : "Pending for Approval"
//                )
//                .build();
//
//        // 🔹 Optional PackingInstruction
//        if (dto.getPackingInstruction() != null) {
//            PackingInstruction instruction = PackingInstruction.builder()
//                    .typeOfPacking(dto.getPackingInstruction().getTypeOfPacking())
//                    .weightInstructions(dto.getPackingInstruction().getWeightInstructions())
//                    .additionalRemarks(dto.getPackingInstruction().getAdditionalRemarks())
//                    .salesOrder(order)
//                    .build();
//            order.setPackingInstruction(instruction);
//        }
//
//        // 🔹 LineItems Mapping with unique slNo
//        List<SalesOrderLineItem> items = IntStream.range(0, dto.getItems().size())
//                .mapToObj(index -> {
//                    SalesOrderLineItemDTO i = dto.getItems().get(index);
//                    return SalesOrderLineItem.builder()
//                            .slNo(index + 1)
//                            .orderType(i.getOrderType())
//                            .productCategory(i.getProductCategory())
//                            .itemDescription(i.getItemDescription())
//                            .brand(i.getBrand())
//                            .grade(i.getGrade())
//                            .temper(i.getTemper())
//                            .dimension(i.getDimension())
//                            .quantityKg(i.getQuantityKg())
//                            .uomKg(i.getUomKg())
//                            .quantityNos(i.getQuantityNos())
//                            .uomNos(i.getUomNos())
//                            .orderMode(i.getOrderMode())
//                            .productionStrategy(i.getProductionStrategy())
//                            .currentPrice(i.getCurrentPrice())
//                            .status(i.getStatus())
//                            .creditPeriod(i.getCreditPeriod())
//                            .targetDispatchDate(i.getTargetDispatchDate())
//                            .packing(dto.getPackingRequired())
//                            .salesOrder(order)
//                            .build();
//                })
//                .collect(Collectors.toList());
//
//        order.setItems(items); // ✅ Cascade will handle save
//
//        // 🔹 Save SalesOrder (items will be saved automatically)
//        SalesOrder savedOrder = salesOrderRepository.save(order);
//
//        return savedOrder;
//    }



    @Override
    public SalesOrder createSalesOrder(SalesOrderDTO dto) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   🚀 CREATING SALES ORDER              ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        System.out.println("📊 Input DTO Details:");
        System.out.println("   Quotation No: " + dto.getQuotationNo());
        System.out.println("   Customer Code: " + dto.getCustomerCode());
        System.out.println("   Customer Name: " + dto.getCustomerName());
        System.out.println("   Has PO No: " + (dto.getCustomerPoNo() != null && !dto.getCustomerPoNo().isBlank()));
        System.out.println("   Has PO File: " + (dto.getCustomerPoFile() != null && !dto.getCustomerPoFile().isBlank()));
        System.out.println("   Total Items in DTO: " + (dto.getItems() != null ? dto.getItems().size() : 0));

        log.info("🚀 Creating SalesOrder for user: {}", dto.getUserId());
        log.info("📦 Incoming packingRequired from DTO = {}", dto.getPackingRequired());

        // ─── FRD CREDIT CHECK ───────────────────────────────────────
        // Per FRD: Before creating SO, validate customer credit limit
        if (dto.getCustomerCode() != null) {
            try {
                CustomerMasterEntity customer = customerMasterRepository.findByCustomerCode(dto.getCustomerCode());
                if (customer != null) {
                    // Check if customer is approved
                    if (!"APPROVED".equalsIgnoreCase(customer.getStatus())) {
                        log.warn("⚠️ Customer {} is not approved (status: {}). SO will be created with Hold status.",
                                customer.getCustomerCode(), customer.getStatus());
                    }

                    // Credit limit check
                    Double creditLimit = customer.getCreditLimitAmount();
                    Double totalAmount = dto.getTotalAmount() != null ? dto.getTotalAmount().doubleValue() : 0.0;
                    if (creditLimit != null && creditLimit > 0 && totalAmount > creditLimit) {
                        log.warn("⚠️ Order amount {} exceeds credit limit {} for customer {}",
                                totalAmount, creditLimit, customer.getCustomerCode());
                        // Don't block, but set status to Hold
                        dto.setCustomerOverdue(true);
                    }
                }
            } catch (Exception e) {
                log.warn("Could not perform credit check: {}", e.getMessage());
            }
        }
        // ─── END CREDIT CHECK ────────────────────────────────────────

        boolean hasPoNo = dto.getCustomerPoNo() != null && !dto.getCustomerPoNo().isBlank();
        boolean hasPoFile = dto.getCustomerPoFile() != null && !dto.getCustomerPoFile().isBlank();

        String status;

        // CONDITION 1 → PO No + PDF AVAILABLE
        if (hasPoNo && hasPoFile) {

            // Check customer overdue
            if (Boolean.TRUE.equals(dto.getCustomerOverdue())) {
                status = "Hold";          // overdue → HOLD
            } else {
                status = "Active";        // not overdue → ACTIVE
            }
        }

        // CONDITION 2 → PO No or PDF missing
        else {
            status = "Pending for Approval";   // customer must approve
        }


        SalesOrder order = SalesOrder.builder()
                .quotationNo(dto.getQuotationNo())
                .userId(dto.getUserId())
                .unit(dto.getUnit())
                .unitCode(dto.getUnitCode())
                .customerPoNo(dto.getCustomerPoNo())
                .customerPoFile(dto.getCustomerPoFile())
                .customerCode(dto.getCustomerCode())
                .customerName(dto.getCustomerName())
                .customerPhone(dto.getCustomerPhone())
                .customerEmail(dto.getCustomerEmail())
                .billingAddress(dto.getBillingAddress())
                .shippingAddress(dto.getShippingAddress())
                .sameAsBillingAddress(dto.getSameAsBillingAddress())
                .marketingExecutiveName(dto.getMarketingExecutiveName())
                .managementAuthority(dto.getManagementAuthority())
                .packingRequired(dto.getPackingRequired())
                .pdflink(dto.getPdflink())
                .acknowledgementSent(dto.getAcknowledgementSent())
                .approvalLinkSent(dto.getApprovalLinkSent())
                .customerOverdue(dto.getCustomerOverdue())
                .status(status)
                .cgst(dto.getCgst())
                .sgst(dto.getSgst())
                .igst(dto.getIgst())
                .freightCharges(dto.getFreightCharges())
                .hamaliCharges(dto.getHamaliCharges())
                .packingCharges(dto.getPackingCharges())
                .cuttingCharges(dto.getCuttingCharges())
                .laminationCharges(dto.getLaminationCharges())
                .totalAmount(dto.getTotalAmount())
                .subTotalAmount(dto.getSubTotalAmount())
                .build();

        log.info("🛠️ SalesOrder entity built with packingRequired={}", order.getPackingRequired());

        // 🔹 PackingInstruction Mapping
        if (dto.getPackingInstruction() != null) {
            PackingInstruction instruction = PackingInstruction.builder()
                    .typeOfPacking(dto.getPackingInstruction().getTypeOfPacking())
                    .weightInstructions(dto.getPackingInstruction().getWeightInstructions())
                    .additionalRemarks(dto.getPackingInstruction().getAdditionalRemarks())
                    .salesOrder(order)
                    .build();
            order.setPackingInstruction(instruction);
            log.info("📑 PackingInstruction mapped for SalesOrder with packingRequired={}", order.getPackingRequired());
        }

        // 🔹 Create LineItems with backend-generated lineNumber
        AtomicInteger counter = new AtomicInteger(1);
        List<SalesOrderLineItem> items = dto.getItems().stream()
                .map(i -> {
                    String lineNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    log.info("📄 Generated lineNumber={} for itemDesc={} | packingRequired={}", lineNumber, i.getItemDescription(), dto.getPackingRequired());

                    return SalesOrderLineItem.builder()
                            .slNo(counter.getAndIncrement())
                            .orderType(i.getOrderType())
                            .productCategory(i.getProductCategory())
                            .itemDescription(i.getItemDescription())
                            .brand(i.getBrand())
                            .grade(i.getGrade())
                            .creditPeriod(i.getCreditPeriod())
                            .temper(i.getTemper())
                            .dimension(i.getDimension())
                            .quantityKg(i.getQuantityKg())
                            .uomKg(i.getUomKg())
                            .quantityNos(i.getQuantityNos())
                            .uomNos(i.getUomNos())
                            .targetDispatchDate(i.getTargetDispatchDate())
                            .currentPrice(i.getCurrentPrice())
                            .orderMode(i.getOrderMode())
                            .status(i.getStatus())
                            .productionStrategy(i.getProductionStrategy())
                            .lineNumber(lineNumber)
                            .packing(dto.getPackingRequired())
                            .salesOrder(order)
                            .totalPrice(i.getTotalPrice())
                            .priceSnapshot(serializeJson(i.getPriceSnapshot()))
                            .stockSummary(serializeJson(i.getStockSummary()))
                            .build();
                })
                .toList();

        order.setItems(items);

        // 🔹 Save SalesOrder
        SalesOrder savedOrder = salesOrderRepository.save(order);
        String soNumber = savedOrder.getSoNumber();
        log.info("✅ SalesOrder saved with soNumber={} and packingRequired={}", soNumber, savedOrder.getPackingRequired());

        // 🔹 Customer Category
        String customerCategory = Optional.ofNullable(savedOrder.getCustomerCode())
                .map(customerMasterRepository::findByCustomerCode)
                .map(CustomerMasterEntity::getCustomerCategory)
                .orElse(null);

        List<SalesOrderSchedulerEntity> schedulerEntries = new ArrayList<>();
        List<SoSummaryItemEntity> summaryItems = new ArrayList<>();
        Set<String> addedLines = new HashSet<>();

        // 🔹 Process each LineItem
        for (SalesOrderLineItem item : savedOrder.getItems()) {

            if (!"WAREHOUSE".equalsIgnoreCase(item.getOrderMode())) continue;

            if (!addedLines.add(item.getLineNumber())) continue;

            String nextProcess = ("INHOUSE".equalsIgnoreCase(item.getProductionStrategy()) &&
                    "CUT".equalsIgnoreCase(item.getOrderType())) ? "MARKING & CUTTING" : "DISPATCH";

            schedulerEntries.add(SalesOrderSchedulerEntity.builder()
                    .slNo(item.getSlNo())
                    .lineNumber(item.getLineNumber())
                    .nextProcess(nextProcess)
                    .planDate(LocalDate.now())
                    .soNumber(soNumber)
                    .unit(savedOrder.getUnit())
                    .customerCode(savedOrder.getCustomerCode())
                    .customerName(savedOrder.getCustomerName())
                    .customerCategory(customerCategory)
                    .orderType(item.getOrderType())
                    .productCategory(item.getProductCategory())
                    .itemDescription(item.getItemDescription())
                    .brand(item.getBrand())
                    .grade(item.getGrade())
                    .uomKg(item.getUomKg())
                    .uomNo(item.getUomNos())
                    .productionStrategy(item.getProductionStrategy())
                    .temper(item.getTemper())
                    .packing(savedOrder.getPackingRequired())
                    .dimension(item.getDimension())
                    .requiredQuantityKg(BigDecimal.valueOf(item.getQuantityKg()))
                    .requiredQuantityNo((int) item.getQuantityNos())
                    .targetDateOfDispatch(item.getTargetDispatchDate())
                    .retrievalStatus("PENDING")
                    .build());

            summaryItems.add(SoSummaryItemEntity.builder()
                    .lineNumber(item.getLineNumber())
                    .orderType(item.getOrderType())
                    .productCategory(item.getProductCategory())
                    .itemDescription(item.getItemDescription())
                    .brand(item.getBrand())
                    .grade(item.getGrade())
                    .temper(item.getTemper())
                    .dimension(item.getDimension())
                    .orderQuantityKg(BigDecimal.valueOf(item.getQuantityKg()))
                    .uomKg(item.getUomKg())
                    .productionStrategy(item.getProductionStrategy())
                    .orderQuantityNo((int) item.getQuantityNos())
                    .uomNo(item.getUomNos())
                    .creditDays(item.getCreditPeriod())
                    .targetDispatchDate(item.getTargetDispatchDate())
                    .dispatchDate(null)
                    .dispatchQuantityKg(BigDecimal.ZERO)
                    .dispatchQuantityNo(0)
                    .amount(BigDecimal.ZERO)
                    .totalAmount(BigDecimal.ZERO)
                    .invoiceNumber(null)
                    .lrNumberUpdation(null)
                    .soStatus("CREATED")
                    .build());
        }

        log.info("💾 Saving {} scheduler entries with packingRequired={}", schedulerEntries.size(), savedOrder.getPackingRequired());
        schedulerRepository.saveAll(schedulerEntries);

        log.info("📦 Saving summary entity with {} items and packingStatus={}", summaryItems.size(), savedOrder.getPackingRequired());
        SoSummaryEntity summary = SoSummaryEntity.builder()
                .userId(savedOrder.getUserId())
                .timestamp(savedOrder.getCreatedAt())
                .quotationNo(savedOrder.getQuotationNo())
                .soNumber(soNumber)
                .unit(savedOrder.getUnit())
                .customerPoNo(savedOrder.getCustomerPoNo())
                .customerCode(savedOrder.getCustomerCode())
                .customerName(savedOrder.getCustomerName())
                .customerPhoneNo(savedOrder.getCustomerPhone())
                .customerEmail(savedOrder.getCustomerEmail())
                .marketingExecutiveName(savedOrder.getMarketingExecutiveName())
                .managementAuthority(savedOrder.getManagementAuthority())
                .packingStatus(savedOrder.getPackingRequired())
                .timestamp(LocalDateTime.now())
                .items(summaryItems)
                .build();

        summaryItems.forEach(i -> i.setSummary(summary));
        soSummaryRepository.save(summary);

        log.info("🎉 SalesOrder creation complete for soNumber={} with packingRequired={}", soNumber, savedOrder.getPackingRequired());

        // Audit log for SO creation
        auditLogService.logAction("CREATE", "SALES_ORDER", "SalesOrder",
                savedOrder.getId(), soNumber, null, savedOrder.getStatus(),
                "Sales Order " + soNumber + " created for customer " + savedOrder.getCustomerName() +
                        " with status: " + savedOrder.getStatus(),
                dto.getUserId() != null ? dto.getUserId() : "SYSTEM",
                savedOrder.getUnitCode());

        return savedOrder;
    }

    @Override
    public SalesOrder getSalesOrderById(Long id) {
        return salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Order not found with id: " + id));
    }

    @Override
    public SalesOrder deleteSalesOrder(Long id) {
        SalesOrder order = getSalesOrderById(id);
        salesOrderRepository.delete(order);
        return order;
    }

//    @Override
//    public SalesOrder cancelAndDeleteSalesOrder(Long id) {
//        SalesOrder order = getSalesOrderById(id);
//        order.setApprovalLinkSent(false);
//        order.setAcknowledgementSent(false);
//        order.setPackingRequired(false);
//        order.setCustomerPoNo("CANCELLED");
//        salesOrderRepository.save(order);
//        salesOrderRepository.delete(order);
//        return order;
//    }

    @Override
    public Page<SalesOrder> getAllSalesOrders(Pageable pageable) {
        return salesOrderRepository.findAll(pageable);
    }

    @Override
    public List<SalesOrder> getAllSalesOrdersWithoutPagination() {
        return salesOrderRepository.findAll();
    }

    @Override
    public List<SalesAuthority> getAllSalesAuthorities() {
        return salesAuthorityRepository.findAll();
    }

    @Override
    public SalesAuthority addSalesAuthority(String name) {
        if (salesAuthorityRepository.existsByName(name)) {
            throw new RuntimeException("Sales authority already exists");
        }
        return salesAuthorityRepository.save(new SalesAuthority(name));
    }

    @Override
    public SalesOrder getSalesOrderBySoNumber(String soNumber) {
        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales order not found with number: " + soNumber));
        if (order.getStatus().equalsIgnoreCase("Pending for Approval")) {
            return order;
        }
        if (order.getStatus().equalsIgnoreCase("Cancelled") ||
                order.getStatus().equalsIgnoreCase("Rejected")) {
            return order;
        }
        boolean isOverdue = Boolean.TRUE.equals(order.getCustomerOverdue());

        String finalStatus = isOverdue ? "Hold" : "Active";
        if (!order.getStatus().equalsIgnoreCase(finalStatus)) {
            order.setStatus(finalStatus);
            order = salesOrderRepository.save(order);
        }

        return order;
    }



    @Override
    @Transactional
    public SalesOrder cancelSalesOrder(String soNumber) {

        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales order not found with number: " + soNumber));

        // 1. Cancel Sales Order
        order.setStatus("Cancelled");
        SalesOrder savedOrder = salesOrderRepository.save(order);

        // 2. Cancel Scheduler Rows
        List<SalesOrderSchedulerEntity> schedulers =
                schedulerRepository.findBySoNumber(soNumber);

        for (SalesOrderSchedulerEntity scheduler : schedulers) {
            scheduler.setRetrievalStatus("Cancelled");
        }

        schedulerRepository.saveAll(schedulers);

        // Audit log for SO cancellation
        auditLogService.logAction("STATUS_CHANGE", "SALES_ORDER", "SalesOrder",
                savedOrder.getId(), soNumber, "Active", "Cancelled",
                "Sales Order " + soNumber + " cancelled. " + schedulers.size() + " scheduler entries also cancelled.",
                savedOrder.getUserId() != null ? savedOrder.getUserId() : "SYSTEM",
                savedOrder.getUnitCode());

        return savedOrder;
    }



    @Override
    public SalesOrder updateStatus(String soNumber, String status) {
        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales order not found with number: " + soNumber));

        String oldStatus = order.getStatus();
        order.setStatus(status);
        SalesOrder saved = salesOrderRepository.save(order);

        // Audit log for status change
        auditLogService.logAction("STATUS_CHANGE", "SALES_ORDER", "SalesOrder",
                saved.getId(), soNumber, oldStatus, status,
                "Sales Order " + soNumber + " status changed from " + oldStatus + " to " + status,
                "SYSTEM", saved.getUnitCode());

        return saved;
    }


    @Override
    public Page<SalesOrder> getAllSalesOrder(Pageable pageable) {
        return salesOrderRepository.findAll(pageable);
    }


    @Override
    public Map<String, Object> updateMultipleLineItemStatuses(List<String> lineNumbers, String status, String soNumber) {
        List<Map<String, String>> updatedItems = new ArrayList<>();

        for (String lineNumber : lineNumbers) {
            SalesOrderLineItem item = lineItemRepository.findByLineNumber(lineNumber)
                    .orElseThrow(() -> new RuntimeException("Line item not found: " + lineNumber));

            item.setStatus(status);
            lineItemRepository.save(item);

            updatedItems.add(Map.of("lineNumber", lineNumber, "status", status));
        }

        // ✅ Update SalesOrder status directly using soNumber
        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales Order not found: " + soNumber));

        order.setStatus(status);
        salesOrderRepository.save(order);

        return Map.of(
                "salesOrder", soNumber,
                "updatedStatus", status,
                "updatedItems", updatedItems
        );
    }

    @Override
    @Transactional
    public SalesOrder updateSalesOrder(Long id, SalesOrderDTO dto) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("SalesOrder not found with ID: " + id));

        // 🔹 Update only non-null basic fields
        if (dto.getQuotationNo() != null) order.setQuotationNo(dto.getQuotationNo());
        if (dto.getUserId() != null) order.setUserId(dto.getUserId());
        if (dto.getUnit() != null) order.setUnit(dto.getUnit());
        if (dto.getCustomerPoNo() != null) order.setCustomerPoNo(dto.getCustomerPoNo());
        if (dto.getCustomerCode() != null) order.setCustomerCode(dto.getCustomerCode());
        if (dto.getCustomerName() != null) order.setCustomerName(dto.getCustomerName());
        if (dto.getCustomerPhone() != null) order.setCustomerPhone(dto.getCustomerPhone());
        if (dto.getCustomerEmail() != null) order.setCustomerEmail(dto.getCustomerEmail());
        if (dto.getBillingAddress() != null) order.setBillingAddress(dto.getBillingAddress());
        if (dto.getUnitCode() != null) order.setUnitCode(dto.getUnitCode());
        if (dto.getShippingAddress() != null) order.setShippingAddress(dto.getShippingAddress());
        if (dto.getSameAsBillingAddress() != null) order.setSameAsBillingAddress(dto.getSameAsBillingAddress());
        if (dto.getMarketingExecutiveName() != null) order.setMarketingExecutiveName(dto.getMarketingExecutiveName());
        if (dto.getManagementAuthority() != null) order.setManagementAuthority(dto.getManagementAuthority());
        if (dto.getCustomerPoFile() != null) order.setCustomerPoFile(dto.getCustomerPoFile());
        if (dto.getPdflink() != null) order.setPdflink(dto.getPdflink());
        if (dto.getPackingRequired() != null) order.setPackingRequired(dto.getPackingRequired());
        if (dto.getAcknowledgementSent() != null) order.setAcknowledgementSent(dto.getAcknowledgementSent());
        if (dto.getApprovalLinkSent() != null) order.setApprovalLinkSent(dto.getApprovalLinkSent());

        if (dto.getCgst() != null) order.setCgst(dto.getCgst());
        if (dto.getSgst() != null) order.setSgst(dto.getSgst());
        if (dto.getIgst() != null) order.setIgst(dto.getIgst());

        if (dto.getFreightCharges() != null) order.setFreightCharges(dto.getFreightCharges());
        if (dto.getHamaliCharges() != null) order.setHamaliCharges(dto.getHamaliCharges());
        if (dto.getPackingCharges() != null) order.setPackingCharges(dto.getPackingCharges());
        if (dto.getCuttingCharges() != null) order.setCuttingCharges(dto.getCuttingCharges());
        if (dto.getLaminationCharges() != null) order.setLaminationCharges(dto.getLaminationCharges());

        if (dto.getSubTotalAmount() != null) order.setSubTotalAmount(dto.getSubTotalAmount());
        if (dto.getTotalAmount() != null) order.setTotalAmount(dto.getTotalAmount());

        // 🔹 Packing Instruction
        if (dto.getPackingInstruction() != null) {
            PackingInstruction instruction = order.getPackingInstruction();
            if (instruction == null) {
                instruction = new PackingInstruction();
                instruction.setSalesOrder(order);
                order.setPackingInstruction(instruction);
            }
            if (dto.getPackingInstruction().getTypeOfPacking() != null)
                instruction.setTypeOfPacking(dto.getPackingInstruction().getTypeOfPacking());
            if (dto.getPackingInstruction().getWeightInstructions() != null)
                instruction.setWeightInstructions(dto.getPackingInstruction().getWeightInstructions());
            if (dto.getPackingInstruction().getAdditionalRemarks() != null)
                instruction.setAdditionalRemarks(dto.getPackingInstruction().getAdditionalRemarks());
        }

        // 🔹 Line Items (preserve all items, don't collapse duplicates)
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            System.out.println("\n📦 Updating line items...");
            System.out.println("   DTO Items count: " + dto.getItems().size());
            System.out.println("   Existing Items count: " + order.getItems().size());

            // Preserve existing line numbers
            List<String> existingLineNumbers = order.getItems().stream()
                    .map(SalesOrderLineItem::getLineNumber)
                    .collect(Collectors.toList());

            List<SalesOrderLineItem> rebuilt = new ArrayList<>();

            for (int index = 0; index < dto.getItems().size(); index++) {
                SalesOrderLineItemDTO i = dto.getItems().get(index);
                System.out.println("   [Item " + (index + 1) + "] Processing: " + i.getItemDescription());

                // ✅ Use existing lineNumber if available, otherwise generate new
                String lineNumber;
                if (index < existingLineNumbers.size()) {
                    lineNumber = existingLineNumbers.get(index);
                    System.out.println("        Using existing lineNumber: " + lineNumber);
                } else {
                    lineNumber = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    System.out.println("        Generated new lineNumber: " + lineNumber);
                }

                SalesOrderLineItem target = new SalesOrderLineItem();
                target.setSalesOrder(order);
                target.setLineNumber(lineNumber);
                target.setSlNo(index + 1);

                // Set all fields from DTO
                if (i.getOrderType() != null) target.setOrderType(i.getOrderType());
                if (i.getProductCategory() != null) target.setProductCategory(i.getProductCategory());
                if (i.getItemDescription() != null) target.setItemDescription(i.getItemDescription());
                if (i.getBrand() != null) target.setBrand(i.getBrand());
                if (i.getGrade() != null) target.setGrade(i.getGrade());
                if (i.getCreditPeriod() != null) target.setCreditPeriod(i.getCreditPeriod());
                if (i.getTemper() != null) target.setTemper(i.getTemper());
                if (i.getDimension() != null) target.setDimension(i.getDimension());
                if (i.getQuantityKg() != null) target.setQuantityKg(i.getQuantityKg());
                if (i.getUomKg() != null) target.setUomKg(i.getUomKg());
                if (i.getQuantityNos() != null) target.setQuantityNos(i.getQuantityNos());
                if (i.getUomNos() != null) target.setUomNos(i.getUomNos());
                if (i.getTargetDispatchDate() != null) target.setTargetDispatchDate(i.getTargetDispatchDate());
                if (i.getCurrentPrice() != null) target.setCurrentPrice(i.getCurrentPrice());
                if (i.getOrderMode() != null) target.setOrderMode(i.getOrderMode());
                if (i.getStatus() != null) target.setStatus(i.getStatus());
                if (i.getProductionStrategy() != null) target.setProductionStrategy(i.getProductionStrategy());
                if (i.getTotalPrice() != null) target.setTotalPrice(i.getTotalPrice());
                if (i.getPriceSnapshot() != null) target.setPriceSnapshot(serializeJson(i.getPriceSnapshot()));
                if (i.getStockSummary() != null) target.setStockSummary(serializeJson(i.getStockSummary()));

                target.setPacking(Boolean.TRUE.equals(order.getPackingRequired()));
                rebuilt.add(target);
                System.out.println("        ✅ Added to rebuilt list");
            }

            System.out.println("   Final rebuilt items count: " + rebuilt.size());
            order.getItems().clear();
            order.getItems().addAll(rebuilt);
            System.out.println("   ✅ All items updated successfully\n");
        }
        // else: keep old items unchanged

        // 🔹 Save Sales Order
        SalesOrder updatedOrder = salesOrderRepository.save(order);

        // 🔹 Scheduler Mapping (avoid duplicates)
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            Set<String> existingLineNumbers = schedulerRepository.findBySoNumber(updatedOrder.getSoNumber()).stream()
                    .map(SalesOrderSchedulerEntity::getLineNumber)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            List<SalesOrderSchedulerEntity> schedulerEntries = updatedOrder.getItems().stream()
                    .filter(i -> i.getOrderMode() != null && i.getOrderMode().equalsIgnoreCase("WAREHOUSE"))
                    .filter(i -> i.getLineNumber() != null && !existingLineNumbers.contains(i.getLineNumber()))
                    .map(i -> {
                        double qtyKg = i.getQuantityKg(); // primitive safe
                        int qtyNos = (int) i.getQuantityNos();  // primitive safe
                        return SalesOrderSchedulerEntity.builder()
                                .slNo(i.getSlNo())
                                .lineNumber(i.getLineNumber())
                                .nextProcess("MARKING & CUTTING")
                                .planDate(LocalDate.now())
                                .soNumber(updatedOrder.getSoNumber())
                                .unit(updatedOrder.getUnit())
                                .customerCode(updatedOrder.getCustomerCode())
                                .customerName(updatedOrder.getCustomerName())
                                .orderType(i.getOrderType())
                                .productCategory(i.getProductCategory())
                                .itemDescription(i.getItemDescription())
                                .brand(i.getBrand())
                                .grade(i.getGrade())
                                .temper(i.getTemper())
                                .dimension(i.getDimension())
                                .requiredQuantityKg(qtyKg <= 0 ? BigDecimal.ZERO : BigDecimal.valueOf(qtyKg))
                                .requiredQuantityNo(Math.max(0, qtyNos))
                                .targetDateOfDispatch(i.getTargetDispatchDate())
                                .retrievalStatus("PENDING")
                                .build();
                    })
                    .toList();

            if (!schedulerEntries.isEmpty()) {
                schedulerRepository.saveAll(schedulerEntries);
            }
        }

        return updatedOrder;
    }

    @Override
    public Page<SalesOrder> getSalesOrdersBetweenDates(LocalDateTime fromDate, LocalDateTime toDate, Pageable pageable) {
        return salesOrderRepository.findByCreatedAtBetween(fromDate, toDate, pageable);
    }


    @Override
    public void storePurchaseFollowUps(SalesOrder salesOrder) {
        for (SalesOrderLineItem item : salesOrder.getItems()) {
            // ✅ Check orderMode instead of orderType
            if ("Purchase Followup".equalsIgnoreCase(item.getOrderMode())) {
                String itemDesc = item.getItemDescription();
                System.out.println("Checking item for follow-up: " + itemDesc);

                // ✅ Filter PORequests with status PENDING and matching itemDescription
                List<PORequestEntity> matchingPOs = poRequestRepository.findAll().stream()
                        .filter(po -> "PENDING".equalsIgnoreCase(po.getStatus()))
                        .filter(po -> po.getProducts().stream()
                                .anyMatch(prod -> prod.getItemDescription() != null &&
                                        prod.getItemDescription().equalsIgnoreCase(itemDesc)))
                        .toList();

                for (PORequestEntity po : matchingPOs) {
                    for (POProductEntity prod : po.getProducts()) {
                        if (prod.getItemDescription() != null &&
                                prod.getItemDescription().equalsIgnoreCase(itemDesc)) {

                            System.out.println("Matched PO: " + po.getPrNumber() + " for item: " + itemDesc);

                            PurchaseFollowUpEntity followUp = PurchaseFollowUpEntity.builder()
                                    .salesOrderNumber(salesOrder.getSoNumber())
                                    .lineItemNumber(item.getLineNumber())
                                    .itemDescription(itemDesc)
                                    .poNumber(po.getPrNumber())
                                    .poStatus(po.getStatus())
                                    .supplierName(po.getSupplierName())
                                    .unit(po.getUnit())
                                    .requiredQuantity(prod.getRequiredQuantity())
                                    .poOrderDate(po.getTimeStamp())
                                    .build();

                            followUpRepository.save(followUp);
                        }
                    }
                }
            }
        }
    }


    @Override
    public void storePurchaseFollowUpsV2(SalesOrder salesOrder) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   📦 STORING PURCHASE FOLLOW UPS V2    ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        System.out.println("📍 Sales Order Details:");
        System.out.println("   SO Number: " + salesOrder.getSoNumber());
        System.out.println("   Customer Code: " + salesOrder.getCustomerCode());
        System.out.println("   Customer Name: " + salesOrder.getCustomerName());
        System.out.println("   Total Items: " + (salesOrder.getItems() != null ? salesOrder.getItems().size() : 0));

        if (salesOrder.getItems() == null || salesOrder.getItems().isEmpty()) {
            System.out.println("⚠️ WARNING: No items found in sales order!");
            return;
        }

        List<POGenerationEntity> allPOs = generationRepository.findAll();
        System.out.println("\n📊 Total POs in system: " + allPOs.size());

        // DEBUG: Show all PO items
        System.out.println("📋 Available PO Items in System:");
        for (POGenerationEntity po : allPOs) {
            System.out.println("\n   PO Number: " + po.getPoNumber());
            if (po.getItems() != null) {
                for (POGenerationItemEntity poItem : po.getItems()) {
                    System.out.println("      - Item: " + poItem.getItemDescription());
                    System.out.println("        RM Status: " + poItem.getRmReceiptStatus());
                    System.out.println("        SO Line No: " + (poItem.getSoLineNumber() == null ? "NULL" : poItem.getSoLineNumber()));
                }
            }
        }

        Set<String> processedDescriptions = new HashSet<>();

        for (SalesOrderLineItem item : salesOrder.getItems()) {
            String itemDesc = item.getItemDescription();
            System.out.println("\n✓ Processing Line Item:");
            System.out.println("   Line Number: " + item.getLineNumber());
            System.out.println("   Item Description: " + itemDesc);
            System.out.println("   Quantity KG: " + item.getQuantityKg());

            if (processedDescriptions.contains(itemDesc)) {
                System.out.println("   ⚠️ Already processed this description, skipping...");
                continue;
            }

            POGenerationItemEntity matchedItem = null;
            POGenerationEntity matchedPO = null;

            System.out.println("   🔍 Searching for matching PO...");

            // DEBUG: Check each PO
            for (POGenerationEntity po : allPOs) {
                System.out.println("      Checking PO: " + po.getPoNumber());
                if (po.getItems() == null || po.getItems().isEmpty()) {
                    System.out.println("         ⚠️ No items in this PO");
                    continue;
                }

                for (POGenerationItemEntity poItem : po.getItems()) {
                    System.out.println("         Comparing with: " + poItem.getItemDescription());
                    System.out.println("            Match desc? " + itemDesc.equalsIgnoreCase(poItem.getItemDescription()));
                    System.out.println("            RM Status = PENDING? " + "PENDING".equalsIgnoreCase(poItem.getRmReceiptStatus()));
                    System.out.println("            Order Type: " + poItem.getOrderType());
                    System.out.println("            Is Make to Stock? " + ("Make to Stock".equalsIgnoreCase(poItem.getOrderType())));
                    System.out.println("            Is Inventory Analysis? " + ("Inventory Analysis".equalsIgnoreCase(poItem.getOrderType())));
                    System.out.println("            SO Line blank? " + (poItem.getSoLineNumber() == null || poItem.getSoLineNumber().isBlank()));

                    // Updated condition: Check item desc + RM status PENDING + Order Type (Make to Stock OR Inventory Analysis)
                    boolean isValidOrderType = "Make to Stock".equalsIgnoreCase(poItem.getOrderType()) ||
                                              "Inventory Analysis".equalsIgnoreCase(poItem.getOrderType());

                    if (itemDesc.equalsIgnoreCase(poItem.getItemDescription()) &&
                            "PENDING".equalsIgnoreCase(poItem.getRmReceiptStatus()) &&
                            isValidOrderType &&
                            (poItem.getSoLineNumber() == null || poItem.getSoLineNumber().isBlank())) {
                        System.out.println("            ✅ MATCH FOUND!");
                        matchedItem = poItem;
                        matchedPO = po;
                        break;
                    }
                }

                if (matchedItem != null) {
                    break;
                }
            }

            if (matchedItem != null && matchedPO != null) {
                System.out.println("   ✅ FOUND MATCHING PO!");
                System.out.println("      PO Number: " + matchedPO.getPoNumber());
                System.out.println("      PO Item ID: " + matchedItem.getId());

                System.out.println("   💾 Assigning SO line number to PO item...");
                // ✅ Assign SO line number
                matchedItem.setSoLineNumber(item.getLineNumber());

                // ✅ Save updated PO entity with modified item
                generationRepository.save(matchedPO);
                System.out.println("   ✅ PO item updated and saved!");

                System.out.println("   📝 Creating PurchaseFollowUp record...");
                PurchaseFollowUpEntityV2 followUp = PurchaseFollowUpEntityV2.builder()
                        .unit(matchedItem.getUnit())
                        .poNumber(matchedPO.getPoNumber())
                        .poGeneratedBy(matchedPO.getPoGeneratedBy())
                        .orderDate(matchedPO.getTimeStamp())
                        .poQuantityKg(matchedItem.getRequiredQuantity())
                        .supplier(matchedPO.getSupplierName())
                        .billingAddress(matchedPO.getBillingAddress())
                        .shippingAddress(matchedPO.getShippingAddress())
                        .prNumber(matchedItem.getPrNumber())
                        .prCreatedBy(matchedItem.getPrCreatedBy())
                        .prType(matchedItem.getPrTypeAndReasonVerifiaction())
                        .sectionNo(matchedItem.getSectionNo())
                        .itemDescription(matchedItem.getItemDescription())
                        .productCategory(matchedItem.getProductCategory())
                        .brand(matchedItem.getBrand())
                        .grade(matchedItem.getGrade())
                        .temper(matchedItem.getTemper())
                        .salesOrderNumber(salesOrder.getSoNumber())
                        .lineItemNumber(item.getLineNumber())
                        .targetDispatchDate(item.getTargetDispatchDate())
                        .followUpStatus("Pending")
                        .requiredQuantity((int) item.getQuantityKg())
                        .uom(item.getUomKg())
                        .build();

                purchaseFollowUpV2Repository.save(followUp);
                System.out.println("   ✅ PurchaseFollowUp record saved!");
                processedDescriptions.add(itemDesc);
            } else {
                System.out.println("   ❌ NO MATCHING PO FOUND for item: " + itemDesc);
                System.out.println("      Reason: Item description not found OR RM Status is not PENDING OR Order Type is neither 'Make to Stock' nor 'Inventory Analysis' OR SO Line already assigned");
            }
        }

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   ✅ FOLLOW UP STORAGE COMPLETE        ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }


    @Override
    public SalesOrder viewSalesOrderBySoNumber(String soNumber) {
        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales order not found with number: " + soNumber));

        String status = order.getStatus();

        if (status.equalsIgnoreCase("Pending for Approval") ||
                status.equalsIgnoreCase("Cancelled") ||
                status.equalsIgnoreCase("Rejected")) {
            return order;
        }
        if (status.equalsIgnoreCase("Approved") ||
                status.equalsIgnoreCase("Partially Approved") ||
                status.equalsIgnoreCase("Active")) {

            boolean isOverdue = Boolean.TRUE.equals(order.getCustomerOverdue());
            String finalStatus = isOverdue ? "Hold" : "Active";

            if (!order.getStatus().equalsIgnoreCase(finalStatus)) {
                order.setStatus(finalStatus);
                order = salesOrderRepository.save(order);
            }

            return order;
        }
        boolean isOverdue = Boolean.TRUE.equals(order.getCustomerOverdue());
        String finalStatus = isOverdue ? "Hold" : "Active";

        if (!order.getStatus().equalsIgnoreCase(finalStatus)) {
            order.setStatus(finalStatus);
            order = salesOrderRepository.save(order);
        }

        return order;
    }

    @Override
    public void deleteAllSalesOrders() {
        salesOrderRepository.deleteAll();
    }

    // ========== SO Approval for Overdue Customers ==========

    @Override
    public List<SalesOrder> getPendingOverdueSalesOrders() {
        return salesOrderRepository.findByStatusAndCustomerOverdue("Hold", true);
    }

    @Override
    public List<SalesOrder> getPendingApprovalSalesOrders() {
        return salesOrderRepository.findByStatusIgnoreCase("Pending for Approval");
    }

    @Override
    public SalesOrder approveSalesOrder(Long id, String approvalRemarks) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Order not found with id: " + id));

        order.setStatus("Active");
        order.setApprovalRemarks(approvalRemarks);

        log.info("✅ SO {} approved. Status changed to Active", order.getSoNumber());
        return salesOrderRepository.save(order);
    }

    @Override
    public SalesOrder rejectSalesOrder(Long id, String approvalRemarks) {
        SalesOrder order = salesOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sales Order not found with id: " + id));

        order.setStatus("Rejected");
        order.setApprovalRemarks(approvalRemarks);

        log.info("❌ SO {} rejected", order.getSoNumber());
        return salesOrderRepository.save(order);
    }

    @Override
    public SalesOrderAddressCreditDTO getAddressAndCreditDetails(String soNumber, String lineNumber) {
        log.info("🔍 Looking for SalesOrder with soNumber: {}", soNumber);

        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber).orElse(null);

        if (order == null) {
            log.warn("❌ SalesOrder not found for soNumber: {}", soNumber);
            return new SalesOrderAddressCreditDTO("Data not found", "Data not found", "Data not found");
        }

        log.info("✅ SalesOrder found: ID={}, Billing={}, Shipping={}", order.getId(), order.getBillingAddress(), order.getShippingAddress());

        SalesOrderLineItem lineItem = order.getItems().stream()
                .filter(item -> item.getLineNumber() != null && item.getLineNumber().equalsIgnoreCase(lineNumber))
                .findFirst()
                .orElse(null);

        if (lineItem == null) {
            log.warn("❌ LineItem not found for lineNumber: {} in SalesOrder: {}", lineNumber, soNumber);
            return new SalesOrderAddressCreditDTO(
                    order.getBillingAddress() != null ? order.getBillingAddress() : "Data not found",
                    order.getShippingAddress() != null ? order.getShippingAddress() : "Data not found",
                    "Data not found"
            );
        }

        log.info("✅ LineItem found: ID={}, CreditPeriod={}", lineItem.getId(), lineItem.getCreditPeriod());

        return new SalesOrderAddressCreditDTO(
                order.getBillingAddress() != null ? order.getBillingAddress() : "Data not found",
                order.getShippingAddress() != null ? order.getShippingAddress() : "Data not found",
                lineItem.getCreditPeriod() != null ? lineItem.getCreditPeriod().toString() : "Data not found"
        );
    }

    @Override
    public SalesOrder approveSalesOrderAfterCustomerClick(String soNumber) {
        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales order not found"));

        boolean isOverdue = Boolean.TRUE.equals(order.getCustomerOverdue());

        if (isOverdue) {
            order.setStatus("Hold");
        } else {
            order.setStatus("Active");
        }

        order.setApprovalLinkSent(true);
        return salesOrderRepository.save(order);
    }

    @Override
    public SalesOrderLineItemDetailsDto getLineItemDetailsBySoAndLineNumber(String soNumber, String lineNumber) {
        log.info("🔍 Fetching line item details for SO: {}, LineNumber: {}", soNumber, lineNumber);

        SalesOrder order = salesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales order not found with SO Number: " + soNumber));

        SalesOrderLineItem lineItem = order.getItems().stream()
                .filter(item -> item.getLineNumber() != null && item.getLineNumber().equalsIgnoreCase(lineNumber))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Line item not found with Line Number: " + lineNumber));

        log.info("✅ Line item found: ID={}, Description={}, QuantityKg={}, TargetDate={}",
                lineItem.getId(), lineItem.getItemDescription(), lineItem.getQuantityKg(), lineItem.getTargetDispatchDate());

        return SalesOrderLineItemDetailsDto.builder()
                .soNumber(order.getSoNumber())
                .lineNumber(lineItem.getLineNumber())
                .itemDescription(lineItem.getItemDescription())
                .quantityKg(lineItem.getQuantityKg())
                .targetDispatchDate(lineItem.getTargetDispatchDate())
                .build();
    }

    private String serializeJson(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Failed to serialize JSON", e);
            return null;
        }
    }

}



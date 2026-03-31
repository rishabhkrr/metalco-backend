package com.indona.invento.services.impl;

import com.indona.invento.dao.NewSalesOrderRepository;
import com.indona.invento.dao.SoUpdateRepository;
import com.indona.invento.dto.NewSalesOrderDTO;
import com.indona.invento.dto.PackingInstructionDTO;
import com.indona.invento.dto.SalesOrderDTO;
import com.indona.invento.dto.SalesOrderLineItemDTO;
import com.indona.invento.entities.*;
import com.indona.invento.services.NewSalesOrderService;
import com.indona.invento.services.SalesOrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.stream.Collectors;


import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewSalesOrderServiceImpl implements NewSalesOrderService {

    private final NewSalesOrderRepository newSalesOrderRepository;
    private final SoUpdateRepository soUpdateRepository;

    @Autowired
    private final SalesOrderService salesOrderService;




    @Override
    public NewSalesOrder createNewSalesOrder(NewSalesOrderDTO dto) {
        NewSalesOrder order = NewSalesOrder.builder()
                .quotationNo(dto.getQuotationNo())
                .userId(dto.getUserId())
                .unit(dto.getUnit())
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
                .packingRequired(dto.isPackingRequired())
                .creditPeriod(dto.getCreditPeriod())
                .remark(dto.getRemark())
                .acknowledgementSent(dto.isAcknowledgementSent())
                .approvalLinkSent(dto.isApprovalLinkSent())
                .pdfLink(dto.getPdfLink())
                .targetDateOfDispatch(dto.getTargetDateOfDispatch())
                .status(dto.isApprovalLinkSent() ? "Pending for Approval" : "Active")
                .build();

        // 🔁 Map items
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            List<SalesOrderItem> itemEntities = dto.getItems().stream()
                    .map(i -> SalesOrderItem.builder()
                            .orderType(i.getOrderType())
                            .productCategory(i.getProductCategory())
                            .itemDescription(i.getItemDescription())
                            .brand(i.getBrand())
                            .grade(i.getGrade())
                            .temper(i.getTemper())
                            .dimension(i.getDimension())
                            .quantityKg(i.getQuantityKg())
                            .uomKg(i.getUomKg())
                            .quantityNos(i.getQuantityNos())
                            .uomNos(i.getUomNos())
                            .targetDispatchDate(i.getTargetDispatchDate())
                            .orderMode(i.getOrderMode())
                            .creditPeriodDays(i.getCreditPeriodDays())
                            .productionStrategy(i.getProductionStrategy())
                            .currentPrice(i.getCurrentPrice())
                            .salesOrder(order)
                            .build())
                    .toList();

            order.setItems(itemEntities);
        }

        // ✅ Map packing instruction
        if (dto.getPackingInstruction() != null) {
            NewPackingInstruction packingInstruction = NewPackingInstruction.builder()
                    .typeOfPacking(dto.getPackingInstruction().getTypeOfPacking())
                    .weightInstructions(dto.getPackingInstruction().getWeightInstructions())
                    .additionalRemarks(dto.getPackingInstruction().getAdditionalRemarks())
                    .newSalesOrder(order)
                    .build();

            order.setPackingInstruction(packingInstruction);
        }

        NewSalesOrder savedOrder = newSalesOrderRepository.save(order);

        // 🔹 Build SoUpdate
        SoUpdate update = SoUpdate.builder()
                .soNumber(savedOrder.getSoNumber())
                .quotationNo(savedOrder.getQuotationNo())
                .userId(savedOrder.getUserId())
                .unit(savedOrder.getUnit())
                .customerPoNo(savedOrder.getCustomerPoNo())
                .customerCode(savedOrder.getCustomerCode())
                .customerName(savedOrder.getCustomerName())
                .customerPhone(savedOrder.getCustomerPhone())
                .customerEmail(savedOrder.getCustomerEmail())
                .billingAddress(savedOrder.getBillingAddress())
                .shippingAddress(savedOrder.getShippingAddress())
                .marketingExecutiveName(savedOrder.getMarketingExecutiveName())
                .managementAuthority(savedOrder.getManagementAuthority())
                .packingRequired(savedOrder.isPackingRequired())
                .pdfLink(savedOrder.getPdfLink())
                .creditPeriod(savedOrder.getCreditPeriod())
                .status("Pending")
                .build();

        // 🔁 Map items to SoUpdate
        if (savedOrder.getItems() != null && !savedOrder.getItems().isEmpty()) {
            List<SoUpdateItem> updateItems = savedOrder.getItems().stream()
                    .map(i -> SoUpdateItem.builder()
                            .orderType(i.getOrderType())
                            .productCategory(i.getProductCategory())
                            .itemDescription(i.getItemDescription())
                            .brand(i.getBrand())
                            .grade(i.getGrade())
                            .temper(i.getTemper())
                            .dimension(i.getDimension())
                            .quantityKg(i.getQuantityKg())
                            .uomKg(i.getUomKg())
                            .quantityNos(i.getQuantityNos())
                            .uomNos(i.getUomNos())
                            .orderMode(i.getOrderMode())
                            .productionStrategy(i.getProductionStrategy())
                            .currentPrice(i.getCurrentPrice())
                            .creditPeriodDays(i.getCreditPeriodDays())
                            .targetDispatchDate(i.getTargetDispatchDate())
                            .soUpdate(update)
                            .build())
                    .toList();

            update.setItems(updateItems);
        }

        // ✅ Map packing instruction to SoUpdate
        if (savedOrder.getPackingInstruction() != null) {
            SoUpdatePackingInstruction updatePacking = SoUpdatePackingInstruction.builder()
                    .typeOfPacking(savedOrder.getPackingInstruction().getTypeOfPacking())
                    .weightInstructions(savedOrder.getPackingInstruction().getWeightInstructions())
                    .additionalRemarks(savedOrder.getPackingInstruction().getAdditionalRemarks())
                    .soUpdate(update)
                    .build();

            update.setPackingInstruction(updatePacking);
        }

        soUpdateRepository.save(update);

        return savedOrder;
    }

    @Transactional
    @Override
    public NewSalesOrder updateSalesOrder(String soNumber, NewSalesOrderDTO dto) {
        log.info("🔄 Starting updateSalesOrder for soNumber={}", soNumber);

        // 1️⃣ Find existing order
        NewSalesOrder existingOrder = newSalesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> {
                    log.error("❌ Sales Order not found: {}", soNumber);
                    return new RuntimeException("Sales Order not found: " + soNumber);
                });
        log.info("✅ Found existing order with id={}", existingOrder.getId());

        // 2️⃣ Update only non-null fields (Partial Update)
        log.info("✏️ Updating fields for order {} (partial update)", soNumber);

        if (dto.getQuotationNo() != null) existingOrder.setQuotationNo(dto.getQuotationNo());
        if (dto.getUserId() != null) existingOrder.setUserId(dto.getUserId());
        if (dto.getUnit() != null) existingOrder.setUnit(dto.getUnit());
        if (dto.getCustomerPoNo() != null) existingOrder.setCustomerPoNo(dto.getCustomerPoNo());
        if (dto.getCustomerPoFile() != null) existingOrder.setCustomerPoFile(dto.getCustomerPoFile());
        if (dto.getCustomerCode() != null) existingOrder.setCustomerCode(dto.getCustomerCode());
        if (dto.getCustomerName() != null) existingOrder.setCustomerName(dto.getCustomerName());
        if (dto.getCustomerPhone() != null) existingOrder.setCustomerPhone(dto.getCustomerPhone());
        if (dto.getCustomerEmail() != null) existingOrder.setCustomerEmail(dto.getCustomerEmail());
        if (dto.getBillingAddress() != null) existingOrder.setBillingAddress(dto.getBillingAddress());
        if (dto.getShippingAddress() != null) existingOrder.setShippingAddress(dto.getShippingAddress());
        if (dto.getSameAsBillingAddress() != null) existingOrder.setSameAsBillingAddress(dto.getSameAsBillingAddress());
        if (dto.getMarketingExecutiveName() != null) existingOrder.setMarketingExecutiveName(dto.getMarketingExecutiveName());
        if (dto.getManagementAuthority() != null) existingOrder.setManagementAuthority(dto.getManagementAuthority());

        existingOrder.setPackingRequired(dto.isPackingRequired());

        if (dto.getCreditPeriod() != null) existingOrder.setCreditPeriod(dto.getCreditPeriod());
        if (dto.getRemark() != null) existingOrder.setRemark(dto.getRemark());

        existingOrder.setAcknowledgementSent(dto.isAcknowledgementSent());
        existingOrder.setApprovalLinkSent(dto.isApprovalLinkSent());

        if (dto.getPdfLink() != null) existingOrder.setPdfLink(dto.getPdfLink());
        if (dto.getTargetDateOfDispatch() != null) existingOrder.setTargetDateOfDispatch(dto.getTargetDateOfDispatch());

        // Update status based on approval
        existingOrder.setStatus(dto.isApprovalLinkSent() ? "Pending for Approval" : "Active");

        // 3️⃣ Update items only if provided
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            log.info("🔁 Updating items for order {}", soNumber);
            existingOrder.getItems().clear();
            List<SalesOrderItem> updatedItems = dto.getItems().stream()
                    .map(i -> SalesOrderItem.builder()
                            .orderType(i.getOrderType())
                            .productCategory(i.getProductCategory())
                            .itemDescription(i.getItemDescription())
                            .brand(i.getBrand())
                            .grade(i.getGrade())
                            .temper(i.getTemper())
                            .dimension(i.getDimension())
                            .quantityKg(i.getQuantityKg())
                            .uomKg(i.getUomKg())
                            .targetDispatchDate(i.getTargetDispatchDate())
                            .quantityNos(i.getQuantityNos())
                            .uomNos(i.getUomNos())
                            .orderMode(i.getOrderMode())
                            .productionStrategy(i.getProductionStrategy())
                            .currentPrice(i.getCurrentPrice())

                            .salesOrder(existingOrder)
                            .build())
                    .collect(Collectors.toCollection(ArrayList::new));

            existingOrder.getItems().addAll(updatedItems);
            log.info("✅ {} items updated for order {}", updatedItems.size(), soNumber);
        }

        NewSalesOrder savedOrder = newSalesOrderRepository.save(existingOrder);
        log.info("💾 Saved updated NewSalesOrder with id={}", savedOrder.getId());

        // 3.5️⃣ Update packing instruction only if provided
        if (dto.getPackingInstruction() != null) {
            log.info("📦 Updating packing instruction for order {}", soNumber);
            NewPackingInstruction packingInst = savedOrder.getPackingInstruction();
            if (packingInst == null) {
                packingInst = new NewPackingInstruction();
                packingInst.setNewSalesOrder(savedOrder);
            }

            if (dto.getPackingInstruction().getTypeOfPacking() != null) {
                packingInst.setTypeOfPacking(dto.getPackingInstruction().getTypeOfPacking());
            }
            if (dto.getPackingInstruction().getWeightInstructions() != null) {
                packingInst.setWeightInstructions(dto.getPackingInstruction().getWeightInstructions());
            }
            if (dto.getPackingInstruction().getAdditionalRemarks() != null) {
                packingInst.setAdditionalRemarks(dto.getPackingInstruction().getAdditionalRemarks());
            }

            savedOrder.setPackingInstruction(packingInst);
            newSalesOrderRepository.save(savedOrder);
            log.info("✅ Packing instruction updated for order {}", soNumber);
        }

        // 4️⃣ Update SoUpdate table
        log.info("🔁 Updating SoUpdate for soNumber={}", soNumber);
        SoUpdate update = soUpdateRepository.findBySoNumber(soNumber)
                .orElse(SoUpdate.builder().soNumber(soNumber).build());

        update.setQuotationNo(savedOrder.getQuotationNo());
        update.setUserId(savedOrder.getUserId());
        update.setUnit(savedOrder.getUnit());
        update.setCustomerPoNo(savedOrder.getCustomerPoNo());
        update.setCustomerCode(savedOrder.getCustomerCode());
        update.setCustomerName(savedOrder.getCustomerName());
        update.setCustomerPhone(savedOrder.getCustomerPhone());
        update.setCustomerEmail(savedOrder.getCustomerEmail());
        update.setBillingAddress(savedOrder.getBillingAddress());
        update.setShippingAddress(savedOrder.getShippingAddress());
        update.setRemark(savedOrder.getRemark());
        update.setMarketingExecutiveName(savedOrder.getMarketingExecutiveName());
        update.setManagementAuthority(savedOrder.getManagementAuthority());
        update.setPackingRequired(savedOrder.isPackingRequired());
        update.setCreditPeriod(savedOrder.getCreditPeriod());
        update.setStatus("Verified");

        update.getItems().clear();
        if (savedOrder.getItems() != null && !savedOrder.getItems().isEmpty()) {
            List<SoUpdateItem> updateItems = savedOrder.getItems().stream()
                    .map(i -> SoUpdateItem.builder()
                            .orderType(i.getOrderType())
                            .productCategory(i.getProductCategory())
                            .itemDescription(i.getItemDescription())
                            .brand(i.getBrand())
                            .grade(i.getGrade())
                            .temper(i.getTemper())
                            .dimension(i.getDimension())
                            .quantityKg(i.getQuantityKg())
                            .uomKg(i.getUomKg())
                            .quantityNos(i.getQuantityNos())
                            .uomNos(i.getUomNos())
                            .targetDispatchDate(i.getTargetDispatchDate())
                            .orderMode(i.getOrderMode())
                            .productionStrategy(i.getProductionStrategy())
                            .currentPrice(i.getCurrentPrice())
                            .soUpdate(update)
                            .build())
                    .collect(Collectors.toCollection(ArrayList::new));

            update.getItems().addAll(updateItems);
            log.info("✅ {} items updated in SoUpdate for soNumber={}", updateItems.size(), soNumber);
        }

        soUpdateRepository.save(update);
        log.info("💾 SoUpdate saved for soNumber={}", soNumber);

        // 5️⃣ Convert NewSalesOrder → SalesOrderDTO
        log.info("🔄 Converting NewSalesOrder to SalesOrderDTO for soNumber={}", soNumber);
        SalesOrderDTO salesOrderDTO = convertToSalesOrderDTO(savedOrder);

        // 6️⃣ Call existing SalesOrder flow
        log.info("🚀 Calling SalesOrderService.createSalesOrder for soNumber={}", soNumber);
        SalesOrder newSalesOrder = salesOrderService.createSalesOrder(salesOrderDTO);
        salesOrderService.storePurchaseFollowUpsV2(newSalesOrder);
        log.info("🎉 SalesOrder flow completed for soNumber={}", newSalesOrder.getSoNumber());

        return savedOrder;
    }


        // ---------------- HELPER METHOD ----------------
        private SalesOrderDTO convertToSalesOrderDTO(NewSalesOrder order) {
            return SalesOrderDTO.builder()
                    .quotationNo(order.getQuotationNo())
                    .userId(order.getUserId())
                    .unit(order.getUnit())
                    .unitCode(order.getUnit()) // adjust if you have separate unitCode field
                    .customerPoNo(order.getCustomerPoNo())
                    .customerPoFile(order.getCustomerPoFile())
                    .customerCode(order.getCustomerCode())
                    .customerName(order.getCustomerName())
                    .customerPhone(order.getCustomerPhone())
                    .customerEmail(order.getCustomerEmail())
                    .billingAddress(order.getBillingAddress())
                    .shippingAddress(order.getShippingAddress())
                    .marketingExecutiveName(order.getMarketingExecutiveName())
                    .managementAuthority(order.getManagementAuthority())
                    .packingRequired(order.isPackingRequired())
                    .acknowledgementSent(order.isAcknowledgementSent())
                    .approvalLinkSent(order.isApprovalLinkSent())
                    .status(order.getStatus())
                    .customerOverdue(false) // set as per your business logic
                    // ✅ PackingInstruction mapping
                    .packingInstruction(order.getPackingInstruction() != null ? PackingInstructionDTO.builder()
                        //    .id(order.getPackingInstruction().getId())
                            .typeOfPacking(order.getPackingInstruction().getTypeOfPacking())
                            .weightInstructions(order.getPackingInstruction().getWeightInstructions())
                            .additionalRemarks(order.getPackingInstruction().getAdditionalRemarks())
                            .build() : null)
                    .items(order.getItems().stream()
                            .map(i -> SalesOrderLineItemDTO.builder()
                                    .orderType(i.getOrderType())
                                    .productCategory(i.getProductCategory())
                                    .itemDescription(i.getItemDescription())
                                    .brand(i.getBrand())
                                    .grade(i.getGrade())
                                    .temper(i.getTemper())
                                    .dimension(i.getDimension())
                                    .quantityKg(Double.valueOf(i.getQuantityKg()))
                                    .uomKg(i.getUomKg())
                                    .quantityNos(Double.valueOf(i.getQuantityNos()))
                                    .uomNos(i.getUomNos())
                                    .orderMode(i.getOrderMode())
                                    .productionStrategy(i.getProductionStrategy())
                                    .currentPrice(i.getCurrentPrice())
                                    .targetDispatchDate(i.getTargetDispatchDate())
                                    .build())
                            .toList())
                    .build();
        }

    @Override
    public NewSalesOrder getSalesOrderBySoNumber(String soNumber) {
        return newSalesOrderRepository.findBySoNumber(soNumber)
                .orElseThrow(() -> new RuntimeException("Sales Order not found with SO Number: " + soNumber));
    }
    }

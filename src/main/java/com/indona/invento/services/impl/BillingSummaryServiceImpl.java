package com.indona.invento.services.impl;


import com.indona.invento.dao.*;
import com.indona.invento.dao.BillingSummaryRepository;
import com.indona.invento.dao.PackingSubmissionRepository;
import com.indona.invento.dto.BillLineDetailsDTO;
import com.indona.invento.dto.BillingSummaryDTO;
import com.indona.invento.entities.*;
import com.indona.invento.services.BillingSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class BillingSummaryServiceImpl implements BillingSummaryService {

    @Autowired
    private BillingSummaryRepository repository;

    @Autowired
    private PackingSubmissionRepository packingSubmissionRepository;

    @Autowired
    private SoSummaryRepository soSummaryRepository;

    @Autowired
    private SalesmanMasterRepository salesmanMasterRepository;

    @Autowired
    private SalesmanIncentiveEntryRepository incentiveEntryRepository;

    @Autowired
    private UserMasterRepository userMasterRepository;

    @Override
    public BillingSummaryEntity saveBilling(BillingSummaryDTO dto) {
        log.info("🚀 Saving BillingSummary for SO: {}, Line: {}", dto.getSoNumber(), dto.getLineNumber());

        // 1️⃣ Save BillingSummary
        BillingSummaryEntity entity = BillingSummaryEntity.builder()
                .timestamp(dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now())
                .invoiceNumber(dto.getInvoiceNumber())
                .soNumber(dto.getSoNumber())
                .lineNumber(dto.getLineNumber())
                .unit(dto.getUnit())
                .customerCode(dto.getCustomerCode())
                .customerName(dto.getCustomerName())
                .customerBillingAddress(dto.getCustomerBillingAddress())
                .customerShippingAddress(dto.getCustomerShippingAddress())
                .packingStatus(dto.getPackingStatus())
                .orderType(dto.getOrderType())
                .productCategory(dto.getProductCategory())
                .itemDescription(dto.getItemDescription())
                .brand(dto.getBrand())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .price(dto.getPrice())
                .dimension(dto.getDimension())
                .quantityKg(dto.getQuantityKg())
                .uomKg(dto.getUomKg())
                .quantityNo(dto.getQuantityNo())
                .uomNo(dto.getUomNo())
                .itemPrice(dto.getItemPrice())
                .amount(dto.getAmount())
                .cgst(dto.getCgst())
                .sgst(dto.getSgst())
                .igst(dto.getIgst())
                .transportationCharges(dto.getTransportationCharges())
                .packingCharges(dto.getPackingCharges())
                .cuttingCharges(dto.getCuttingCharges())
                .laminationCharges(dto.getLaminationCharges())
                .totalAmount(dto.getTotalAmount())
//                .billingStatus(dto.getBillingStatus())
                .billingStatus("COMPLETED")
                .creditPeriodDays(dto.getCreditPeriodDays())
                .coc(dto.getCoc())
                .build();

        BillingSummaryEntity saved = repository.save(entity);

        // 2️⃣ Fetch SoSummaryEntity by soNumber
        SoSummaryEntity summary = soSummaryRepository.findBySoNumber(dto.getSoNumber());
        if (summary != null && summary.getItems() != null) {
            SoSummaryItemEntity item = summary.getItems().stream()
                    .filter(i -> i.getLineNumber().equals(dto.getLineNumber()))
                    .findFirst()
                    .orElse(null);

            if (item != null) {
                item.setDispatchQuantityKg(dto.getQuantityKg());
                item.setDispatchQuantityNo(dto.getQuantityNo());
                item.setAmount(dto.getAmount());
                item.setTotalAmount(dto.getTotalAmount());
                item.setInvoiceNumber(dto.getInvoiceNumber());
                item.setPrice(dto.getPrice());
                item.setLrNumberUpdation(""); // blank
                item.setSoStatus("Closed");

                // ✅ Set dispatchDate from billing timestamp
                LocalDate dispatchDate = (dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now()).toLocalDate();
                item.setDispatchDate(dispatchDate);

                soSummaryRepository.save(summary); // cascade saves item
                log.info("✅ SO Summary updated for SO: {}, Line: {}", dto.getSoNumber(), dto.getLineNumber());

                if ("Closed".equalsIgnoreCase(item.getSoStatus())) {
                    generateSalesmanIncentive(dto, summary, item);
                }
            }
            else {
                log.warn("⚠️ LineNumber not found in SO Summary: {}", dto.getLineNumber());
            }
        } else {
            log.warn("⚠️ SO Summary not found for SO Number: {}", dto.getSoNumber());
        }

        return saved;
    }

    @Override
    public List<BillingSummaryEntity> getAllBillings() {
        return repository.findAll();
    }

    @Override
    public PackingSubmission getPackingDetailsBySoAndLine(String soNumber, String lineNumber) {
        return packingSubmissionRepository.findBySoNumberAndLineNumber(soNumber, lineNumber);
    }

    @Override
    public void deleteAllBillings() {
        repository.deleteAll();
    }

    private void generateSalesmanIncentive(BillingSummaryDTO dto, SoSummaryEntity summary, SoSummaryItemEntity item) {
        LocalDate dispatchDate = item.getDispatchDate();
        Integer creditDays = item.getCreditDays();

        LocalDate targetDate = null;
        if (dispatchDate != null && creditDays != null) {
            targetDate = dispatchDate.plusDays(creditDays);
        } else {
            log.warn("⚠️ Dispatch date or credit days is null for SO: {}, Line: {} — setting targetDateOfPayment as null",
                    dto.getSoNumber(), dto.getLineNumber());
        }

        // ✅ Normalize grade + temper
        String rawGrade = item.getGrade() != null ? item.getGrade() : "";
        String rawTemper = item.getTemper() != null ? item.getTemper() : "";
        String gradeTemperKey = (rawGrade + rawTemper).replaceAll("[\\s\\-]", "").toUpperCase();

        log.info("🔍 Looking for incentive config for normalized GradeTemper: {}", gradeTemperKey);

        // ✅ Step 1: Find UserMaster by marketingExecutiveName (case-insensitive)
        Optional<UserMasterEntity> userOpt = userMasterRepository.findByUserNameIgnoreCase(summary.getMarketingExecutiveName());

        SalesmanMasterEntity salesman = null;
        if (userOpt.isPresent()) {
            String userId = userOpt.get().getUserId();
            // ✅ Step 2: Find SalesmanMaster by userId
            salesman = salesmanMasterRepository.findByUserId(userId);
        }

        if (salesman == null) {
            log.warn("⚠️ No SalesmanMaster found for marketingExecutiveName={} (resolved userId={})",
                    summary.getMarketingExecutiveName(),
                    userOpt.map(UserMasterEntity::getUserId).orElse("N/A"));
            return;
        }

        // ✅ Step 3: Find incentive rate config
        SalesmanIncentiveEntity rateConfig = salesman.getIncentiveRates().stream()
                .filter(r -> {
                    String configKey = r.getMaterialGradeAndTemper() != null
                            ? r.getMaterialGradeAndTemper().replaceAll("[\\s\\-]", "").toUpperCase()
                            : "";
                    return configKey.equals(gradeTemperKey);
                })
                .findFirst()
                .orElse(null);

        if (rateConfig == null) {
            log.warn("⚠️ No incentive rate config found for GradeTemper: {}", gradeTemperKey);
        }

        BigDecimal incentiveRate = rateConfig != null ? BigDecimal.valueOf(rateConfig.getRatePerKg()) : BigDecimal.ZERO;
        BigDecimal lapseInterestRate = rateConfig != null ? BigDecimal.valueOf(rateConfig.getLapseInterestRate()) : BigDecimal.ZERO;

        log.info("📊 Calculated incentiveRate: {}, lapseInterestRate: {}", incentiveRate, lapseInterestRate);

        // ✅ Calculate incentive amount (rate × dispatch quantity)
        BigDecimal dispatchQty = dto.getQuantityKg() != null ? dto.getQuantityKg() : BigDecimal.ZERO;
        BigDecimal incentiveAmount = incentiveRate.multiply(dispatchQty);

        SalesmanIncentiveEntryEntity incentive = SalesmanIncentiveEntryEntity.builder()
                .marketingExecutiveName(summary.getMarketingExecutiveName())
                .userId(summary.getUserId())
                .unit(summary.getUnit())
                .customerCode(summary.getCustomerCode())
                .customerName(summary.getCustomerName())
                .soNumber(summary.getSoNumber())
                .lineNumber(item.getLineNumber())
                .orderType(item.getOrderType())
                .productCategory(item.getProductCategory())
                .itemDescription(item.getItemDescription())
                .grade(item.getGrade())
                .temper(item.getTemper())
                .dimension(item.getDimension())
                .orderQuantityKg(item.getOrderQuantityKg())
                .uomKg(item.getUomKg())
                .orderQuantityNo(item.getOrderQuantityNo())
                .uomNo(item.getUomNo())
                .dispatchDate(dispatchDate)
                .dispatchQuantityKg(dispatchQty)
                .dispatchQuantityNo(dto.getQuantityNo())
                .amount(dto.getAmount())
                .totalAmount(dto.getTotalAmount())
                .creditDays(creditDays)
                .targetDateOfPayment(targetDate)
                .incentiveRate(incentiveRate)
                .incentiveAmount(incentiveAmount)
                .paymentStatus("Pending")
                .lapseInterestRate(lapseInterestRate)
                .lapseInterestAmount(BigDecimal.ZERO)
                .finalIncentiveAmount(BigDecimal.ZERO)
                .amountReceived(null)
                .dateOfPayment(null)
                .numberOfDaysLapse(0)
                .build();

        log.info("💾 Saving Salesman Incentive entry for SO: {}, Line: {}", dto.getSoNumber(), dto.getLineNumber());
        SalesmanIncentiveEntryEntity saved = incentiveEntryRepository.save(incentive);
        log.info("✅ Saved Incentive Entry with ID: {}", saved.getId());
    }


    @Override
    public List<String> getCompletedInvoiceNumbers() {
        return repository.findDistinctInvoiceNumbersByBillingStatusCompleted();
    }

    @Override
    public List<String> getSoNumbersByInvoice(String invoiceNumber) {
        return repository.findDistinctSoNumbersByInvoiceNumber(invoiceNumber);
    }

    @Override
    public List<String> getLineNumbersByInvoiceAndSo(String invoiceNumber, String soNumber) {
        return repository.findDistinctLineNumbersByInvoiceAndSo(invoiceNumber, soNumber);
    }

    @Override
    public BillLineDetailsDTO getLineDetails(String invoiceNumber, String soNumber, String lineNumber) {
        List<BillingSummaryEntity> entities = repository.findByInvoiceNumberAndSoNumberAndLineNumber(
                invoiceNumber, soNumber, lineNumber);

        if (entities == null || entities.isEmpty()) {
            throw new RuntimeException("No billing record found for invoice: " + invoiceNumber +
                    ", SO: " + soNumber + ", Line: " + lineNumber);
        }

        // Use the first entity (all duplicates should have the same line details)
        BillingSummaryEntity entity = entities.get(0);

        return BillLineDetailsDTO.builder()
                .unit(entity.getUnit())
                .customerCode(entity.getCustomerCode())
                .customerName(entity.getCustomerName())
                .customerBillingAddress(entity.getCustomerBillingAddress())
                .customerShippingAddress(entity.getCustomerShippingAddress())
                .orderType(entity.getOrderType())
                .productCategory(entity.getProductCategory())
                .itemDescription(entity.getItemDescription())
                .brand(entity.getBrand())
                .grade(entity.getGrade())
                .temper(entity.getTemper())
                .dimension(entity.getDimension())
                .uomKg(entity.getUomKg())
                .uomNo(entity.getUomNo())
                // itemPrice is NOT auto-filled - it's a user entry field
                .build();
    }

}

package com.indona.invento.services.impl;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.indona.invento.dao.*;
import com.indona.invento.dto.BillLineDetailsDTO;
import com.indona.invento.dto.BillingSummaryDTO;
import com.indona.invento.entities.*;
import com.indona.invento.services.BillingSummaryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

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
                .hamaliCharges(dto.getHamaliCharges())
                .totalAmount(dto.getTotalAmount())
                .billingStatus("COMPLETED")
                .creditPeriodDays(dto.getCreditPeriodDays())
                .coc(dto.getCoc())
                // FRD v3.0 new fields
                .rfdListNumber(dto.getRfdListNumber())
                .eWaybillNumber(dto.getEWaybillNumber())
                .build();

        BillingSummaryEntity saved = repository.save(entity);

        // 2️⃣ Fetch SoSummaryEntity by soNumber and update dispatch data
        updateSoSummary(dto, saved);

        return saved;
    }

    private void updateSoSummary(BillingSummaryDTO dto, BillingSummaryEntity saved) {
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

                // Set dispatchDate from billing timestamp
                LocalDate dispatchDate = (dto.getTimestamp() != null ? dto.getTimestamp() : LocalDateTime.now()).toLocalDate();
                item.setDispatchDate(dispatchDate);

                soSummaryRepository.save(summary);
                log.info("✅ SO Summary updated for SO: {}, Line: {}", dto.getSoNumber(), dto.getLineNumber());

                if ("Closed".equalsIgnoreCase(item.getSoStatus())) {
                    generateSalesmanIncentive(dto, summary, item);
                }
            } else {
                log.warn("⚠️ LineNumber not found in SO Summary: {}", dto.getLineNumber());
            }
        } else {
            log.warn("⚠️ SO Summary not found for SO Number: {}", dto.getSoNumber());
        }
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

    // ==================== FRD v3.0 New Implementations ====================

    @Override
    public BillingSummaryEntity getBillingById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public BillingSummaryEntity updateBilling(Long id, BillingSummaryDTO dto) {
        BillingSummaryEntity existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing Summary not found with ID: " + id));

        // Update fields from DTO
        if (dto.getInvoiceNumber() != null) existing.setInvoiceNumber(dto.getInvoiceNumber());
        if (dto.getEWaybillNumber() != null) existing.setEWaybillNumber(dto.getEWaybillNumber());
        if (dto.getRfdListNumber() != null) existing.setRfdListNumber(dto.getRfdListNumber());
        if (dto.getAmount() != null) existing.setAmount(dto.getAmount());
        if (dto.getCgst() != null) existing.setCgst(dto.getCgst());
        if (dto.getSgst() != null) existing.setSgst(dto.getSgst());
        if (dto.getIgst() != null) existing.setIgst(dto.getIgst());
        if (dto.getTransportationCharges() != null) existing.setTransportationCharges(dto.getTransportationCharges());
        if (dto.getPackingCharges() != null) existing.setPackingCharges(dto.getPackingCharges());
        if (dto.getCuttingCharges() != null) existing.setCuttingCharges(dto.getCuttingCharges());
        if (dto.getLaminationCharges() != null) existing.setLaminationCharges(dto.getLaminationCharges());
        if (dto.getHamaliCharges() != null) existing.setHamaliCharges(dto.getHamaliCharges());
        if (dto.getTotalAmount() != null) existing.setTotalAmount(dto.getTotalAmount());
        if (dto.getCreditPeriodDays() != null) existing.setCreditPeriodDays(dto.getCreditPeriodDays());

        return repository.save(existing);
    }

    @Override
    public List<String> getAvailableRfdListNumbers() {
        // BS-BR-001: Return RFD List Numbers NOT already in billing_summary
        List<String> usedNumbers = repository.findDistinctUsedRfdListNumbers();
        // TODO: Replace with actual RFD List Transfer query when that entity exists
        // For now, return used list for frontend to filter
        log.info("📋 Used RFD List Numbers: {}", usedNumbers);
        return usedNumbers;
    }

    @Override
    public BillingSummaryEntity updateLrNumber(Long id, String lrNumber) {
        BillingSummaryEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing Summary not found with ID: " + id));

        entity.setLrNumber(lrNumber);
        BillingSummaryEntity saved = repository.save(entity);

        // BS-BR-008: Auto-populate SO Summary with LR Number
        if (entity.getInvoiceNumber() != null) {
            List<BillingSummaryEntity> records = repository.findByInvoiceNumber(entity.getInvoiceNumber());
            for (BillingSummaryEntity record : records) {
                if (record.getSoNumber() != null) {
                    SoSummaryEntity soSummary = soSummaryRepository.findBySoNumber(record.getSoNumber());
                    if (soSummary != null && soSummary.getItems() != null) {
                        soSummary.getItems().stream()
                                .filter(item -> record.getLineNumber() != null
                                        && record.getLineNumber().equals(item.getLineNumber()))
                                .forEach(item -> {
                                    item.setLrNumberUpdation(lrNumber);
                                    log.info("✅ LR Number '{}' propagated to SO Summary SO: {}, Line: {}",
                                            lrNumber, record.getSoNumber(), record.getLineNumber());
                                });
                        soSummaryRepository.save(soSummary);
                    }
                }
            }
        }

        return saved;
    }

    @Override
    public BillingSummaryEntity uploadInvoicePdf(Long id, MultipartFile file) {
        BillingSummaryEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Billing Summary not found with ID: " + id));

        try {
            // Upload to Azure Blob Storage
            String filename = "Invoice_" + entity.getInvoiceNumber() + "_" + UUID.randomUUID() + ".pdf";

            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(azureStorageConnectionString)
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("invoices");
            if (!containerClient.exists()) {
                containerClient.create();
                containerClient.setAccessPolicy(com.azure.storage.blob.models.PublicAccessType.BLOB, null);
            }

            BlobClient blobClient = containerClient.getBlobClient(filename);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(file.getBytes());

            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType("application/pdf")
                    .setContentDisposition("attachment; filename=\"" + filename + "\"");

            blobClient.upload(inputStream, file.getSize(), true);
            blobClient.setHttpHeaders(headers);

            String pdfUrl = blobClient.getBlobUrl();
            entity.setInvoicePdfUrl(pdfUrl);

            log.info("✅ Invoice PDF uploaded: {}", pdfUrl);
            return repository.save(entity);
        } catch (Exception e) {
            log.error("❌ Failed to upload invoice PDF", e);
            throw new RuntimeException("Failed to upload invoice: " + e.getMessage());
        }
    }

    @Override
    public List<String> getInvoicesByUnit(String unit) {
        return repository.findDistinctInvoiceNumbersByUnit(unit);
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
                .build();
    }

    // ==================== Salesman Incentive Logic (existing) ====================

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

        String rawGrade = item.getGrade() != null ? item.getGrade() : "";
        String rawTemper = item.getTemper() != null ? item.getTemper() : "";
        String gradeTemperKey = (rawGrade + rawTemper).replaceAll("[\\s\\-]", "").toUpperCase();

        log.info("🔍 Looking for incentive config for normalized GradeTemper: {}", gradeTemperKey);

        Optional<UserMasterEntity> userOpt = userMasterRepository.findByUserNameIgnoreCase(summary.getMarketingExecutiveName());

        SalesmanMasterEntity salesman = null;
        if (userOpt.isPresent()) {
            String userId = userOpt.get().getUserId();
            salesman = salesmanMasterRepository.findByUserId(userId);
        }

        if (salesman == null) {
            log.warn("⚠️ No SalesmanMaster found for marketingExecutiveName={} (resolved userId={})",
                    summary.getMarketingExecutiveName(),
                    userOpt.map(UserMasterEntity::getUserId).orElse("N/A"));
            return;
        }

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
}

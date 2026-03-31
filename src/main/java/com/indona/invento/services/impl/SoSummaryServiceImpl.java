package com.indona.invento.services.impl;

import com.indona.invento.dao.DeliveryChallanJWRepository;
import com.indona.invento.dao.GRNJobWorkRepository;
import com.indona.invento.dao.SoSummaryRepository;
import com.indona.invento.dto.JobworkBatchDetailDTO;
import com.indona.invento.dto.JobworkMergedDTO;
import com.indona.invento.dto.SoSummaryDTO;
import com.indona.invento.entities.DeliveryChallanJWEntity;
import com.indona.invento.entities.GRNJobWorkEntity;
import com.indona.invento.entities.SoSummaryEntity;
import com.indona.invento.entities.SoSummaryItemEntity;
import com.indona.invento.services.SoSummaryService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SoSummaryServiceImpl implements SoSummaryService {

    @Autowired
    private SoSummaryRepository repository;
    
    @Autowired
    private DeliveryChallanJWRepository deliveryChallanJWRepository;
    
    @Autowired
    private GRNJobWorkRepository grnJobWorkRepository;

    @Override
    public SoSummaryEntity saveSummary(SoSummaryDTO dto) {
        SoSummaryEntity entity = SoSummaryEntity.builder()
                .userId(dto.getUserId())
                .timestamp(dto.getTimestamp())
                .quotationNo(dto.getQuotationNo())
                .soNumber(dto.getSoNumber())
                .unit(dto.getUnit())
                .customerPoNo(dto.getCustomerPoNo())
                .customerCode(dto.getCustomerCode())
                .customerName(dto.getCustomerName())
                .customerPhoneNo(dto.getCustomerPhoneNo())
                .customerEmail(dto.getCustomerEmail())
                .marketingExecutiveName(dto.getMarketingExecutiveName())
                .managementAuthority(dto.getManagementAuthority())
                .packingStatus(dto.getPackingStatus())
                .build();

        List<SoSummaryItemEntity> items = dto.getItems().stream()
                .map(i -> SoSummaryItemEntity.builder()
                        .lineNumber(i.getLineNumber())
                        .orderType(i.getOrderType())
                        .productCategory(i.getProductCategory())
                        .itemDescription(i.getItemDescription())
                        .brand(i.getBrand())
                        .grade(i.getGrade())
                        .temper(i.getTemper())
                        .dimension(i.getDimension())
                        .orderQuantityKg(i.getOrderQuantityKg())
                        .uomKg(i.getUomKg())
                        .orderQuantityNo(i.getOrderQuantityNo())
                        .uomNo(i.getUomNo())
                        .productionStrategy(i.getProductionStrategy())
                        .creditDays(i.getCreditDays())
                        .targetDispatchDate(i.getTargetDispatchDate())
                        .dispatchDate(i.getDispatchDate())
                        .dispatchQuantityKg(i.getDispatchQuantityKg())
                        .dispatchQuantityNo(i.getDispatchQuantityNo())
                        .amount(i.getAmount())
                        .totalAmount(i.getTotalAmount())
                        .invoiceNumber(i.getInvoiceNumber())
                        .lrNumberUpdation(i.getLrNumberUpdation())
                        .soStatus(i.getSoStatus())
                        .summary(entity)
                        .build())
                .toList();

        entity.setItems(items);
        return repository.save(entity);
    }

    @Override
    public List<SoSummaryEntity> getAllSummaries() {
        return repository.findAll();
    }

    @Override
    public int updateLrNumber(String soNumber, String lineNumber, String lrNumberUpdation) {
        return repository.updateLrNumber(soNumber, lineNumber, lrNumberUpdation);
    }
    
    @Override
    public List<JobworkMergedDTO> getJobworkMergedData() {

        List<SoSummaryEntity> allSummaries = repository.findAll();
        List<JobworkMergedDTO> mergedData = new ArrayList<>();

        for (SoSummaryEntity summary : allSummaries) {
            for (SoSummaryItemEntity item : summary.getItems()) {

                if ("JOBWORK".equalsIgnoreCase(item.getProductionStrategy())) {

                    List<DeliveryChallanJWEntity> challans =
                            deliveryChallanJWRepository.findBySoNumberAndLineNumber(
                                    summary.getSoNumber(),
                                    item.getLineNumber()
                            );

                    DeliveryChallanJWEntity challan = challans.isEmpty()
                            ? null
                            : challans.get(challans.size() - 1);

                    JobworkMergedDTO dto = new JobworkMergedDTO();

                    dto.setTimestamp(summary.getTimestamp());
                    dto.setSoNumber(summary.getSoNumber());
                    dto.setLineNumber(item.getLineNumber());
                    dto.setUnit(summary.getUnit());
                    dto.setCustomerCode(summary.getCustomerCode());
                    dto.setCustomerName(summary.getCustomerName());

                    dto.setOrderType(item.getOrderType());
                    dto.setProductCategory(item.getProductCategory());
                    dto.setItemDescription(item.getItemDescription());
                    dto.setBrand(item.getBrand());
                    dto.setGrade(item.getGrade());
                    dto.setTemper(item.getTemper());
                    dto.setDimension(item.getDimension());
                    dto.setProductionStrategy(item.getProductionStrategy());

                    dto.setUomKg(item.getUomKg());
                    dto.setUomNo(item.getUomNo());
                    dto.setSoStatus(item.getSoStatus());

                    // ====== DELIVERY CHALLAN DATA ======
                    if (challan != null) {
                        dto.setMedcNumber(challan.getMedcNumber());
                        dto.setPackingListNumber(challan.getPackingListNumber());
                        dto.setSubContractorCode(challan.getSubContractorCode());
                        dto.setSubContractorName(challan.getSubContractorName());
                        dto.setSubContractorBillingAddress(challan.getSubContractorBillingAddress());
                        dto.setSubContractorShippingAddress(challan.getSubContractorShippingAddress());
                        dto.setPackingStatus(challan.getPackingStatus());
                        dto.setQuantityKg(challan.getQuantityKg());
                        dto.setQuantityNo(challan.getQuantityNo());
                    }

                    // ====== FETCH ALL GRN JOBWORK RECORDS FOR BATCH DETAILS ======
                    List<GRNJobWorkEntity> grns = new ArrayList<>();
                    if (challan != null && challan.getMedcNumber() != null) {
                        grns = grnJobWorkRepository.findAllByMedcNumber(challan.getMedcNumber());
                    }

                    // ====== BUILD BATCH DETAILS LIST ======
                    List<JobworkBatchDetailDTO> batchDetails = new ArrayList<>();
                    Double totalReceivedKg = 0.0;
                    Integer totalReceivedNos = 0;
                    Double totalScrapKg = 0.0;
                    Integer totalScrapNo = 0;

                    for (GRNJobWorkEntity grn : grns) {
                        JobworkBatchDetailDTO batch = new JobworkBatchDetailDTO();
                        batch.setItemDescription(grn.getItemDescription());
                        batch.setItemDimension(grn.getDimension());
                        batch.setBatchNumber(grn.getGrnRefNumber());
                        batch.setDateOfInward(grn.getTimestamp());

                        // Sent quantities from Delivery Challan
                        if (challan != null) {
                            batch.setSentQuantityKg(challan.getQuantityKg() != null 
                                ? challan.getQuantityKg().doubleValue() : null);
                            batch.setSentQuantityNo(challan.getQuantityNo());
                            batch.setItemPriceMedc(challan.getItemPrice());
                        }

                        // Received and Scrap quantities from GRN
                        batch.setReceivedQuantityKg(grn.getReceivedQuantityKg());
                        batch.setReceivedQuantityNo(grn.getReceivedQuantityNos());
                        batch.setScrapQuantityKg(grn.getScrapQuantityKg());
                        // scrapQuantityNo not in GRN entity, set null
                        batch.setScrapQuantityNo(null);

                        batchDetails.add(batch);

                        // Accumulate totals for summary row
                        if (grn.getReceivedQuantityKg() != null) totalReceivedKg += grn.getReceivedQuantityKg();
                        if (grn.getReceivedQuantityNos() != null) totalReceivedNos += grn.getReceivedQuantityNos();
                        if (grn.getScrapQuantityKg() != null) totalScrapKg += grn.getScrapQuantityKg();
                    }

                    dto.setBatchDetails(batchDetails);

                    // ====== MAP SUMMARY GRN FIELDS (from latest GRN) ======
                    if (!grns.isEmpty()) {
                        GRNJobWorkEntity latestGrn = grns.get(grns.size() - 1);
                        dto.setGrnDimension(latestGrn.getGrnDimension());
                        dto.setGrnUomKg(latestGrn.getGrnUomKg());
                        dto.setGrnUomNo(latestGrn.getGrnUomNo());
                        dto.setJobworkRate(latestGrn.getJobworkRate());
                        dto.setJobworkValue(latestGrn.getJobworkValue());
                    }

                    // Set accumulated totals
                    dto.setReceivedQuantityKg(totalReceivedKg);
                    dto.setReceivedQuantityNos(totalReceivedNos > 0 ? totalReceivedNos : null);
                    dto.setScrapQuantityKg(totalScrapKg);
                    dto.setScrapQuantityNo(totalScrapNo > 0 ? totalScrapNo : null);

                    // ====== JOB WORK STATUS LOGIC (per FRD) ======
                    String dispatchStatus = item.getSoStatus();
                    if ("Dispatched".equalsIgnoreCase(dispatchStatus) || "Closed".equalsIgnoreCase(dispatchStatus)) {
                        dto.setJobWorkStatus("DISPATCHED");
                    } else if (!grns.isEmpty()) {
                        dto.setJobWorkStatus("READY FOR DISPATCH");
                    } else if (challan != null) {
                        dto.setJobWorkStatus("SUB-CONTRACTOR");
                    } else if ("Active".equalsIgnoreCase(dispatchStatus)) {
                        dto.setJobWorkStatus("PENDING");
                    } else {
                        dto.setJobWorkStatus("PENDING");
                    }

                    mergedData.add(dto);
                }
            }
        }

        return mergedData;
    }



    @Override
    public void deleteAllSummaries() {
        repository.deleteAll();
    }
}

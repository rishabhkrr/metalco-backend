package com.indona.invento.services.impl;

import com.indona.invento.dao.GRNJobWorkRepository;
import com.indona.invento.dao.GateInwardRepository;
import com.indona.invento.entities.GRNJobWorkEntity;
import com.indona.invento.entities.GateInwardEntity;
import com.indona.invento.services.GRNJobWorkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GRNJobWorkServiceImpl implements GRNJobWorkService {

    private final GRNJobWorkRepository repository;
    
    private final GateInwardRepository gateInwardRepo;

    @Override
    @Transactional
    public List<GRNJobWorkEntity> saveAll(List<GRNJobWorkEntity> entities) {
        log.info("Saving {} GRN Jobwork records", entities.size());
        entities.forEach(this::generateGRNRefNumber);
        entities.forEach(e -> e.setTimestamp(LocalDateTime.now()));
        return repository.saveAll(entities);
    }

    @Override
    public List<GRNJobWorkEntity> getAll() {
        log.info("Fetching all GRN Jobwork records");
        return repository.findAll();
    }

    @Override
    @Transactional
    public GRNJobWorkEntity updateById(Long id, GRNJobWorkEntity updatedEntity) {
        log.info("Updating GRN Jobwork record with id: {}", id);
        Optional<GRNJobWorkEntity> optional = repository.findById(id);

        if (optional.isEmpty()) {
            throw new RuntimeException("GRN Jobwork record not found with id: " + id);
        }

        GRNJobWorkEntity existing = optional.get();

        existing.setUnit(updatedEntity.getUnit());
        existing.setMedcNumber(updatedEntity.getMedcNumber());
        existing.setInvoiceNumber(updatedEntity.getInvoiceNumber());
        existing.setSubContractorCode(updatedEntity.getSubContractorCode());
        existing.setSubContractorName(updatedEntity.getSubContractorName());
        existing.setGatePassRefNumber(updatedEntity.getGatePassRefNumber());
        existing.setEWayBillNumber(updatedEntity.getEWayBillNumber());
        existing.setVehicleNumber(updatedEntity.getVehicleNumber());
        existing.setInvoiceScanUrls(updatedEntity.getInvoiceScanUrls());
        existing.setDcDocumentScanUrls(updatedEntity.getDcDocumentScanUrls());
        existing.setEWayBillScanUrls(updatedEntity.getEWayBillScanUrls());
        existing.setVehicleDocumentsScanUrls(updatedEntity.getVehicleDocumentsScanUrls());
        existing.setWeightmentRefNumber(updatedEntity.getWeightmentRefNumber());
        existing.setLoadWeight(updatedEntity.getLoadWeight());
        existing.setEmptyWeight(updatedEntity.getEmptyWeight());
        existing.setMaterialUnloadingStatus(updatedEntity.getMaterialUnloadingStatus());

        existing.calculateDerivedFields();

        return repository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        log.info("Deleting GRN Jobwork record with id: {}", id);
        repository.deleteById(id);
    }

    private void generateGRNRefNumber(GRNJobWorkEntity entity) {
        String prefix = "MEGR";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));
        long count = repository.count() + 1;
        String sequence = String.format("%04d", count);
        entity.setGrnRefNumber(prefix + datePart + sequence);
    }
    
    public List<String> getPendingInvoiceNumbers() {
        return repository.findPendingInvoiceNumbersIgnoreCase();
    }
    
    @Override
    public List<GRNJobWorkEntity> updateMaterialUnloadingStatus(
            String invoiceNumber,
            String materialUnloadingStatus
    ) {

        List<GRNJobWorkEntity> grnList = repository.findByInvoiceNumber(invoiceNumber);

        if (grnList.isEmpty()) {
            throw new RuntimeException("No GRN Jobwork found for invoice: " + invoiceNumber);
        }

        // GRN jobwork update
        for (GRNJobWorkEntity grn : grnList) {
            grn.setMaterialUnloadingStatus(materialUnloadingStatus);
        }

        repository.saveAll(grnList);

        // Gate Inward update
        List<GateInwardEntity> inwardList = gateInwardRepo.findByInvoiceNumber(invoiceNumber);

        for (GateInwardEntity inward : inwardList) {
            inward.setMaterialUnloadingStatus(materialUnloadingStatus);
        }

        gateInwardRepo.saveAll(inwardList);

        return grnList;
    }

    @Override
    @Transactional
    public GRNJobWorkEntity approveById(Long id) {
        log.info("Approving GRN Jobwork record with id: {}", id);
        GRNJobWorkEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("GRN Jobwork record not found with id: " + id));

        // FRD §18.7 Step 5: Editing disabled if Approved
        if ("Approved".equalsIgnoreCase(entity.getMasterApprovalStatus())) {
            throw new RuntimeException("GRN Jobwork already approved, cannot approve again.");
        }

        entity.setMasterApprovalStatus("Approved");
        return repository.save(entity);
    }

    @Override
    @Transactional
    public GRNJobWorkEntity rejectById(Long id) {
        log.info("Rejecting GRN Jobwork record with id: {}", id);
        GRNJobWorkEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("GRN Jobwork record not found with id: " + id));

        // FRD §18.7 Step 6: If rejected, re-edit and re-approval required
        entity.setMasterApprovalStatus("Rejected");
        return repository.save(entity);
    }

}

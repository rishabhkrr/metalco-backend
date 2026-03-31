package com.indona.invento.services.impl;

import com.indona.invento.dao.POGenerationRepository;
import com.indona.invento.dao.POManagementApprovalRepository;
import com.indona.invento.dao.PORequestRepository;
import com.indona.invento.dto.POManagementApprovalDto;
import com.indona.invento.entities.POGenerationEntity;
import com.indona.invento.entities.POManagementApprovalEntity;
import com.indona.invento.entities.POManagementApprovalItemEntity;
import com.indona.invento.services.POManagementApprovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class POManagementApprovalServiceImpl implements POManagementApprovalService {

    @Autowired
    private final POManagementApprovalRepository repository;

    @Autowired
    private POGenerationRepository poGenerationRepository;

    @Autowired
    private PORequestRepository poRequestRepository;

    @Override
    public List<POManagementApprovalEntity> getAllApprovals() {
        List<POManagementApprovalEntity> approvals = repository.findAll();

        approvals.forEach(approval -> {
            approval.getItems().forEach(item -> {
                if (item.getPrNumber() != null) {
                    poRequestRepository.findByPrNumber(item.getPrNumber())
                            .ifPresent(req -> {
                                item.setSoLineNumber(req.getSoNumberLineNumber()); // ✅ set SO line number
                                item.setOrderType(req.getOrderType());             // ✅ set order type
                            });
                }
            });
        });

        return approvals;
    }


    @Override
    public POGenerationEntity approvePOByNumber(String poNumber) {
        // ✅ Step 1: Approve POGenerationEntity
        POGenerationEntity po = poGenerationRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));

        po.setPoStatus("PO APPROVED");

        // ✅ Step 2: Also update POManagementApprovalEntity
        POManagementApprovalEntity approval = repository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("Approval record not found for PO: " + poNumber));

        approval.setStatus("PO APPROVED");
        repository.save(approval);

        // ✅ Step 3: Save and return updated PO
        return poGenerationRepository.save(po);
    }


    @Override
    public POGenerationEntity rejectPOByNumber(String poNumber) {
        // ✅ Step 1: Update POGenerationEntity
        POGenerationEntity po = poGenerationRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));

        po.setPoStatus("PO REJECTED");

        // ✅ Step 2: Update POManagementApprovalEntity
        POManagementApprovalEntity approval = repository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("Approval record not found for PO: " + poNumber));

        approval.setStatus("PO REJECTED");
        repository.save(approval);

        // ✅ Step 3: Save and return updated PO
        return poGenerationRepository.save(po);
    }


    public POManagementApprovalEntity updateRemarksByPoNumber(String poNumber, String remarks) {
        POManagementApprovalEntity entity = repository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found with number: " + poNumber));
        entity.setRemarks(remarks);
        return repository.save(entity);
    }

    @Override
    public POGenerationEntity cancelPOByNumber(String poNumber) {
        POGenerationEntity po = poGenerationRepository.findByPoNumber(poNumber)
                .orElseThrow(() -> new RuntimeException("PO not found: " + poNumber));

        po.setPoStatus("PO CANCELLED");
        return poGenerationRepository.save(po);
    }

}

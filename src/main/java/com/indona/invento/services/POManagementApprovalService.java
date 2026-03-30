package com.indona.invento.services;


import com.indona.invento.dto.POManagementApprovalDto;
import com.indona.invento.entities.POGenerationEntity;
import com.indona.invento.entities.POManagementApprovalEntity;
import java.util.List;
import java.util.Optional;

public interface POManagementApprovalService {
    List<POManagementApprovalEntity> getAllApprovals();

    POGenerationEntity approvePOByNumber(String poNumber);

    POGenerationEntity rejectPOByNumber(String poNumber);

    POManagementApprovalEntity updateRemarksByPoNumber(String poNumber, String remarks);

    POGenerationEntity cancelPOByNumber(String poNumber);

}


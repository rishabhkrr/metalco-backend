package com.indona.invento.services;

import com.indona.invento.dto.PackingSubmissionDTO;
import com.indona.invento.dto.PackingSubmissionResponseDTO;
import com.indona.invento.entities.PackingSubmission;

import java.util.List;

public interface PackingSubmissionService {
    PackingSubmissionResponseDTO submitPackingForms(List<PackingSubmissionDTO> dtos);

    List<PackingSubmission> getAllPackingSubmissions();

    void deleteAllPackingSubmissions();

    void updatePackingPdf(String packingId, String pdf);
}

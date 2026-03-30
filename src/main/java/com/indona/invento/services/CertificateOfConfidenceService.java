package com.indona.invento.services;

import com.indona.invento.dto.CertificateOfConfidenceDTO;
import com.indona.invento.dto.CocLineItemDTO;
import com.indona.invento.entities.CertificateOfConfidenceEntity;

import java.util.List;

public interface CertificateOfConfidenceService {

    /**
     * Get CoC form for a specific bill (pre-filled with bill summary and all available line items)
     * Does NOT create CoC - just returns form data
     */
    CertificateOfConfidenceDTO getCoCFormForBill(String soNumber, String invoiceNumber, String lineNumber);

    /**
     * Generate PDF for CoC with selected line items
     * Creates CoC on-the-fly with auto-generated cocNumber and timestamp
     */
    CertificateOfConfidenceEntity generateCoCPdf(CertificateOfConfidenceDTO dto);

    /**
     * Get CoC by ID
     */
    CertificateOfConfidenceEntity getCoCById(Long cocId);
}


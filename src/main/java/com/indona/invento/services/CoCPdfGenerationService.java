package com.indona.invento.services;

import com.indona.invento.entities.CertificateOfConfidenceEntity;
import com.indona.invento.entities.CocLineItemEntity;

import java.io.ByteArrayOutputStream;

public interface CoCPdfGenerationService {

    /**
     * Generate PDF for a complete CoC
     */
    ByteArrayOutputStream generateCoCPdf(CertificateOfConfidenceEntity coc);

    /**
     * Generate PDF for a specific line item
     */
    ByteArrayOutputStream generateLineItemCoCPdf(CertificateOfConfidenceEntity coc, CocLineItemEntity lineItem);
}


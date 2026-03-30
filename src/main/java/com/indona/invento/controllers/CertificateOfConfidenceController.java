package com.indona.invento.controllers;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.indona.invento.dto.CertificateOfConfidenceDTO;
import com.indona.invento.dto.CocLineItemDTO;
import com.indona.invento.entities.CertificateOfConfidenceEntity;
import com.indona.invento.entities.CocLineItemEntity;
import com.indona.invento.services.CertificateOfConfidenceService;
import com.indona.invento.services.CoCPdfGenerationService;
import com.indona.invento.services.CoCDistributionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/certificate-of-confidence")
public class CertificateOfConfidenceController {

    @Autowired
    private CertificateOfConfidenceService cocService;

    @Autowired
    private CoCPdfGenerationService pdfGenerationService;

    @Autowired
    private CoCDistributionService distributionService;

    @Value("${azure.storage.connection-string}")
    private String azureStorageConnectionString;

    /**
     * Get CoC form for a specific bill (pre-filled with bill summary and all available line items)
     */
    @GetMapping("/form")
    public ResponseEntity<?> getCoCForm(
            @RequestParam String soNumber,
            @RequestParam String invoiceNumber,
            @RequestParam String lineNumber) {
        try {
            CertificateOfConfidenceDTO form = cocService.getCoCFormForBill(soNumber, invoiceNumber, lineNumber);
            if (form == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "Bill not found"));
            }
            return ResponseEntity.ok(Map.of("success", true, "data", form));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Generate PDF for CoC with selected line items
     * Creates CoC, uploads PDF to Azure Blob Storage, and returns blob URL
     */
    @PostMapping("/generate-pdf")
    public ResponseEntity<?> generateCoCPdf(@RequestBody CertificateOfConfidenceDTO dto) {
        try {
            // Generate and save CoC entity
            CertificateOfConfidenceEntity coc = cocService.generateCoCPdf(dto);
            
            // Generate PDF
            ByteArrayOutputStream pdfStream = pdfGenerationService.generateCoCPdf(coc);
            byte[] pdfBytes = pdfStream.toByteArray();
            
            // Upload PDF to Azure Blob Storage
            String filename = "CoC_" + coc.getCocNumber() + "_" + UUID.randomUUID().toString() + ".pdf";
            
            BlobServiceClientBuilder serviceClientBuilder = new BlobServiceClientBuilder()
                    .connectionString(azureStorageConnectionString);
            BlobServiceClient blobServiceClient = serviceClientBuilder.buildClient();
            
            // Get or create container for COC PDFs with public access
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient("coc-pdfs");
            if (!containerClient.exists()) {
                containerClient.create();
                // Set public access level to BLOB (allows public read access to blobs)
                containerClient.setAccessPolicy(com.azure.storage.blob.models.PublicAccessType.BLOB, null);
            }
            
            // Upload PDF to Azure Storage with headers to force download
            BlobClient blobClient = containerClient.getBlobClient(filename);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(pdfBytes);
            
            // Set headers to force download when opened in browser
            BlobHttpHeaders headers = new BlobHttpHeaders()
                    .setContentType("application/pdf")
                    .setContentDisposition("attachment; filename=\"" + "CoC_" + coc.getCocNumber() + ".pdf" + "\"");
            
            blobClient.upload(inputStream, pdfBytes.length, true);
            blobClient.setHttpHeaders(headers);
            
            // Get the blob URL
            String pdfUrl = blobClient.getBlobUrl();
            
            // Prepare response with COC data and PDF URL
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "CoC generated successfully");
            response.put("cocId", coc.getId());
            response.put("cocNumber", coc.getCocNumber());
            response.put("pdfUrl", pdfUrl);
            response.put("data", coc);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Download PDF for complete CoC
     */
    @GetMapping("/{cocId}/download-pdf")
    public ResponseEntity<?> downloadCoCPdf(@PathVariable Long cocId) {
        try {
            // Retrieve CoC from database
            CertificateOfConfidenceEntity coc = cocService.getCoCById(cocId);
            if (coc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "CoC not found"));
            }

            ByteArrayOutputStream pdfStream = pdfGenerationService.generateCoCPdf(coc);
            byte[] pdfBytes = pdfStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "CoC_" + coc.getCocNumber() + ".pdf");
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Send CoC PDF via Email
     */
    @PostMapping("/{cocId}/send-email")
    public ResponseEntity<?> sendCoCViaEmail(
            @PathVariable Long cocId,
            @RequestParam String recipientEmail
    ) {
        try {
            CertificateOfConfidenceEntity coc = cocService.getCoCById(cocId);
            if (coc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "CoC not found"));
            }

            ByteArrayOutputStream pdfStream = pdfGenerationService.generateCoCPdf(coc);
            distributionService.sendCoCViaEmail(recipientEmail, coc.getCustomerName(), coc.getCocNumber(), pdfStream);
            return ResponseEntity.ok(Map.of("success", true, "message", "CoC sent via email successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Send CoC PDF via WhatsApp
     */
    @PostMapping("/{cocId}/send-whatsapp")
    public ResponseEntity<?> sendCoCViaWhatsApp(
            @PathVariable Long cocId,
            @RequestParam String phoneNumber
    ) {
        try {
            CertificateOfConfidenceEntity coc = cocService.getCoCById(cocId);
            if (coc == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("success", false, "message", "CoC not found"));
            }

            ByteArrayOutputStream pdfStream = pdfGenerationService.generateCoCPdf(coc);
            distributionService.sendCoCViaWhatsApp(phoneNumber, coc.getCocNumber(), pdfStream);
            return ResponseEntity.ok(Map.of("success", true, "message", "CoC sent via WhatsApp successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}


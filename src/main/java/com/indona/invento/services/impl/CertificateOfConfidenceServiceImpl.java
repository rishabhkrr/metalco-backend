package com.indona.invento.services.impl;

import com.indona.invento.dao.BillingSummaryRepository;
import com.indona.invento.dao.CertificateOfConfidenceRepository;
import com.indona.invento.dao.CustomerMasterRepository;
import com.indona.invento.dao.PurchaseFollowUpV2Repository;
import com.indona.invento.dto.CertificateOfConfidenceDTO;
import com.indona.invento.dto.CocLineItemDTO;
import com.indona.invento.entities.BillingSummaryEntity;
import com.indona.invento.entities.CertificateOfConfidenceEntity;
import com.indona.invento.entities.CocLineItemEntity;
import com.indona.invento.entities.CustomerMasterEntity;
import com.indona.invento.entities.PurchaseFollowUpEntityV2;
import com.indona.invento.services.CertificateOfConfidenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CertificateOfConfidenceServiceImpl implements CertificateOfConfidenceService {

    private static final Logger logger = LoggerFactory.getLogger(CertificateOfConfidenceServiceImpl.class);

    @Autowired
    private CertificateOfConfidenceRepository cocRepository;

    @Autowired
    private BillingSummaryRepository billingSummaryRepository;

    @Autowired
    private CustomerMasterRepository customerMasterRepository;

    @Autowired
    private com.indona.invento.dao.SoSummaryRepository soSummaryRepository;

    @Autowired
    private com.indona.invento.dao.POGenerationRepository poGenerationRepository;

    @Autowired
    private PurchaseFollowUpV2Repository purchaseFollowUpV2Repository;

    /**
     * Get CoC form for a specific bill (pre-filled with bill summary and all available line items)
     * Does NOT create CoC - just returns form data
     */

    @Override
    public CertificateOfConfidenceDTO getCoCFormForBill(String soNumber, String invoiceNumber, String lineNumber) {
        // Check if CoC already exists for this SO and invoice (get the most recent one)
        List<CertificateOfConfidenceEntity> existingCocs = cocRepository.findBySoNumberAndInvoiceNumberOrderByCreatedAtDesc(soNumber, invoiceNumber);
        
        if (existingCocs != null && !existingCocs.isEmpty()) {
            // Return the most recent CoC data including declaration
            CertificateOfConfidenceEntity coc = existingCocs.get(0);
            
            // Convert line items to DTO
            List<CocLineItemDTO> lineItemDTOs = coc.getLineItems() != null ? 
                coc.getLineItems().stream()
                    .map(item -> CocLineItemDTO.builder()
                            .lineNumber(item.getLineNumber())
                            .productCategory(item.getProductCategory())
                            .itemDescription(item.getItemDescription())
                            .brand(item.getBrand())
                            .grade(item.getGrade())
                            .temper(item.getTemper())
                            .dimension(item.getDimension())
                            .quantityKg(item.getQuantityKg())
                            .build())
                    .collect(Collectors.toList()) : null;
            
            // Get available line items for this bill
            List<CocLineItemDTO> availableLineItems = getAvailableLineItemsForBill(soNumber, invoiceNumber, lineNumber);
            
            return CertificateOfConfidenceDTO.builder()
                    .id(coc.getId())
                    .cocNumber(coc.getCocNumber())
                    .timestamp(coc.getTimestamp())
                    .soNumber(coc.getSoNumber())
                    .invoiceNumber(coc.getInvoiceNumber())
                    .dispatchedQuantity(coc.getDispatchedQuantity())
                    .unit(coc.getUnit())
                    .customerPONumber(coc.getCustomerPONumber())
                    .customerPODate(coc.getCustomerPODate())
                    .customerPOQuantity(coc.getCustomerPOQuantity())
                    .customerCode(coc.getCustomerCode())
                    .customerName(coc.getCustomerName())
                    .customerBillingAddress(coc.getCustomerBillingAddress())
                    .customerShippingAddress(coc.getCustomerShippingAddress())
                    .customerEmail(coc.getCustomerEmail())
                    .customerPhone(coc.getCustomerPhone())
                    .declaration(coc.getDeclaration())
                    .lineItems(lineItemDTOs)
                    .selectedLineItems(lineItemDTOs)
                    .availableLineItems(availableLineItems)
                    .status(coc.getStatus())
                    .createdAt(coc.getCreatedAt())
                    .updatedAt(coc.getUpdatedAt())
                    .build();
        }
        
        // If no existing CoC, fetch billing summary data
        List<BillingSummaryEntity> billings = billingSummaryRepository
                .findByInvoiceNumberAndSoNumberAndLineNumber(invoiceNumber, soNumber, lineNumber);

        if (billings == null || billings.isEmpty()) {
            return null;
        }

        // Use the first billing record for customer and basic info (all duplicates should have same customer info)
        BillingSummaryEntity billing = billings.get(0);

        // Get customer details for email and phone (with contacts eagerly loaded)
        CustomerMasterEntity customer = customerMasterRepository.findByCustomerCodeWithContacts(billing.getCustomerCode());

        String customerEmail = null;
        String customerPhone = null;
        if (customer != null && customer.getContactDetails() != null && !customer.getContactDetails().isEmpty()) {
            customerEmail = customer.getContactDetails().get(0).getEmailId();
            customerPhone = customer.getContactDetails().get(0).getPhoneNumber();
        }

        // Get all available line items for this bill (filtered by SO, invoice, and line number)
        List<CocLineItemDTO> availableLineItems = getAvailableLineItemsForBill(soNumber, invoiceNumber, lineNumber);

        // Fetch PO information from SO Summary
        String customerPONumber = null;
        LocalDateTime customerPODate = null;
        String customerPOQuantity = null;
        
        com.indona.invento.entities.SoSummaryEntity soSummary = soSummaryRepository.findBySoNumber(soNumber);
        if (soSummary != null) {
            // Set PO Number if it exists
            if (soSummary.getCustomerPoNo() != null && !soSummary.getCustomerPoNo().isEmpty()) {
                customerPONumber = soSummary.getCustomerPoNo();
            }
            
            // Set PO Date from SO Summary timestamp
            if (soSummary.getTimestamp() != null) {
                customerPODate = soSummary.getTimestamp();
            }
            
            // Calculate PO Quantity by summing all billing items for SO + Line + Invoice
            List<BillingSummaryEntity> allBillings = billingSummaryRepository
                .findByInvoiceNumberAndSoNumberAndLineNumber(invoiceNumber, soNumber, lineNumber);
            
            if (allBillings != null && !allBillings.isEmpty()) {
                double totalQuantity = allBillings.stream()
                    .mapToDouble(b -> b.getQuantityKg() != null ? b.getQuantityKg().doubleValue() : 0.0)
                    .sum();
                customerPOQuantity = String.valueOf((int) totalQuantity);
            }
        }

        // Return form data (does NOT create CoC)
        return CertificateOfConfidenceDTO.builder()
                .soNumber(billing.getSoNumber())
                .invoiceNumber(billing.getInvoiceNumber())
                .dispatchedQuantity(billing.getQuantityKg().toString())
                .unit(billing.getUnit())
                .customerPONumber(customerPONumber)
                .customerPODate(customerPODate)
                .customerPOQuantity(customerPOQuantity)
                .customerCode(billing.getCustomerCode())
                .customerName(billing.getCustomerName())
                .customerBillingAddress(billing.getCustomerBillingAddress())
                .customerShippingAddress(billing.getCustomerShippingAddress())
                .customerEmail(customerEmail)
                .customerPhone(customerPhone)
                .availableLineItems(availableLineItems)
                .build();
    }

    /**
     * Helper method to get available line items for a bill
     * Returns all items matching the SO number, invoice number, and line number (including duplicates)
     */
    private List<CocLineItemDTO> getAvailableLineItemsForBill(String soNumber, String invoiceNumber, String lineNumber) {
        // Fetch all billing records matching the SO number, invoice number, and line number
        List<BillingSummaryEntity> billings = billingSummaryRepository
                .findByInvoiceNumberAndSoNumberAndLineNumber(invoiceNumber, soNumber, lineNumber);

        return billings.stream()
                .map(b -> CocLineItemDTO.builder()
                        .lineNumber(b.getLineNumber())
                        .productCategory(b.getProductCategory())
                        .itemDescription(b.getItemDescription())
                        .brand(b.getBrand())
                        .grade(b.getGrade())
                        .temper(b.getTemper())
                        .dimension(b.getDimension())
                        .quantityKg(b.getQuantityKg().toString())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Generate PDF for CoC with selected line items
     * Creates CoC on-the-fly with auto-generated cocNumber and timestamp
     */
    @Override
    public CertificateOfConfidenceEntity generateCoCPdf(CertificateOfConfidenceDTO dto) {
        logger.info("Generating CoC PDF for SO: {}, Invoice: {}", dto.getSoNumber(), dto.getInvoiceNumber());
        logger.info("Initial PO data - Number: {}, Date: {}, Quantity: {}", 
            dto.getCustomerPONumber(), dto.getCustomerPODate(), dto.getCustomerPOQuantity());
        
        // Ensure PO details are populated even if client did not send them
        populateMissingPoInfo(dto);
        
        logger.info("After populateMissingPoInfo - PO Number: {}, Date: {}, Quantity: {}", 
            dto.getCustomerPONumber(), dto.getCustomerPODate(), dto.getCustomerPOQuantity());

        // Create new CoC entity with auto-generated cocNumber
        CertificateOfConfidenceEntity coc = CertificateOfConfidenceEntity.builder()
                .cocNumber(generateCoCNumber())
                .timestamp(LocalDateTime.now())
                .soNumber(dto.getSoNumber())
                .invoiceNumber(dto.getInvoiceNumber())
                .dispatchedQuantity(dto.getDispatchedQuantity())
                .unit(dto.getUnit())
                .customerPONumber(dto.getCustomerPONumber())
                .customerPODate(dto.getCustomerPODate())
                .customerPOQuantity(dto.getCustomerPOQuantity())
                .customerCode(dto.getCustomerCode())
                .customerName(dto.getCustomerName())
                .customerBillingAddress(dto.getCustomerBillingAddress())
                .customerShippingAddress(dto.getCustomerShippingAddress())
                .customerEmail(dto.getCustomerEmail())
                .customerPhone(dto.getCustomerPhone())
                .declaration(dto.getDeclaration())
                .status("GENERATED")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Add selected line items with generated cocNumber and timestamp
        if (dto.getSelectedLineItems() != null && !dto.getSelectedLineItems().isEmpty()) {
            List<CocLineItemEntity> items = dto.getSelectedLineItems().stream()
                    .map(item -> CocLineItemEntity.builder()
                            .lineNumber(item.getLineNumber())
                            .productCategory(item.getProductCategory())
                            .itemDescription(item.getItemDescription())
                            .brand(item.getBrand())
                            .grade(item.getGrade())
                            .temper(item.getTemper())
                            .dimension(item.getDimension())
                            .quantityKg(item.getQuantityKg())
                            .cocNumber(generateCoCNumber())
                            .cocTimestamp(LocalDateTime.now())
                            .coc(coc)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build())
                    .collect(Collectors.toList());

            coc.setLineItems(items);
        }

        return cocRepository.save(coc);
    }

    private void populateMissingPoInfo(CertificateOfConfidenceDTO dto) {
        if (dto == null) {
            return;
        }

        boolean needsPoNumber = dto.getCustomerPONumber() == null || dto.getCustomerPONumber().isEmpty();
        boolean needsPoDate = dto.getCustomerPODate() == null;
        boolean needsPoQuantity = dto.getCustomerPOQuantity() == null || dto.getCustomerPOQuantity().isEmpty();

        if (!needsPoNumber && !needsPoDate && !needsPoQuantity) {
            return;
        }

        String soNumber = dto.getSoNumber();
        if (soNumber == null || soNumber.isEmpty()) {
            logger.warn("SO Number is null or empty, cannot fetch PO data");
            return;
        }

        // Look up PO data from SO Summary
        logger.info("Looking up PO data from SO Summary for SO Number: {}", soNumber);
        com.indona.invento.entities.SoSummaryEntity soSummary = soSummaryRepository.findBySoNumber(soNumber);
        
        if (soSummary != null) {
            logger.info("Found SO Summary - PO Number: {}, Date: {}", 
                soSummary.getCustomerPoNo(), soSummary.getTimestamp());

            // Set PO Number only if it exists in SO Summary
            if (needsPoNumber && soSummary.getCustomerPoNo() != null && !soSummary.getCustomerPoNo().isEmpty()) {
                dto.setCustomerPONumber(soSummary.getCustomerPoNo());
                logger.info("Set PO Number from SO Summary: {}", soSummary.getCustomerPoNo());
            }
            
            // Set PO Date from SO Summary timestamp
            if (needsPoDate && soSummary.getTimestamp() != null) {
                dto.setCustomerPODate(soSummary.getTimestamp());
                logger.info("Set PO Date from SO Summary: {}", soSummary.getTimestamp());
            }
            
            // Calculate PO Quantity by summing all billing items for SO + Line + Invoice
            if (needsPoQuantity && dto.getSelectedLineItems() != null && !dto.getSelectedLineItems().isEmpty()) {
                String lineNumber = dto.getSelectedLineItems().get(0).getLineNumber();
                String invoiceNumber = dto.getInvoiceNumber();
                
                logger.info("Calculating PO Quantity for SO: {}, Line: {}, Invoice: {}", 
                    soNumber, lineNumber, invoiceNumber);
                
                List<BillingSummaryEntity> billings = billingSummaryRepository
                    .findByInvoiceNumberAndSoNumberAndLineNumber(invoiceNumber, soNumber, lineNumber);
                
                if (billings != null && !billings.isEmpty()) {
                    double totalQuantity = billings.stream()
                        .mapToDouble(b -> b.getQuantityKg() != null ? b.getQuantityKg().doubleValue() : 0.0)
                        .sum();
                    
                    dto.setCustomerPOQuantity(String.valueOf((int) totalQuantity));
                    logger.info("Set PO Quantity (sum of {} items): {}", billings.size(), (int) totalQuantity);
                } else {
                    logger.warn("No billing records found for SO: {}, Line: {}, Invoice: {}", 
                        soNumber, lineNumber, invoiceNumber);
                }
            }
        } else {
            logger.warn("No SO Summary found for SO Number: {}", soNumber);
        }
    }

    /**
     * Get CoC by ID
     */
    @Override
    public CertificateOfConfidenceEntity getCoCById(Long cocId) {
        return cocRepository.findById(cocId).orElse(null);
    }

    // Helper method to generate COC number in format: MECOC + YYMM + sequence (e.g., MECOC25060255)
    private String generateCoCNumber() {
        String prefix = "MECOC";
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMM"));

        // Get count of COCs with this prefix to generate sequence
        String basePrefix = prefix + datePart;
        long count = cocRepository.countByPrefix(basePrefix);

        String cocNumber;
        int sequence = (int) count + 1;

        // Generate unique COC number with sequence
        do {
            String sequencePart = String.format("%04d", sequence);
            cocNumber = prefix + datePart + sequencePart;
            sequence++;
        } while (cocRepository.existsByCocNumber(cocNumber));

        return cocNumber;
    }
}


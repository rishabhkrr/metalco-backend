package com.indona.invento.services.impl;

import com.indona.invento.dao.CreditDebitNoteRepository;
import com.indona.invento.dto.CreditDebitNoteDTO;
import com.indona.invento.entities.CreditDebitNoteEntity;
import com.indona.invento.services.CreditDebitNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CreditDebitNoteServiceImpl implements CreditDebitNoteService {

    @Autowired
    private CreditDebitNoteRepository repository;

    @Override
    @Transactional
    public CreditDebitNoteEntity createCreditDebitNote(CreditDebitNoteDTO dto) {
        
        // Step 1: Generate transaction number based on type
        String transactionNumber = generateTransactionNumber(dto.getTransactionType());

        // Step 2: Create credit/debit note entity
        CreditDebitNoteEntity entity = CreditDebitNoteEntity.builder()
                .timestamp(LocalDateTime.now())
                .transactionNumber(transactionNumber)
                .transactionType(dto.getTransactionType())
                .receiver(dto.getReceiver())
                .invoiceNumber(dto.getInvoiceNumber())
                .soNumber(dto.getSoNumber())
                .lineNumber(dto.getLineNumber())
                .unit(dto.getUnit())
                .customerCode(dto.getCustomerCode())
                .customerName(dto.getCustomerName())
                .customerBillingAddress(dto.getCustomerBillingAddress())
                .customerShippingAddress(dto.getCustomerShippingAddress())
                .orderType(dto.getOrderType())
                .productCategory(dto.getProductCategory())
                .itemDescription(dto.getItemDescription())
                .brand(dto.getBrand())
                .grade(dto.getGrade())
                .temper(dto.getTemper())
                .dimension(dto.getDimension())
                .itemPrice(dto.getItemPrice())
                .transactionAmount(dto.getTransactionAmount())
                .cgst(dto.getCgst())
                .sgst(dto.getSgst())
                .igst(dto.getIgst())
                .totalAmount(dto.getTotalAmount())
                .createdBy(dto.getCreatedBy())
                .build();

        // Step 3: Save and return
        return repository.save(entity);
    }

    /**
     * Generate transaction number based on type
     * CREDIT -> MECRD(YY)(MM)(0001)
     * DEBIT -> MEDB(YY)(MM)(0001)
     */
    private String generateTransactionNumber(String transactionType) {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyMM"));
        
        // Determine prefix based on transaction type
        String prefix = transactionType.equalsIgnoreCase("CREDIT") ? "MECRD" : "MEDB";
        
        // Get the latest transaction number for this type
        Optional<String> latestNumberOpt = repository.findLatestTransactionNumberByType(transactionType);
        
        int nextSequence = 1;
        
        if (latestNumberOpt.isPresent()) {
            String latestNumber = latestNumberOpt.get();
            // Extract the last 4 digits (sequence number)
            String sequencePart = latestNumber.substring(latestNumber.length() - 4);
            int currentSequence = Integer.parseInt(sequencePart);
            
            // Check if it's the same month, if yes increment, otherwise reset to 1
            String latestYearMonth = latestNumber.substring(prefix.length(), prefix.length() + 4);
            if (latestYearMonth.equals(yearMonth)) {
                nextSequence = currentSequence + 1;
            }
        }
        
        // Format: MECRD2511(0001) or MEDB2511(0001)
        return String.format("%s%s%04d", prefix, yearMonth, nextSequence);
    }

    @Override
    public List<CreditDebitNoteEntity> getAllCreditDebitNotes() {
        return repository.findAll();
    }

    @Override
    public List<CreditDebitNoteEntity> getCreditDebitNotesByInvoice(String invoiceNumber) {
        return repository.findByInvoiceNumber(invoiceNumber);
    }

    @Override
    public List<CreditDebitNoteEntity> getCreditDebitNotesBySo(String soNumber) {
        return repository.findBySoNumber(soNumber);
    }

    @Override
    public List<CreditDebitNoteEntity> getCreditDebitNotesByType(String transactionType) {
        return repository.findByTransactionType(transactionType);
    }

    @Override
    public CreditDebitNoteEntity getCreditDebitNoteById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit/Debit note not found with id: " + id));
    }

    @Override
    public void deleteAll() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║  🗑️  DELETE ALL CREDIT/DEBIT NOTES   ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        try {
            long totalCount = repository.count();
            System.out.println("📊 Total credit/debit notes before deletion: " + totalCount);

            repository.deleteAll();

            long afterCount = repository.count();
            System.out.println("✅ All credit/debit notes deleted successfully!");
            System.out.println("📊 Total credit/debit notes after deletion: " + afterCount);
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║     ✅ DELETION COMPLETE               ║");
            System.out.println("╚════════════════════════════════════════╝\n");
        } catch (Exception e) {
            System.err.println("❌ Error deleting all credit/debit notes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete all credit/debit notes: " + e.getMessage());
        }
    }
}

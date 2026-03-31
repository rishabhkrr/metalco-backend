package com.indona.invento.services.impl;

import com.indona.invento.dao.PurchaseCreditDebitNoteRepository;
import com.indona.invento.dto.PurchaseCreditDebitNoteDTO;
import com.indona.invento.entities.PurchaseCreditDebitNoteEntity;
import com.indona.invento.services.PurchaseCreditDebitNoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseCreditDebitNoteServiceImpl implements PurchaseCreditDebitNoteService {

    private final PurchaseCreditDebitNoteRepository repository;

    @Override
    public PurchaseCreditDebitNoteEntity createCreditDebitNote(PurchaseCreditDebitNoteDTO dto) {
        String transactionNumber = generateTransactionNumber(dto.getTransactionType());

        PurchaseCreditDebitNoteEntity entity = PurchaseCreditDebitNoteEntity.builder()
                .timestamp(LocalDateTime.now())
                .transactionNumber(transactionNumber)
                .transactionType(dto.getTransactionType())
                .receiver(dto.getReceiver())
                .invoiceNumber(dto.getInvoiceNumber())
                .poNumber(dto.getPoNumber())
                .unit(dto.getUnit())
                .supplierCode(dto.getSupplierCode())
                .supplierName(dto.getSupplierName())
                .supplierBillingAddress(dto.getSupplierBillingAddress())
                .supplierShippingAddress(dto.getSupplierShippingAddress())
                .productCategory(dto.getProductCategory())
                .itemDescription(dto.getItemDescription())
                .sectionNumber(dto.getSectionNumber())
                .brand(dto.getBrand())
                .grade(dto.getGrade())
                .itemPrice(dto.getItemPrice())
                .transactionAmount(dto.getTransactionAmount())
                .cgst(dto.getCgst())
                .sgst(dto.getSgst())
                .igst(dto.getIgst())
                .totalAmount(dto.getTotalAmount())
                .createdBy(dto.getCreatedBy())
                .build();

        return repository.save(entity);
    }

    @Override
    public List<PurchaseCreditDebitNoteEntity> getAllCreditDebitNotes() {
        return repository.findAll();
    }

    @Override
    public List<PurchaseCreditDebitNoteEntity> getByInvoiceNumber(String invoiceNumber) {
        return repository.findByInvoiceNumber(invoiceNumber);
    }

    @Override
    public List<PurchaseCreditDebitNoteEntity> getByPoNumber(String poNumber) {
        return repository.findByPoNumber(poNumber);
    }

    @Override
    public List<PurchaseCreditDebitNoteEntity> getByTransactionType(String transactionType) {
        return repository.findByTransactionType(transactionType);
    }

    @Override
    public PurchaseCreditDebitNoteEntity getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Purchase Credit/Debit Note not found with ID: " + id));
    }

    private String generateTransactionNumber(String transactionType) {
        LocalDateTime now = LocalDateTime.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyMM"));

        String prefix = transactionType.equalsIgnoreCase("CREDIT") ? "MECRD" : "MEDB";

        Optional<String> latestNumberOpt = repository.findLatestTransactionNumberByType(transactionType);

        int nextSequence = 1;

        if (latestNumberOpt.isPresent()) {
            String latestNumber = latestNumberOpt.get();
            String sequencePart = latestNumber.substring(latestNumber.length() - 4);
            int currentSequence = Integer.parseInt(sequencePart);

            String latestYearMonth = latestNumber.substring(prefix.length(), prefix.length() + 4);
            if (latestYearMonth.equals(yearMonth)) {
                nextSequence = currentSequence + 1;
            }
        }

        return String.format("%s%s%04d", prefix, yearMonth, nextSequence);
    }
}


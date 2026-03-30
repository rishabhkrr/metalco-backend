package com.indona.invento.services;

import com.indona.invento.dto.PurchaseCreditDebitNoteDTO;
import com.indona.invento.entities.PurchaseCreditDebitNoteEntity;

import java.util.List;

public interface PurchaseCreditDebitNoteService {
    PurchaseCreditDebitNoteEntity createCreditDebitNote(PurchaseCreditDebitNoteDTO dto);
    List<PurchaseCreditDebitNoteEntity> getAllCreditDebitNotes();
    List<PurchaseCreditDebitNoteEntity> getByInvoiceNumber(String invoiceNumber);
    List<PurchaseCreditDebitNoteEntity> getByPoNumber(String poNumber);
    List<PurchaseCreditDebitNoteEntity> getByTransactionType(String transactionType);
    PurchaseCreditDebitNoteEntity getById(Long id);
}


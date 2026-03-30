package com.indona.invento.services;

import com.indona.invento.dto.CreditDebitNoteDTO;
import com.indona.invento.entities.CreditDebitNoteEntity;

import java.util.List;

public interface CreditDebitNoteService {

    /**
     * Create a new credit or debit note
     */
    CreditDebitNoteEntity createCreditDebitNote(CreditDebitNoteDTO dto);

    /**
     * Get all credit/debit notes
     */
    List<CreditDebitNoteEntity> getAllCreditDebitNotes();

    /**
     * Get credit/debit notes by invoice number
     */
    List<CreditDebitNoteEntity> getCreditDebitNotesByInvoice(String invoiceNumber);

    /**
     * Get credit/debit notes by SO number
     */
    List<CreditDebitNoteEntity> getCreditDebitNotesBySo(String soNumber);

    /**
     * Get credit/debit notes by transaction type (CREDIT or DEBIT)
     */
    List<CreditDebitNoteEntity> getCreditDebitNotesByType(String transactionType);

    /**
     * Get credit/debit note by ID
     */
    CreditDebitNoteEntity getCreditDebitNoteById(Long id);

    void deleteAll();
}

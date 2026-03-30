package com.indona.invento.dao;

import com.indona.invento.entities.CreditDebitNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditDebitNoteRepository extends JpaRepository<CreditDebitNoteEntity, Long> {

    /**
     * Find the latest transaction number for a specific transaction type
     * Used for generating sequential numbers
     */
    @Query("SELECT c.transactionNumber FROM CreditDebitNoteEntity c WHERE c.transactionType = :transactionType ORDER BY c.id DESC LIMIT 1")
    Optional<String> findLatestTransactionNumberByType(@Param("transactionType") String transactionType);

    /**
     * Find all credit/debit notes by invoice number
     */
    List<CreditDebitNoteEntity> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find all credit/debit notes by SO number
     */
    List<CreditDebitNoteEntity> findBySoNumber(String soNumber);

    /**
     * Find all credit/debit notes by transaction type
     */
    List<CreditDebitNoteEntity> findByTransactionType(String transactionType);
}


package com.indona.invento.dao;

import com.indona.invento.entities.PurchaseCreditDebitNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseCreditDebitNoteRepository extends JpaRepository<PurchaseCreditDebitNoteEntity, Long> {

    @Query("SELECT p.transactionNumber FROM PurchaseCreditDebitNoteEntity p WHERE p.transactionType = :transactionType ORDER BY p.id DESC LIMIT 1")
    Optional<String> findLatestTransactionNumberByType(@Param("transactionType") String transactionType);

    List<PurchaseCreditDebitNoteEntity> findByInvoiceNumber(String invoiceNumber);

    List<PurchaseCreditDebitNoteEntity> findByPoNumber(String poNumber);

    List<PurchaseCreditDebitNoteEntity> findByTransactionType(String transactionType);
}


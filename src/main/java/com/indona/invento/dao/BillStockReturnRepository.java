package com.indona.invento.dao;

import com.indona.invento.entities.BillStockReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillStockReturnRepository extends JpaRepository<BillStockReturnEntity, Long> {
    
    // Find all stock returns by invoice number
    List<BillStockReturnEntity> findByInvoiceNumber(String invoiceNumber);
    
    // Find all stock returns by SO number
    List<BillStockReturnEntity> findBySoNumber(String soNumber);
    
    // Find all stock returns by stock selection type
    List<BillStockReturnEntity> findByStockSelection(String stockSelection);
    
    // Find by invoice, SO, and line number combination
    List<BillStockReturnEntity> findByInvoiceNumberAndSoNumberAndLineNumber(
        String invoiceNumber, String soNumber, String lineNumber);

    // Find the latest sales return number for generating next number
    @Query("SELECT b.salesReturnNumber FROM BillStockReturnEntity b ORDER BY b.id DESC LIMIT 1")
    Optional<String> findLatestSalesReturnNumber();
}


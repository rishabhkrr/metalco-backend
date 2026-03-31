package com.indona.invento.dao;

import com.indona.invento.entities.PurchaseReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PurchaseReturnRepository extends JpaRepository<PurchaseReturnEntity, Long> {
    
    // Find the latest purchase return number for generating next number
    @Query("SELECT p.purchaseReturnNumber FROM PurchaseReturnEntity p ORDER BY p.id DESC LIMIT 1")
    Optional<String> findLatestPurchaseReturnNumber();
}


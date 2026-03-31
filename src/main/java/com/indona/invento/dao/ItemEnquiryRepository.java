package com.indona.invento.dao;

import com.indona.invento.entities.ItemEnquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ItemEnquiryRepository extends JpaRepository<ItemEnquiry, Long> {

    Optional<ItemEnquiry> findByQuotationNo(String quotationNo);


    long count(); // totalRecords

    long countByStatus(String status);

    @Query("SELECT e FROM ItemEnquiry e WHERE e.createdAt >= :from AND e.createdAt <= :to")
    Page<ItemEnquiry> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("SELECT e FROM ItemEnquiry e WHERE e.createdAt >= :from AND e.createdAt <= :to")
    List<ItemEnquiry> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    List<ItemEnquiry> findByCustomerName(String customerName);

    // FRD BLK-004: Find quotations created before a threshold with specific statuses for auto-expiry
    List<ItemEnquiry> findByCreatedAtBeforeAndStatusIn(LocalDateTime threshold, List<String> statuses);

}

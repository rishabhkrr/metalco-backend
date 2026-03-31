package com.indona.invento.dao;

import com.indona.invento.entities.SalesOrder;
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
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findBySoNumber(String soNumber);

    @Query("SELECT s FROM SalesOrder s WHERE s.createdAt >= :from AND s.createdAt <= :to")
    Page<SalesOrder> findByCreatedAtBetween(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    // Find all pending SOs with overdue customers
    List<SalesOrder> findByStatusAndCustomerOverdue(String status, Boolean customerOverdue);

    // Find all SOs by status (e.g., "Pending for Approval")
    List<SalesOrder> findByStatusIgnoreCase(String status);


    @Query("SELECT so FROM SalesOrder so JOIN so.items li " +
            "WHERE so.unit = :unit AND li.itemDescription = :itemDescription AND so.status = 'ACTIVE'")
    List<SalesOrder> findActiveOrdersByUnitAndItemDescription(@Param("unit") String unit,
                                                              @Param("itemDescription") String itemDescription);

}


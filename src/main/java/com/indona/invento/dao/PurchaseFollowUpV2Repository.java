package com.indona.invento.dao;

import com.indona.invento.entities.PurchaseFollowUpEntityV2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PurchaseFollowUpV2Repository extends JpaRepository<PurchaseFollowUpEntityV2, Long> {
    Optional<PurchaseFollowUpEntityV2> findByPoNumberAndSalesOrderNumber(String poNumber, String salesOrderNumber);

    // PO summary lookup: get rows for given SO + line, most recent first
    List<PurchaseFollowUpEntityV2> findBySalesOrderNumberAndLineItemNumberOrderByOrderDateDesc(
            String salesOrderNumber,
            String lineItemNumber
    );
    
    // Find PO data by sales order number only, most recent first
    List<PurchaseFollowUpEntityV2> findBySalesOrderNumberOrderByOrderDateDesc(String salesOrderNumber);
}

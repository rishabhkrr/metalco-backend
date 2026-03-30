package com.indona.invento.dao;

import com.indona.invento.entities.SalesOrderLineItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SalesOrderLineItemRepository extends JpaRepository<SalesOrderLineItem, Long> {
    Optional<SalesOrderLineItem> findByLineNumber(String lineNumber);

    @Query("SELECT li FROM SalesOrderLineItem li WHERE li.lineNumber = :lineNumber AND li.salesOrder.soNumber = :soNumber")
    Optional<SalesOrderLineItem> findBySoNumberAndLineNumber(@Param("soNumber") String soNumber, @Param("lineNumber") String lineNumber);


    Optional<SalesOrderLineItem> findBySalesOrder_SoNumberAndLineNumber(String soNumber, String lineNumber);
}

package com.indona.invento.dao;

import com.indona.invento.entities.SalesmanIncentiveEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalesmanIncentiveEntryRepository extends JpaRepository<SalesmanIncentiveEntryEntity, Long> {

    // Find all pending entries for a customer
    List<SalesmanIncentiveEntryEntity> findByCustomerCodeAndCustomerNameAndPaymentStatus(
            String customerCode, String customerName, String paymentStatus);

    // Find the last entry for a customer (most recent dispatch date)
    @Query("SELECT s FROM SalesmanIncentiveEntryEntity s WHERE s.customerCode = :customerCode " +
           "AND s.customerName = :customerName ORDER BY s.dispatchDate DESC")
    List<SalesmanIncentiveEntryEntity> findByCustomerCodeAndCustomerNameOrderByDispatchDateDesc(
            @Param("customerCode") String customerCode,
            @Param("customerName") String customerName);
}


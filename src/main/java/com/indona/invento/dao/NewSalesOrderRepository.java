package com.indona.invento.dao;

import com.indona.invento.entities.NewSalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewSalesOrderRepository extends JpaRepository<NewSalesOrder, Long> {
    Optional<NewSalesOrder> findBySoNumber(String soNumber);
}

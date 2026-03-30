package com.indona.invento.dao;

import com.indona.invento.entities.WarehouseStockTransferEntityScheduler;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseStockTransferSchedulerRepository extends JpaRepository<WarehouseStockTransferEntityScheduler, Long> {
    List<WarehouseStockTransferEntityScheduler> findBySoNumber(String soNumber);
}

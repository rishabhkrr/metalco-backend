package com.indona.invento.dao;

import com.indona.invento.entities.StockTransferWarehouseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransferWarehouseRepository extends JpaRepository<StockTransferWarehouseEntity, Long> {
}

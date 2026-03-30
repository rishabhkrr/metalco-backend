package com.indona.invento.dao;

import com.indona.invento.entities.WarehouseStockTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface WarehouseStockTransferRepository extends JpaRepository<WarehouseStockTransferEntity, Long> {

    List<WarehouseStockTransferEntity> findBySoNumberAndLineNumber(String soTrimmed, String lineNumber);
}

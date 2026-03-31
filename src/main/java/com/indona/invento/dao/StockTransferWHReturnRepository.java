package com.indona.invento.dao;

import com.indona.invento.entities.StockTransferWHReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockTransferWHReturnRepository extends JpaRepository<StockTransferWHReturnEntity, Long> {
}

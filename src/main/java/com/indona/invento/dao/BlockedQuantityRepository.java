package com.indona.invento.dao;

import com.indona.invento.entities.BlockedProductEntity;
import com.indona.invento.entities.BlockedQuantityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface BlockedQuantityRepository extends JpaRepository<BlockedQuantityEntity, Long> {

    List<BlockedQuantityEntity> findByCreatedAtBefore(Date cutoffDate);

    @Query("SELECT DISTINCT bq FROM BlockedQuantityEntity bq JOIN FETCH bq.products bp WHERE bp.itemDescription = :itemDescription")
    List<BlockedQuantityEntity> findByItemDescription(@Param("itemDescription") String itemDescription);

    Optional<BlockedQuantityEntity> findByQuotationNo(String quotationNo);
}

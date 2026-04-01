package com.indona.invento.dao;

import com.indona.invento.entities.BillingBatchDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingBatchDetailRepository extends JpaRepository<BillingBatchDetailEntity, Long> {

    List<BillingBatchDetailEntity> findByBillingSummaryItemId(Long billingSummaryItemId);
}

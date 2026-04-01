package com.indona.invento.dao;

import com.indona.invento.entities.BillingSummaryItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingSummaryItemRepository extends JpaRepository<BillingSummaryItemEntity, Long> {

    List<BillingSummaryItemEntity> findByBillingSummaryId(Long billingSummaryId);

    List<BillingSummaryItemEntity> findBySoNumberAndLineNumber(String soNumber, String lineNumber);
}

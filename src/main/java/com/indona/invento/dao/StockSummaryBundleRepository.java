package com.indona.invento.dao;

import com.indona.invento.entities.StockSummaryBundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockSummaryBundleRepository extends JpaRepository<StockSummaryBundleEntity, Long> {

    // Find bundles by GRN Number
    List<StockSummaryBundleEntity> findByGrnNumber(String grnNumber);

    // Find bundles by Stock Summary ID
    List<StockSummaryBundleEntity> findByStockSummaryId(Long stockSummaryId);

    // Find bundles by Transfer Number
    List<StockSummaryBundleEntity> findByTransferNumber(String transferNumber);

    // Find bundles by GRN Number and Item Description
    List<StockSummaryBundleEntity> findByGrnNumberAndItemDescription(String grnNumber, String itemDescription);

    // Find bundles by Stock Summary ID and GRN Number
    List<StockSummaryBundleEntity> findByStockSummaryIdAndGrnNumber(Long stockSummaryId, String grnNumber);

    // Delete bundles by GRN Number and Stock Summary ID
    void deleteByStockSummaryIdAndGrnNumber(Long stockSummaryId, String grnNumber);
}


package com.indona.invento.dao;

import com.indona.invento.entities.GrnLineItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface GrnLineItemRepository extends JpaRepository<GrnLineItemEntity, Long> {

    List<GrnLineItemEntity> findByGrnNumber(String grnNumber);

    List<GrnLineItemEntity> findByGrnId(Long grnId);

    List<GrnLineItemEntity> findByTransferNumber(String transferNumber);

    List<GrnLineItemEntity> findByStockTransferId(Long stockTransferId);

    List<GrnLineItemEntity> findByStatus(String status);

    Optional<GrnLineItemEntity> findByQrCode(String qrCode);

    List<GrnLineItemEntity> findByGrnNumberAndStatus(String grnNumber, String status);

    List<GrnLineItemEntity> findByQrGeneratedTrue();

    void deleteByGrnNumber(String grnNumber);

    // Find by GRN number and item description
    List<GrnLineItemEntity> findByGrnNumberAndItemDescription(String grnNumber, String itemDescription);

    // Find by GRN number, item description and quantity for exact match
    @Query("SELECT g FROM GrnLineItemEntity g WHERE g.grnNumber = :grnNumber AND g.itemDescription = :itemDescription AND g.weightmentQuantityKg = :quantityKg")
    Optional<GrnLineItemEntity> findByGrnNumberAndItemDescriptionAndQuantity(
            @Param("grnNumber") String grnNumber,
            @Param("itemDescription") String itemDescription,
            @Param("quantityKg") BigDecimal quantityKg);
}

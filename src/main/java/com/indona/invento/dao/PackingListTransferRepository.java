package com.indona.invento.dao;

import com.indona.invento.entities.PackingListTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PackingListTransferRepository extends JpaRepository<PackingListTransferEntity, Long> {
    PackingListTransferEntity findByPackingListNumber(String packingListNumber);
    PackingListTransferEntity findBySoNumberAndLineNumber(String soNumber, String lineNumber);

    // Get all items for a specific RFD List (by packingListNumber)
    List<PackingListTransferEntity> findByPackingListNumberOrderByIdAsc(String packingListNumber);

    // Get distinct packingListNumbers for summary grouping
    @Query("SELECT DISTINCT e.packingListNumber FROM PackingListTransferEntity e ORDER BY e.packingListNumber DESC")
    List<String> findDistinctPackingListNumbers();
}

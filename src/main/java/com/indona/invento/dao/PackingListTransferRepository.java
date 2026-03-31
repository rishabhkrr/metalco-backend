package com.indona.invento.dao;

import com.indona.invento.entities.PackingListTransferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackingListTransferRepository extends JpaRepository<PackingListTransferEntity, Long> {
    PackingListTransferEntity findByPackingListNumber(String packingListNumber);
    PackingListTransferEntity findBySoNumberAndLineNumber(String soNumber, String lineNumber);
}

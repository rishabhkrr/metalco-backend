package com.indona.invento.dao;

import com.indona.invento.entities.ManualCashInHandEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ManualCashInHandRepository extends JpaRepository<ManualCashInHandEntity, Long> {
	List<ManualCashInHandEntity> findByCreationDateBetweenAndStoreId(Date startDate, Date endDate, Long storeId);
}
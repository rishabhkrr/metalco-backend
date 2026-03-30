package com.indona.invento.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.AdjustmentEntity;
import com.indona.invento.entities.StockinEntity;
  
@Repository
public interface AdjustmentRepository extends JpaRepository<AdjustmentEntity, Long> {

	List<AdjustmentEntity> findByStoreId(Long storeId);

	List<AdjustmentEntity> findByRefNo(String id);

	boolean existsByRefNo(String string);

	List<AdjustmentEntity> findByStoreIdOrderByDateTimeDesc(Long storeId);

	List<AdjustmentEntity> findAllByOrderByDateTimeDesc(); 

}
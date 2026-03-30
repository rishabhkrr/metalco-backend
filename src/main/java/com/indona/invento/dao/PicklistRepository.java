package com.indona.invento.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.PicklistEntity;
  
@Repository
public interface PicklistRepository extends JpaRepository<PicklistEntity, Long> { 

	List<PicklistEntity> findByRefNoAndStoreIdAndTypeOrderByDateTime(Long refNo, Long storeId, String type);
}
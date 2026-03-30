package com.indona.invento.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.SkuEntity;
  
@Repository
public interface SkusRepository extends JpaRepository<SkuEntity, Long> { 
	SkuEntity findBySkuCode(String SkuCode);
}
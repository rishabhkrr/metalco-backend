package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.BinsEntity;
  
@Repository
public interface BinsRepository extends JpaRepository<BinsEntity, Long> {

	BinsEntity findByBinName(String binName); 

}
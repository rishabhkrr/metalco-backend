package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.RequisitionEntity;
  
@Repository
public interface RequisitionsRepository extends JpaRepository<RequisitionEntity, Long> { 

}
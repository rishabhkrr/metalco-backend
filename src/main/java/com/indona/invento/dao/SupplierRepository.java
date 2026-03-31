package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.SupplierEntity;
  
@Repository
public interface SupplierRepository extends JpaRepository<SupplierEntity, Long> { 

}
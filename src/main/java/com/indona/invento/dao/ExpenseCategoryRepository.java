package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.DealerEntity;
import com.indona.invento.entities.ExpenseCategoryEntity;
import com.indona.invento.entities.SupplierEntity;
  
@Repository
public interface ExpenseCategoryRepository extends JpaRepository<ExpenseCategoryEntity, Long> { 

}
package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.indona.invento.entities.StoresEntity;
  
@Repository
public interface StoresRepository extends JpaRepository<StoresEntity, Long> { 

}
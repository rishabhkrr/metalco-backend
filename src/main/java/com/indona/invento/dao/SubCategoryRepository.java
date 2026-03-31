package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.indona.invento.entities.SubCategoryEntity;
  
@Repository
public interface SubCategoryRepository extends JpaRepository<SubCategoryEntity, Long> { 

}
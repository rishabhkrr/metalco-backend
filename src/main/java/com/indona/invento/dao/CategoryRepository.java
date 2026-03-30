package com.indona.invento.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.indona.invento.entities.CategoryEntity;
  
@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> { 

}